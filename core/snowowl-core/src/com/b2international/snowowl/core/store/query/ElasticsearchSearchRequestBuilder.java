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

import java.util.Deque;
import java.util.List;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.b2international.snowowl.core.store.query.Select.Multiple;
import com.b2international.snowowl.core.store.query.Select.NestedObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

/**
 * @since 5.0
 */
public class ElasticsearchSearchRequestBuilder {

	private final ElasticsearchQueryBuilder queryBuilder;
	private final String indexName;

	public ElasticsearchSearchRequestBuilder(String indexName, ElasticsearchQueryBuilder queryBuilder) {
		this.indexName = checkNotNull(indexName, "indexName");
		this.queryBuilder = checkNotNull(queryBuilder, "queryBuilder");
	}

	public SearchRequest build(Query query) {
		checkNotNull(query, "query");
		Select select = query.getSelect();
		checkArgument(!(select instanceof Select.Count), "Count queries are not handled by this builder.");
		Expression where = query.getWhere();
		QueryBuilder elasticsearchQuery = queryBuilder.build(where);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(elasticsearchQuery);
		SearchRequest searchRequest = new SearchRequest().source(searchSourceBuilder).indices(indexName);
		
		List<String> sourceIncludePatterns = Lists.newArrayList();
		List<String> fields = Lists.newArrayList();
		
		Deque<Select> deque = Queues.newLinkedBlockingDeque();
		deque.add(select);
		
		while (!deque.isEmpty()) {
			Select currentSelect = deque.poll();
			if (currentSelect instanceof Select.Fields) {
				Select.Fields selectFields = (Select.Fields) currentSelect;
				fields.addAll(selectFields.getFields());
			} else if (currentSelect instanceof Select.All) {
				sourceIncludePatterns.add("*");
			} else if (currentSelect instanceof NestedObject) {
				String nestedPath = ((NestedObject) currentSelect).getPath();
				sourceIncludePatterns.add(nestedPath);
			} else if (currentSelect instanceof Multiple) {
				Multiple multiple = (Multiple) currentSelect;
				for (Select childSelect : multiple.getItems()) {
					deque.push(childSelect);
				}
			} else {
				throw new IllegalArgumentException("Unexpected select clause: " + currentSelect);
			}
		}
		
		if (!fields.isEmpty()) {
			searchSourceBuilder.fields(fields);
		}
		if (!sourceIncludePatterns.isEmpty()) {
			searchSourceBuilder.fetchSource(sourceIncludePatterns.toArray(new String[0]), null);
		}
		
		return searchRequest;
	}

}
