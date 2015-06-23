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
package com.b2international.snowowl.core.index;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.snowowl.core.index.mapping.ComponentMappingStrategy;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.google.common.collect.Sets;

/**
 * @since 5.0
 */
@JsonAutoDetect(getterVisibility=Visibility.NON_PRIVATE)
public class IndexCommit {
	
	private static final Logger LOG = LoggerFactory.getLogger(IndexCommit.class);
	private static final int INDEX_THRESHOLD = 100_000;
	
	private int commitId;
	private String branchPath;
	private long commitTimestamp;
	
	private transient Index index;
	private transient BulkRequestBuilder bulk;
	private transient AtomicInteger bulkSize = new AtomicInteger(0);
	private transient Collection<ListenableActionFuture<BulkResponse>> awaitingResponses = Collections.synchronizedSet(Sets.<ListenableActionFuture<BulkResponse>>newHashSet());

	IndexCommit(Index index, String branchPath, int commitId, long commitTimestamp) {
		this.index = index;
		this.branchPath = branchPath;
		this.commitId = commitId;
		this.commitTimestamp = commitTimestamp;
		this.bulk = this.index.client().prepareBulk();
	}
	
	int getTransactionId() {
		return commitId;
	}
	
	String getBranchPath() {
		return branchPath;
	}
	
	public void index(Component obj) {
		addRevision(new IndexRevision(this, obj));
	}

	public void delete(Component obj) {
		addRevision(new IndexRevision(this, obj, true));
	}
	
	private void addRevision(IndexRevision rev) {
		checkAndSendBulk();
		final IndexRequest request = index.prepareAdd(rev).request();
		LOG.info("Added revision to commit: {}", request);
		bulk.add(request);
		bulkSize.incrementAndGet();
	}

	private void checkAndSendBulk() {
		synchronized (bulk) {
			if (bulkSize.compareAndSet(INDEX_THRESHOLD, 0)) {
				sendBulk();
			}
		}
	}

	/*Make sure you send the bulk request only when it contains at least one request*/
	private void sendBulk() {
		synchronized (bulk) {
			final BulkRequestBuilder current = this.bulk;
			final ListenableActionFuture<BulkResponse> future = current.execute();
			awaitingResponses.add(future);
			future.addListener(new ActionListener<BulkResponse>() {
				@Override
				public void onResponse(BulkResponse response) {
					System.out.println("Indexed " + response.getItems().length + " in " + response.getTookInMillis() + "ms");
					awaitingResponses.remove(future);
				}
				@Override
				public void onFailure(Throwable e) {
					throw new RuntimeException(e);
				}
			});
			this.bulk = this.index.client().prepareBulk();
		}
	}
	
	public void commit() {
		if (bulkSize.intValue() > 0) {
			sendBulk();
			while (!awaitingResponses.isEmpty()) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
		index.commit(this);
	}
	
	long getCommitTimestamp() {
		return commitTimestamp;
	}

	public Component get(String type, String id) {
		return index.load(type, branchPath, id);
	}

	/*
	 * Non-api
	 */
	Collection<Component> changes() {
		// search is always sorted by ID and commitTimestamp
		final SearchResponse search = index.newQuery(branchPath).setBranchOnly(true).where(QueryBuilders.matchAllQuery()).search();
		final Map<Object, Component> changes = newHashMap();
		for (SearchHit hit : search.getHits()) {
			// id should be the very first sort key, the second is revision
			final Object id = hit.getSortValues()[0];
			if (!changes.containsKey(id)) {
				final ComponentMappingStrategy<? extends Component> mappingStrategy = index.getMappingStrategy(hit.getType());
				changes.put(id, mappingStrategy.fromJSON(hit.getSource()));
			}
		}
		return changes.values();
	}

}
