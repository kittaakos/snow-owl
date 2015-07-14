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
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.b2international.snowowl.core.store.query.DefaultQueryBuilder;
import com.b2international.snowowl.core.store.query.Not;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;

/**
 * @since 5.0
 */
public class NegatingSearchExecutor extends DefaultSearchExecutor {

	public NegatingSearchExecutor(SearchResponseProcessor processor) {
		super(processor);
	}
	
	@Override
	protected void buildQuery(SearchRequestBuilder req, AfterWhereBuilder builder) {
		super.buildQuery(req, builder);
		// storageKey only
		req.setFetchSource(new String[]{"id, storageKey"}, null);
	}
	
	@Override
	protected QueryBuilder getQuery(DefaultQueryBuilder qb) {
		return QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), FilterBuilders.queryFilter(elasticQueryBuilder.build(new Not(qb.getWhere()))));
	}

}
