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

import static com.google.common.base.Preconditions.checkNotNull;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;

/**
 * @since 5.0
 */
public class ElasticsearchConceptFieldValueProvider implements ConceptFieldValueProvider {

	private final Client client;
	private final String indexName = "snomed";
	private final int requestTimeout = 10000;
	
	public ElasticsearchConceptFieldValueProvider(Client client) {
		this.client = checkNotNull(client, "client");
	}

	@Override
	public <V> V getFieldValue(String conceptId, String field) {
		FilterBuilder filterBuilder = new TermFilterBuilder("id", conceptId);
		ListenableActionFuture<SearchResponse> future = client.prepareSearch(indexName)
				.setTimeout(new TimeValue(requestTimeout))
				.setQuery(new FilteredQueryBuilder(QueryBuilders.matchAllQuery(), filterBuilder))
				.addField(field)
				.setSize(1)
				.setTypes("concept")
				.execute();
		SearchResponse searchResponse = future.actionGet(requestTimeout);
		SearchHits searchHits = searchResponse.getHits();
		if (searchHits.totalHits() == 1) {
			SearchHit searchHit = searchHits.hits()[0];
			SearchHitField searchHitField = searchHit.field(field);
			if (searchHitField == null) {
				throw new RuntimeException("Field not found: " + field);
			}
			return searchHitField.getValue();
		} else {
			throw new RuntimeException("Concept not found: " + conceptId);
		}
	}

}
