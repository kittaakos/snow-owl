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
package com.b2international.snowowl.core.store.index;

import java.util.Map;

import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.slf4j.Logger;

import com.b2international.commons.exceptions.FormattedRuntimeException;
import com.b2international.snowowl.core.log.Loggers;

/**
 * General purpose index service implementation on top of Elasticsearch library.
 * 
 * @since 5.0
 */
public class DefaultIndex implements Index {

	private static final Logger LOG = Loggers.REPOSITORY.log();
	private Client client;
	private String index;
	private DefaultIndexAdmin admin;

	public DefaultIndex(Client client, String index, Mappings mappings) {
		this.client = client;
		this.index = index;
		this.admin = new DefaultIndexAdmin(this.client.admin(), index, mappings);
	}
	
	@Override
	public Map<String, Object> get(String type, String id) {
		final GetResponse getResponse = this.client.prepareGet(index, type, id).setFetchSource(true).get();
		if (getResponse.isExists()) {
			return getResponse.getSource();
		} else {
			return null;
		}
	}
	
	@Override
	public void put(String type, String id, Map<String, Object> obj) {
		final IndexRequestBuilder req = this.client.prepareIndex(index, type, id).setSource(obj).setRefresh(true);
		// TODO indexing strategy, IMMEDIATE, BULK, BULK_SIZED
		// determines the refresh flag state as well, in IMMEDIATE the refresh should always be set
		req.get();
	}
	
	@Override
	public void putWithParent(String type, String parentKey, Map<String, Object> obj) {
		final IndexRequestBuilder req = this.client.prepareIndex(index, type).setParent(parentKey).setSource(obj).setRefresh(true);
		// TODO indexing strategy
		req.get();
	}
	
	@Override
	public boolean remove(String type, String id) {
		// TODO not found exception conversion
		final DeleteRequestBuilder req = this.client.prepareDelete(index, type, id).setRefresh(true);
		// TODO indexing strategy
		return req.get().isFound();
	}
	
	@Override
	public SearchHits search(String type, QueryBuilder query) {
		return search(type, query, 0, Integer.MAX_VALUE);
	}
	
	@Override
	public SearchHits search(String type, QueryBuilder query, int offset, int limit) {
		return query(type)
					.where(query)
					.page(offset, limit)
					.search();
	}
	
	@Override
	public final String name() {
		return index;
	}
	
	@Override
	public <T> MappingStrategy<T> mapping(Class<T> type) {
		return admin().mappings().getMapping(type);
	}

	@Override
	public IndexAdmin admin() {
		return admin;
	}
	
	@Override
	public IndexQueryBuilder query(String type) {
		return new IndexQueryBuilder(this, type);
	}
	
	@Override
	public SearchHits search(IndexQueryBuilder query) {
		final String type = query.type();
		final SearchRequestBuilder req = this.client.prepareSearch(index)
				.setTypes(type)
				.setQuery(query.toIndexQuery())
				.setFrom(query.offset())
				.setSize(query.limit());
		for (SortBuilder sort : query.sorts()) {
			req.addSort(sort);
		}
		LOG.info("Executing query: {}", req);
		final SearchResponse response = req.get();
		if (response.getSuccessfulShards() <= 0) {
			throw new FormattedRuntimeException("Failed to execute query '%s' on index '%s/%s': ", name(), type, response);
		}
		return response.getHits();
	}
	
}
