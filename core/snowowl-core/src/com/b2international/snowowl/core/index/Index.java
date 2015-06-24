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

import java.util.Map;

import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;

/**
 * General purpose index service implementation on top of Elasticsearch library.
 * 
 * @since 5.0
 */
public final class Index {

	private Client client;
	private String index;

	public Index(Client client, String index) {
		this.client = client;
		this.index = index;
	}
	
	public Map<String, Object> get(String type, String id) {
		// TODO not found exception conversion
		return this.client.prepareGet(index, type, id).get().getSource();
	}
	
	public void put(String type, String id, Map<String, Object> obj) {
		final IndexRequestBuilder req = this.client.prepareIndex(index, type, id).setSource(obj);
		// TODO indexing strategy, IMMEDIATE, BULK, BULK_SIZED
		// determines the refresh flag state as well, in IMMEDIATE the refresh should always be set
		req.get();
	}
	
	public void remove(String type, String id) {
		// TODO not found exception conversion
		final DeleteRequestBuilder req = this.client.prepareDelete(index, type, id);
		// TODO indexing strategy
		req.get();
	}
	
	public SearchHits search(String type, QueryBuilder query) {
		return search(type, query, 0, Integer.MAX_VALUE);
	}
	
	public SearchHits search(String type, QueryBuilder query, int offset, int limit) {
		return this.client.prepareSearch(index).setTypes(type).setQuery(query).setFrom(offset).setSize(limit).get().getHits();
	}
	
	public String getName() {
		return index;
	}

	public void clear(String type) {
		this.client.admin().indices().prepareDeleteMapping(index).setType(type).get();
	}
	
}
