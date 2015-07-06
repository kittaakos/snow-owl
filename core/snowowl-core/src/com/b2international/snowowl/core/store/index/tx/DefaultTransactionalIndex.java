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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.Map;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.search.SearchHits;

import com.b2international.commons.exceptions.FormattedRuntimeException;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.store.index.BulkIndex;
import com.b2international.snowowl.core.store.index.Index;
import com.b2international.snowowl.core.store.index.IndexQueryBuilder;
import com.b2international.snowowl.core.store.index.MappingStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Adds transaction support to an existing {@link Index} using a combination of components with revision properties and commit groups, the index can
 * act as a transactional store.
 * 
 * @since 5.0
 */
public class DefaultTransactionalIndex implements TransactionalIndex {
	
	private BulkIndex index;
	private ObjectMapper mapper;

	private BranchManager branchManager;
	private TransactionalIndexAdmin admin;

	public DefaultTransactionalIndex(BulkIndex index, ObjectMapper mapper, BranchManager branchManager) {
		this.index = checkNotNull(index, "index");
		this.mapper = checkNotNull(mapper, "mapper");
		this.branchManager = checkNotNull(branchManager, "branchManager");
		this.admin = new DefaultTransactionalIndexAdmin(index.admin());
	}

	@Override
	public BulkIndex index() {
		return index;
	}
	
	@Override
	public Map<String, Object> loadRevision(String type, String branchPath, long storageKey) {
		try {
			final SearchHits hits = query(type, branchPath)
					.where(termQuery(IndexRevision.STORAGE_KEY, storageKey))
					.sortDesc(IndexCommit.COMMIT_TIMESTAMP_FIELD)
					.sortAsc(IndexRevision.STORAGE_KEY)
					.search();
			if (hits.totalHits() <= 0) {
				// TODO add branchPath to exception message
				throw new NotFoundException(type, String.valueOf(storageKey));
			}
			return this.mapper.convertValue(hits.hits()[0].getSource(), DefaultIndexRevision.class).getData();
		} catch (ElasticsearchException e) {
			throw new FormattedRuntimeException("Failed to retrieve '%s' from branch '%s' in index %s/%s", storageKey, branchPath, index.name(), type, e);
		}
	}

	@Override
	public void addRevision(int commitId, long commitTimestamp, long storageKey, String branchPath, String type, Map<String, Object> data) {
		checkArgument(storageKey > 0, "StorageKey should be greater than zero");
		final Map<String, Object> revData = this.mapper.convertValue(new DefaultIndexRevision(commitId, commitTimestamp, storageKey, false, data), Map.class);
		this.index.putWithParent(type, String.valueOf(commitId), revData);
	}
	
	@Override
	public void remove(int commitId, long commitTimestamp, long storageKey, String branchPath, String type) {
		final Map<String, Object> revData = loadRevision(type, branchPath, storageKey);
		final Map<String, Object> revision = this.mapper.convertValue(new DefaultIndexRevision(commitId, commitTimestamp, storageKey, true, revData), Map.class);
		this.index.putWithParent(type, String.valueOf(commitId), revision);
	}
	
	@Override
	public void commit(int commitId, long commitTimestamp, String branchPath, String commitMessage) {
		final MappingStrategy<IndexCommit> commitMapping = mapping(IndexCommit.class);
		final Map<String, Object> commit = commitMapping.convert(new IndexCommit(commitId, commitTimestamp, branchPath, commitMessage));
		this.index.put(IndexCommit.COMMIT_TYPE, String.valueOf(commitId), commit);
		this.index.flush(commitId);
	}
	
	@Override
	public IndexTransaction transaction(int commitId, long commitTimestamp, String branchPath) {
		this.index.create(commitId);
		return new DefaultIndexTransaction(this, commitId, commitTimestamp, branchPath, mapper);
	}
	
	@Override
	public TransactionalIndexAdmin admin() {
		return admin;
	}
	
	@Override
	public <T> MappingStrategy<T> mapping(Class<T> type) {
		return admin().mappings().getMapping(type);
	}
	
	@Override
	public IndexQueryBuilder query(String type, String branchPath) {
		return new TransactionalIndexQueryBuilder(this, branchManager, type, branchPath);
	}
	
}
