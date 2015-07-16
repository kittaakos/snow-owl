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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.MapMaker;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;

import com.b2international.commons.ClassUtils;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.store.index.tx.Revision;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.store.query.Query.QueryBuilder;
import com.google.common.base.Stopwatch;

/**
 * @since 5.0
 */
public class DefaultBulkIndex implements BulkIndex, InternalIndex {

	private static final Logger LOG = Loggers.REPOSITORY.log();
	private static final int BULK_THRESHOLD = 5000;
	
	private final ConcurrentMap<Integer, BulkProcessor> activeBulkProcessors = new MapMaker().makeMap();
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
	public <T> void put(T object) {
		bulkIndex(prepareIndex(getType(object.getClass()), null, object));
	}
	
	@Override
	public <T> void put(String key, T object) {
		put(getType(object.getClass()), key, object);
	}
	
	@Override
	public void put(String type, String key, Object object) {
		bulkIndex(prepareIndex(type, key, object));
	}

	@Override
	public <T> void putWithParent(String parentKey, String key, T object) {
		putWithParent(getType(object.getClass()), parentKey, key, object);
	}
	
	@Override
	public void putWithParent(String type, String parentKey, String key, Object object) {
		bulkIndex(prepareIndexWithParent(type, parentKey, key, object));
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
	
	@Override
	public <T> void updateByScript(Class<T> type, String key, String script, Map<String, Object> params) {
		updateByScript(getType(type), key, script, params);
	}
	
	@Override
	public void updateByScript(String type, String key, String script, Map<String, Object> params) {
		bulkUpdate(prepareUpdateByScript(type, key, script, params));
	}
	
	@Override
	public UpdateRequestBuilder prepareUpdateByScript(String type, String key, String script, Map<String, Object> params) {
		return index.prepareUpdateByScript(type, key, script, params);
	}
	
	private void bulkIndex(final IndexRequestBuilder req) {
		final Map<String, Object> source = req.request().sourceAsMap();
		// TODO remove this limitation somehow
		checkArgument(source.containsKey(Revision.COMMIT_ID), "BulkIndex cannot be used without transaction support, use it via TransactionalIndex");
		final int bulkId = (int) source.get(Revision.COMMIT_ID);
		getBulkProcessor(bulkId).add(req.request());
	}
	
	private boolean bulkDelete(final DeleteRequestBuilder req) {
		throw new UnsupportedOperationException("TODO implement how to get the commit identifier"); 
	}
	
	private void bulkUpdate(UpdateRequestBuilder req) {
		final Map<String, Object> params = req.request().scriptParams();
		checkArgument(params.containsKey(Revision.COMMIT_ID), "BulkUpdate cannot be used without transaction support, use it via TransactionalIndex");
		final int commitId = (int) params.get(Revision.COMMIT_ID);
		getBulkProcessor(commitId).add(req.request());
	}
	
	@Override
	public QueryBuilder query() {
		return index.query();
	}
	
	@Override
	public <T> boolean remove(Class<T> type, String key) {
		return remove(getType(type), key);
	}
	
	@Override
	public boolean remove(String type, String key) {
		return bulkDelete(prepareDelete(type, key));
	}
	
	@Override
	public <T> Iterable<T> search(AfterWhereBuilder query, Class<T> type) {
		return index.search(query, type);
	}
	
	@Override
	public Iterator<SearchHit> scan(org.elasticsearch.index.query.QueryBuilder queryBuilder) {
		return index.scan(queryBuilder);
	}
	
	@Override
	public void create(int bulkId) {
		final BulkProcessor processor = BulkProcessor.builder(index.client(), new BulkProcessor.Listener() {
			
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {
				LOG.info("Executing bulk request of {}", request.requests().size());
			}
			
			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
				LOG.error("Failed to process bulk request", failure);
			}
			
			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
				if (response.hasFailures()) {
					for (BulkItemResponse item : response.getItems()) {
						if (item.getFailureMessage() != null) {
							System.out.println(item.getFailureMessage());
						}
					}
				} else {
					LOG.info("Processed bulk request of {} requests in {}", response.getItems().length, response.getTook());
				}
			}
		})
		.setBulkActions(BULK_THRESHOLD)
		.setBulkSize(new ByteSizeValue(512L, ByteSizeUnit.MB))
		.setConcurrentRequests(2)
		.setFlushInterval(new TimeValue(5, TimeUnit.SECONDS)).build();
		activeBulkProcessors.putIfAbsent(bulkId, processor);
	}
	
	@Override
	public void flush(int bulkId) {
		try {
			boolean allBulkCompleted = getBulkProcessor(bulkId).awaitClose(30, TimeUnit.SECONDS);
			if (!allBulkCompleted) {
				System.out.println("There are remaining unprocessed bulk requests after 30 seconds of bulk processor close");
			}
			final Stopwatch watch = Stopwatch.createStarted();
			client().admin().indices().prepareRefresh(name()).get();
			LOG.info("Refresh index '{}' in {}", name(), watch);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void deleteScript(String lang, String key) {
		index.deleteScript(lang, key);
	}
	
	@Override
	public void putScript(String lang, String key, String source) {
		index.putScript(lang, key, source);
	}
	
	private BulkProcessor getBulkProcessor(int bulkId) {
		return activeBulkProcessors.get(bulkId);
	}

}
