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
package com.b2international.snowowl.core.store.index.tx;

import java.util.Objects;

import com.b2international.snowowl.core.store.index.Mapping;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @since 5.0
 */
@Mapping(type = "commit", mapping = "commit_mapping.json")
class IndexCommit {
	
	static final String COMMIT_TYPE = "commit";
	static final String COMMIT_ID_FIELD = "commitId";
	static final String BRANCH_PATH_FIELD = "branchPath";
	static final String COMMIT_TIMESTAMP_FIELD = "commitTimestamp";
	// do we need baseTimestamp on commit or on revision???
	private static final String BASE_TIMESTAMP_FIELD = "baseTimestamp";
	
	private int commitId;
	private long commitTimestamp;
	private String branchPath;
	private String commitMessage;
	
	@JsonCreator
	IndexCommit(@JsonProperty("commitId") int commitId, @JsonProperty("commitTimestamp") long commitTimestamp, @JsonProperty("branchPath") String branchPath, @JsonProperty("commitMessage") String commitMessage) {
		this.branchPath = branchPath;
		this.commitId = commitId;
		this.commitTimestamp = commitTimestamp;
		this.commitMessage = commitMessage;
	}
	
	public int getCommitId() {
		return commitId;
	}
	
	public String getBranchPath() {
		return branchPath;
	}
	
	public long getCommitTimestamp() {
		return commitTimestamp;
	}
	
	public String getCommitMessage() {
		return commitMessage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(commitId, commitTimestamp, branchPath, commitMessage);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		IndexCommit other = (IndexCommit) obj;
		return Objects.equals(commitId, other.commitId) && Objects.equals(commitTimestamp, other.commitTimestamp) && Objects.equals(branchPath, other.branchPath) && Objects.equals(commitMessage, other.commitMessage);
	}
	
//	private static final int INDEX_THRESHOLD = 100_000;
//	private transient ElasticsearchIndex index;
//	private transient BulkRequestBuilder bulk;
//	private transient AtomicInteger bulkSize = new AtomicInteger(0);
//	private transient Collection<ListenableActionFuture<BulkResponse>> awaitingResponses = Collections.synchronizedSet(Sets.<ListenableActionFuture<BulkResponse>>newHashSet());
	
//	public void index(Component obj) {
//		addRevision(new IndexRevision(this, obj));
//	}
//
//	public void delete(Component obj) {
//		addRevision(new IndexRevision(this, obj, true));
//	}
//	
//	private void addRevision(IndexRevision rev) {
//		checkAndSendBulk();
//		final IndexRequest request = index.prepareAdd(rev).request();
//		bulk.add(request);
//		bulkSize.incrementAndGet();
//	}
//
//	private void checkAndSendBulk() {
//		synchronized (bulk) {
//			if (bulkSize.compareAndSet(INDEX_THRESHOLD, 0)) {
//				sendBulk();
//			}
//		}
//	}
//
//	/*Make sure you send the bulk request only when it contains at least one request*/
//	private void sendBulk() {
//		synchronized (bulk) {
//			final BulkRequestBuilder current = this.bulk;
//			final ListenableActionFuture<BulkResponse> future = current.execute();
//			awaitingResponses.add(future);
//			future.addListener(new ActionListener<BulkResponse>() {
//				@Override
//				public void onResponse(BulkResponse response) {
//					System.out.println("Indexed " + response.getItems().length + " in " + response.getTookInMillis() + "ms");
//					awaitingResponses.remove(future);
//				}
//				@Override
//				public void onFailure(Throwable e) {
//					throw new RuntimeException(e);
//				}
//			});
//			this.bulk = this.index.client().prepareBulk();
//		}
//	}
//	
//	public void commit() {
//		if (bulkSize.intValue() > 0) {
//			sendBulk();
//			while (!awaitingResponses.isEmpty()) {
//				try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					throw new RuntimeException(e);
//				}
//			}
//		}
//		index.commit(this);
//	}
//	
//	public Component get(String type, String id) {
//		return index.load(type, branchPath, id);
//	}

	/*
	 * Non-api
	 */
//	Collection<Component> changes() {
//		// search is always sorted by ID and commitTimestamp
//		final SearchResponse search = index.newQuery(branchPath).setBranchOnly(true).where(QueryBuilders.matchAllQuery()).search();
//		final Map<Object, Component> changes = newHashMap();
//		for (SearchHit hit : search.getHits()) {
//			// id should be the very first sort key, the second is revision
//			final Object id = hit.getSortValues()[0];
//			if (!changes.containsKey(id)) {
//				final MappingStrategy<Component> mappingStrategy = index.getMappingStrategy(hit.getType());
//				changes.put(id, mappingStrategy.convert(hit.getSource()));
//			}
//		}
//		return changes.values();
//	}

}
