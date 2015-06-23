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

import static com.google.common.base.Preconditions.checkArgument;
import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.hasParentFilter;
import static org.elasticsearch.index.query.FilterBuilders.orFilter;
import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;

/**
 * @since 5.0
 */
public class IndexQueryBuilder {

	private Index index;
	
	private QueryBuilder where;
	private String branchPath;
	private FilterBuilder filter;

	private String type;
	private boolean branchOnly = false;
	
	public IndexQueryBuilder(Index index, String branchPath) {
		this(index, null, branchPath);
	}
	
	public IndexQueryBuilder(Index index, String type, String branchPath) {
		this.index = index;
		this.type = type;
		this.branchPath = branchPath;
	}
	
	public IndexQueryBuilder setBranchOnly(boolean branchOnly) {
		this.branchOnly = branchOnly;
		return this;
	}
	
	public IndexQueryBuilder where(QueryBuilder where) {
		checkArgument(!(where instanceof FilteredQueryBuilder), "Where clause should not be a filtered query builder, use #filter instead");
		this.where = where;
		return this;
	}
	
	public IndexQueryBuilder filter(FilterBuilder filter) {
		this.filter = filter;
		return this;
	}
	
	public SearchResponse search() {
		return this.index.search(type, this.toQuery());
	}

	private QueryBuilder toQuery() {
		return filteredQuery(where, createBranchFilter(filter));
	}

	private FilterBuilder createBranchFilter(FilterBuilder additionalFilter) {
		final String[] segments = branchPath.split("/");
		if (!branchOnly && (segments.length > 1 || additionalFilter != null)) {
			final OrFilterBuilder or = orFilter();
			String prev = "";
			for (int i = 0; i < segments.length; i++) {
				final String segment = segments[i];
				// we need the current segment + prevSegment to make it full path and the next one to restrict head timestamp on current based on base of the next one
				String current = "";
				String next = null;
				if (!Index.MAIN_BRANCH.equals(segment)) {
					current = prev.concat("/");
				}
				// if not the last segment, compute next one
				current = current.concat(segment);
				if (!segments[segments.length - 1].equals(segment)) {
					if (!current.endsWith("/")) {
						next = current.concat("/");
					}
					next = next.concat(segments[i+1]);
				}
				or.add(hasParentFilter(Index.COMMIT_TYPE, andFilter(termFilter(Index.BRANCH_PATH_FIELD, current), next == null ? timestampFilter(current) : timestampFilter(current, next))));
				prev = current;
			}
			if (additionalFilter != null) {
				or.add(additionalFilter);
			}
			return or;
		} else {
			return hasParentFilter(Index.COMMIT_TYPE, andFilter(termFilter(Index.BRANCH_PATH_FIELD, branchPath), timestampFilter(branchPath)));
		}
	}
	
	private FilterBuilder timestampFilter(String parentBranchPath, String childToRestrict) {
		// restrict given branchPath's HEAD to baseTimestamp of child
		return timestampFilter(parentBranchPath, this.index.getBaseTimestamp(childToRestrict));
	}
	
	private FilterBuilder timestampFilter(String branchPath) {
		return timestampFilter(branchPath, this.index.getHeadTimestamp(branchPath));
	}
	
	private FilterBuilder timestampFilter(String branchPath, long headTimestamp) {
		return rangeFilter(Index.COMMIT_TIMESTAMP_FIELD).gte(this.index.getBaseTimestamp(branchPath)).lte(headTimestamp);
	}

}
