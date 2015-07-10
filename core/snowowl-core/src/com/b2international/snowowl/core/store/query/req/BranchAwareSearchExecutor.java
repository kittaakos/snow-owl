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
package com.b2international.snowowl.core.store.query.req;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.hasParentFilter;
import static org.elasticsearch.index.query.FilterBuilders.orFilter;
import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.b2international.commons.ClassUtils;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.store.index.tx.DefaultTransactionalQueryBuilder;
import com.b2international.snowowl.core.store.index.tx.IndexCommit;
import com.b2international.snowowl.core.store.query.DefaultQueryBuilder;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;

/**
 * @since 5.0
 */
public class BranchAwareSearchExecutor extends DefaultSearchExecutor {

	private BranchManager branchManager;

	public BranchAwareSearchExecutor(SearchResponseProcessor processor, BranchManager branchManager) {
		super(processor);
		this.branchManager = checkNotNull(branchManager, "BranchManager may not be null");
	}

	@Override
	protected void buildQuery(SearchRequestBuilder req, AfterWhereBuilder builder) {
		super.buildQuery(req, builder);
		req.addSort(IndexCommit.COMMIT_TIMESTAMP_FIELD, SortOrder.DESC);
	}
	
	@Override
	protected QueryBuilder getQuery(DefaultQueryBuilder builder) {
		final DefaultTransactionalQueryBuilder qb = ClassUtils.checkAndCast(builder, DefaultTransactionalQueryBuilder.class);
		final FilterBuilder branchFilter = branchFilter(qb.getBranchPath());
		return QueryBuilders.filteredQuery(super.getQuery(qb), branchFilter);
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
