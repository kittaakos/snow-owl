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

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.b2international.commons.ClassUtils;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.store.index.tx.IndexCommit;
import com.b2international.snowowl.core.store.index.tx.Revision;
import com.b2international.snowowl.core.store.query.DefaultQueryBuilder;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.store.query.SortBy;
import com.b2international.snowowl.core.store.query.SortBy.MultiSortBy;
import com.b2international.snowowl.core.store.query.SortBy.SortByField;

/**
 * @since 5.0
 */
public class AggregatingBranchSearchExecutor extends BranchAwareSearchExecutor {

	public static final String STORAGE_KEY_GROUPING_AGG = "storageKeyAggregation";
	public static final String TOP_HIT_DOCS_AGG = "topHitDocs";
	
	public AggregatingBranchSearchExecutor(AggregationSearchResponseProcessor processor, BranchManager branchManager) {
		super(processor, branchManager);
	}

	@Override
	protected void buildRequest(SearchRequestBuilder req, AfterWhereBuilder builder) {
		final DefaultQueryBuilder qb = ClassUtils.checkAndCast(builder, DefaultQueryBuilder.class);
		req.setQuery(getQuery(qb)).setFrom(0).setSize(0);
		
		final TopHitsBuilder topHits = AggregationBuilders.topHits(TOP_HIT_DOCS_AGG).setSize(1);
		
		final SortBy sortBy = qb.getSortBy();
		if (sortBy instanceof SortByField) {
			topHits.addSort(((SortByField) sortBy).getField(), getSortMode((SortByField) sortBy));
		} else if (sortBy instanceof MultiSortBy) {
			for (SortBy sort : ((MultiSortBy) sortBy).getItems()) {
				topHits.addSort(((SortByField) sort).getField(), getSortMode((SortByField) sort));
			}
		}
		
		topHits.addSort(IndexCommit.COMMIT_TIMESTAMP_FIELD, SortOrder.DESC);
		req.addAggregation(
				AggregationBuilders
					.terms(STORAGE_KEY_GROUPING_AGG).field(Revision.STORAGE_KEY)
					.subAggregation(topHits));
	}

}
