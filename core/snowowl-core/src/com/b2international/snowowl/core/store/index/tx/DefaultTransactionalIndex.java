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
import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.hasParentFilter;
import static org.elasticsearch.index.query.FilterBuilders.orFilter;
import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.Map;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.search.SearchHits;

import com.b2international.commons.exceptions.FormattedRuntimeException;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.store.index.Index;
import com.b2international.snowowl.core.store.index.MappingStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Adds transaction support to an existing {@link Index} using a combination of components with revision properties and commit groups, the index can
 * act as a transactional store.
 * 
 * @since 5.0
 */
public class DefaultTransactionalIndex implements TransactionalIndex {
	
	private Index index;
	private ObjectMapper mapper;

	private BranchManager branchManager;
	private TransactionalIndexAdmin admin;

	public DefaultTransactionalIndex(Index index, ObjectMapper mapper, BranchManager branchManager) {
		this.index = checkNotNull(index, "index");
		this.mapper = checkNotNull(mapper, "mapper");
		this.branchManager = checkNotNull(branchManager, "branchManager");
		this.admin = new DefaultTransactionalIndexAdmin(index.admin());
	}

	@Override
	public Map<String, Object> loadRevision(String type, String branchPath, long storageKey) {
		try {
			final SearchHits hits = index
					.query(type)
					.where(termQuery(IndexRevision.STORAGE_KEY, storageKey))
					.filter(branchFilter(branchPath))
					.sortDesc(IndexCommit.COMMIT_TIMESTAMP_FIELD)
					.sortAsc(IndexRevision.STORAGE_KEY)
					.search();
			if (hits.totalHits() <= 0) {
				// TODO add branchPath to exception message
				throw new NotFoundException(type, String.valueOf(storageKey));
			}
			return this.mapper.convertValue(hits.hits()[0].getSource(), IndexRevision.class).getData();
		} catch (ElasticsearchException e) {
			throw new FormattedRuntimeException("Failed to retrieve '%s' from branch '%s' in index %s/%s", storageKey, branchPath, index.name(), type, e);
		}
	}

	@Override
	public void addRevision(int commitId, long commitTimestamp, long storageKey, String branchPath, String type, Map<String, Object> data) {
		checkArgument(storageKey > 0, "StorageKey should be greater than zero");
		final Map<String, Object> revData = this.mapper.convertValue(new IndexRevision(commitId, commitTimestamp, storageKey, false, data), Map.class);
		this.index.putWithParent(type, String.valueOf(commitId), revData);
	}
	
	@Override
	public void remove(int commitId, long commitTimestamp, long storageKey, String branchPath, String type) {
		final Map<String, Object> revData = loadRevision(type, branchPath, storageKey);
		final Map<String, Object> revision = this.mapper.convertValue(new IndexRevision(commitId, commitTimestamp, storageKey, true, revData), Map.class);
		this.index.putWithParent(type, String.valueOf(commitId), revision);
	}
	
	@Override
	public void commit(int commitId, long commitTimestamp, String branchPath, String commitMessage) {
		final MappingStrategy<IndexCommit> commitMapping = mapping(IndexCommit.class);
		final Map<String, Object> commit = commitMapping.convert(new IndexCommit(commitId, commitTimestamp, branchPath, commitMessage));
		this.index.put(IndexCommit.COMMIT_TYPE, String.valueOf(commitId), commit);
	}
	
	@Override
	public IndexTransaction transaction(int commitId, long commitTimestamp, String branchPath) {
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
	
	private FilterBuilder branchFilter(String branchPath) {
		final String[] segments = branchPath.split(Branch.SEPARATOR);
		if (/*!branchOnly && */(segments.length > 1 /*|| additionalFilter != null*/)) {
			final OrFilterBuilder or = orFilter();
			String prev = "";
			for (int i = 0; i < segments.length; i++) {
				final String segment = segments[i];
				// we need the current segment + prevSegment to make it full path and the next one to restrict head timestamp on current based on base of the next one
				String current = "";
				String next = null;
				if (!Branch.MAIN_PATH.equals(segment)) {
					current = prev.concat(Branch.SEPARATOR);
				}
				// if not the last segment, compute next one
				current = current.concat(segment);
				if (!segments[segments.length - 1].equals(segment)) {
					if (!current.endsWith(Branch.SEPARATOR)) {
						next = current.concat(Branch.SEPARATOR);
					}
					next = next.concat(segments[i+1]);
				}
				or.add(hasParentFilter(IndexCommit.COMMIT_TYPE, andFilter(termFilter(IndexCommit.BRANCH_PATH_FIELD, current), next == null ? timestampFilter(current) : timestampFilter(current, next))));
				prev = current;
			}
			return or;
		} else {
			return hasParentFilter(IndexCommit.COMMIT_TYPE, andFilter(termFilter(IndexCommit.BRANCH_PATH_FIELD, branchPath), timestampFilter(branchPath)));
		}
	}

	/*restricts given branchPath's HEAD to baseTimestamp of child*/
	private FilterBuilder timestampFilter(String parentBranchPath, String childToRestrict) {
		final long baseTimestamp = branchManager.getBranch(childToRestrict).baseTimestamp();
		return timestampFilter(parentBranchPath, baseTimestamp);
	}
	
	private FilterBuilder timestampFilter(String branchPath) {
		final long headTimestamp = branchManager.getBranch(branchPath).headTimestamp();
		return timestampFilter(branchPath, headTimestamp);
	}
	
	private FilterBuilder timestampFilter(String branchPath, long headTimestamp) {
		final long baseTimestamp = branchManager.getBranch(branchPath).baseTimestamp();
		return rangeFilter(IndexCommit.COMMIT_TIMESTAMP_FIELD).gte(baseTimestamp).lte(headTimestamp);
	}
	
}
