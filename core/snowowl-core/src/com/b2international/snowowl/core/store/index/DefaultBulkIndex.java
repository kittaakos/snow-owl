/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.b2international.snowowl.core.store.index;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.MapMaker;
import org.slf4j.Logger;

import com.b2international.commons.ClassUtils;
import com.b2international.snowowl.core.exceptions.SnowOwlException;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.store.index.tx.IndexCommit;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.store.query.Query.QueryBuilder;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * @since 5.0
 */
public class DefaultBulkIndex implements BulkIndex, InternalIndex {

	private static final Logger LOG = Loggers.REPOSITORY.log();
	private static final int BULK_THRESHOLD = 10000;
	
	private final ConcurrentMap<Integer, BulkRequestBuilder> activeBulks = new MapMaker().makeMap();
	private final Multimap<Integer, ListenableActionFuture<BulkResponse>> pendingBulks = Multimaps.synchronizedMultimap(HashMultimap.<Integer, ListenableActionFuture<BulkResponse>>create());
	private InternalIndex index;

	public DefaultBulkIndex(Index index) {
		this.index = ClassUtils.checkAndCast(index, InternalIndex.class);
	}
	
	@Override
	public IndexAdmin admin() {
		return this.index.admin();
	}
	
	@Override
	public <T> T get(Class<T> type, String key) {
		return index.get(type, key);
	}
	
	@Override
	public Map<String, Object> get(String type, String key) {
		return index.get(type, key);
	}
	
	@Override
	public <T> MappingStrategy<T> mapping(Class<T> type) {
		return index.mapping(type);
	}
	
	@Override
	public String name() {
		return index.name();
	}
	
	@Override
	public <T> void put(String key, T object) {
		put(index.getType(object.getClass()), key, object);
	}
	
	@Override
	public void put(String type, String key, Object object) {
		bulkIndex(index.prepareIndex(type, key, object));
	}

	@Override
	public <T> void putWithParent(String parentKey, String key, T object) {
		putWithParent(index.getType(object.getClass()), parentKey, key, object);
	}
	
	@Override
	public void putWithParent(String type, String parentKey, String key, Object object) {
		bulkIndex(index.prepareIndexWithParent(type, parentKey, key, object));
	}
	
	@Override
	public Client client() {
		return index.client();
	}
	
	@Override
	public <T> String getType(Class<T> type) {
		return index.getType(type);
	}
	
	@Override
	public DeleteRequestBuilder prepareDelete(String type, String key) {
		return index.prepareDelete(type, key);
	}
	
	@Override
	public IndexRequestBuilder prepareIndex(String type, String key, Object object) {
		return index.prepareIndex(type, key, object);
	}
	
	@Override
	public IndexRequestBuilder prepareIndexWithParent(String type, String parentKey, String key, Object object) {
		return index.prepareIndexWithParent(type, parentKey, key, object);
	}
	
	private void bulkIndex(final IndexRequestBuilder req) {
		final Map<String, Object> source = req.request().sourceAsMap();
		// TODO remove this limitation somehow
		checkArgument(source.containsKey(IndexCommit.COMMIT_ID_FIELD), "BulkIndex cannot be used without index transaction support, use it via TransactionalIndex");
		final int bulkId = (int) source.get(IndexCommit.COMMIT_ID_FIELD);
		getBulkRequest(bulkId).add(req);
	}
	
	private boolean bulkDelete(final DeleteRequestBuilder req) {
		throw new UnsupportedOperationException("TODO implement how to get the commit identifier"); 
	}
	
	@Override
	public QueryBuilder query() {
		return index.query();
	}
	
	@Override
	public <T> boolean remove(Class<T> type, String key) {
		return remove(index.getType(type), key);
	}
	
	@Override
	public boolean remove(String type, String key) {
		return bulkDelete(index.prepareDelete(type, key));
	}
	
	@Override
	public <T> Iterable<T> search(AfterWhereBuilder query, Class<T> type) {
		return index.search(query, type);
	}
	
	@Override
	public void create(int bulkId) {
		activeBulks.putIfAbsent(bulkId, index.client().prepareBulk());
	}
	
	@Override
	public void flush(int bulkId) {
		executeBulk(bulkId, true);
		// wait for all currently registered pending bulks
		try {
			do {
				Thread.sleep(200);
			} while(pendingBulks.containsKey(bulkId));
			final Stopwatch watch = Stopwatch.createStarted();
			client().admin().indices().prepareRefresh(name()).get();
			LOG.info("Refresh index '{}' in {}", name(), watch);
		} catch (InterruptedException e) {
			throw new SnowOwlException("Failed to wait for pending bulk flush", e);
		}
	}
	
	private BulkRequestBuilder getBulkRequest(int bulkId) {
		executeBulk(bulkId, false);
		return activeBulks.get(bulkId);
	}

	private void executeBulk(final int bulkId, final boolean force) {
		checkArgument(activeBulks.containsKey(bulkId), "Create a bulk before using this kind of index");
		final BulkRequestBuilder req = activeBulks.get(bulkId);
		if (req.numberOfActions() >= BULK_THRESHOLD || force) {
			synchronized (req) {
				if (req.numberOfActions() >= BULK_THRESHOLD || force) {
					if (activeBulks.replace(bulkId, req, index.client().prepareBulk())) {
						final ListenableActionFuture<BulkResponse> future = req.execute();
						pendingBulks.put(bulkId, future);
						future.addListener(new ActionListener<BulkResponse>() {
							@Override
							public void onResponse(BulkResponse response) {
								LOG.info("Processed bulk request of {} in {}", response.getItems().length, response.getTook());
								pendingBulks.remove(bulkId, future);
							}
							
							@Override
							public void onFailure(Throwable e) {
								// FIXME handle failure
								pendingBulks.remove(bulkId, future);
							}
						});
					}
				}
			}		
		}
	}

}
