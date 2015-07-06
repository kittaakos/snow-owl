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
package com.b2international.snowowl.snomed.core.store.query;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.support.QuerySourceBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import com.b2international.snowowl.snomed.core.store.index.SnomedIndexConstants;
import com.b2international.snowowl.snomed.core.store.query.Type.ComponentType;

/**
 * @since 5.0
 */
public class ElasticsearchCountRequestBuilder {

	protected final ElasticsearchQueryBuilder queryBuilder;

	public ElasticsearchCountRequestBuilder(ElasticsearchQueryBuilder queryBuilder) {
		this.queryBuilder = checkNotNull(queryBuilder, "queryBuilder");
	}

	public CountRequest build(Query query) {
		checkNotNull(query, "query");
		Select select = query.getSelect();
		checkArgument(select instanceof Select.Count, "This builder only handles count queries.");
		Type selectType = query.getType();
		Expression where = query.getWhere();
		
		QueryBuilder elasticsearchQuery = queryBuilder.build(where);
		CountRequest countRequest = new CountRequest().indices(SnomedIndexConstants.INDEX_NAME).source(new QuerySourceBuilder().setQuery(elasticsearchQuery));
		if (ComponentType.CONCEPT.equals(selectType)) {
			countRequest.types(SnomedIndexConstants.CONCEPT_OBJECT_TYPE);
		} else {
			// TODO
		}
		
		return countRequest;
	}

}
