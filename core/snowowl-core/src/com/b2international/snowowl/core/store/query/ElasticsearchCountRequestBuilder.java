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
package com.b2international.snowowl.core.store.query;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.support.QuerySourceBuilder;
import org.elasticsearch.index.query.QueryBuilder;

/**
 * @since 5.0
 */
public class ElasticsearchCountRequestBuilder {

	private final ElasticsearchQueryBuilder queryBuilder;
	private final String indexName;

	public ElasticsearchCountRequestBuilder(String indexName, ElasticsearchQueryBuilder queryBuilder) {
		this.indexName = checkNotNull(indexName, "indexName");
		this.queryBuilder = checkNotNull(queryBuilder, "queryBuilder");
	}

	public CountRequest build(Query query) {
		checkNotNull(query, "query");
		Select select = query.getSelect();
		checkArgument(select instanceof Select.Count, "This builder only handles count queries.");
		Expression where = query.getWhere();
		
		QueryBuilder elasticsearchQuery = queryBuilder.build(where);
		CountRequest countRequest = new CountRequest().indices(indexName).source(new QuerySourceBuilder().setQuery(elasticsearchQuery));
		
		return countRequest;
	}

}
