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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;

import com.b2international.commons.ClassUtils;
import com.b2international.snowowl.core.store.query.Query;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.store.query.Query.QueryBuilder;
import com.b2international.snowowl.core.store.query.Query.SearchContextBuilder;
import com.b2international.snowowl.core.store.query.req.DefaultSearchExecutor;
import com.b2international.snowowl.core.store.query.req.SearchExecutor;

/**
 * General purpose index service implementation on top of Elasticsearch library.
 * 
 * @since 5.0
 */
public class DefaultIndex implements Index {

	private Client client;
	private String index;
	private DefaultIndexAdmin admin;

	public DefaultIndex(Client client, String index, Mappings mappings) {
		this(client, index, mappings, null);
	}
	
	public DefaultIndex(Client client, String index, Mappings mappings, Map<String, Object> settings) {
		this.client = checkNotNull(client, "client");
		this.index = index;
		this.admin = new DefaultIndexAdmin(this.client.admin(), index, mappings, settings);
	}
	
	@Override
	public final <T> T get(Class<T> type, String key) {
		final MappingStrategy<T> mapping = mapping(type);
		return mapping.convert(get(mapping.getType(), key));
	}
	
	@Override
	public Map<String, Object> get(String type, String key) {
		final GetResponse getResponse = this.client.prepareGet(index, type, key).setFetchSource(true).get();
		if (getResponse.isExists()) {
			return getResponse.getSource();
		} else {
			return null;
		}
	}
	
	@Override
	public final <T> void put(String key, T object) {
		put(getType(object.getClass()), key, object);
	}
	
	@Override
	public void put(String type, String key, Object object) {
		final Map<String, Object> map = toMap(object);
		final IndexRequestBuilder req = this.client.prepareIndex(index, type, key).setSource(map);
		doIndex(req);		
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> toMap(Object object) {
		return admin().mappings().mapper().convertValue(object, Map.class);
	}

	@Override
	public final <T> void putWithParent(String parentKey, T object) {
		putWithParent(getType(object.getClass()), parentKey, object);
	}
	
	@Override
	public void putWithParent(String type, String parentKey, Object object) {
		final Map<String, Object> map = toMap(object);
		final IndexRequestBuilder req = this.client.prepareIndex(index, type).setParent(parentKey).setSource(map);
		doIndex(req);
	}

	@Override
	public final <T> boolean remove(Class<T> type, String key) {
		return remove(getType(type), key);
	}
	
	@Override
	public boolean remove(String type, String key) {
		final DeleteRequestBuilder req = this.client.prepareDelete(index, type, key);
		return doDelete(req);
	}

	private <T> String getType(Class<T> type) {
		return mapping(type).getType();
	}

	@Override
	public final String name() {
		return index;
	}
	
	@Override
	public final <T> MappingStrategy<T> mapping(Class<T> type) {
		return admin().mappings().getMapping(type);
	}

	@Override
	public final IndexAdmin admin() {
		return admin;
	}
	
	@Override
	public final QueryBuilder query() {
		return Query.builder(this);
	}
	
	@Override
	public final <T> Iterable<T> search(AfterWhereBuilder query, Class<T> type) {
		final SearchContextBuilder context = ClassUtils.checkAndCast(query, SearchContextBuilder.class);
		final MappingStrategy<T> mapping = mapping(type);
		final String typeName = mapping.getType();
		final SearchRequestBuilder req = this.client.prepareSearch(index).setTypes(typeName);
		SearchExecutor executor = context.executor();
		if (executor == null) {
			executor = new DefaultSearchExecutor();
		}
		return executor.execute(req, query, admin().mappings().mapper(), type);
	}
	
	protected final Client client() {
		return client;
	}
	
	protected void doIndex(IndexRequestBuilder req) {
		req.setRefresh(true).get();
	}
	
	protected boolean doDelete(DeleteRequestBuilder req) {
		return req.setRefresh(true).get().isFound();
	}
	
	protected void doUpdate(UpdateRequestBuilder req) {
		req.setRefresh(true).get();
	}
	
}
