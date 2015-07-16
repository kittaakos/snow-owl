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

import java.util.Iterator;
import java.util.Map;

import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.SearchHit;

import com.b2international.commons.ClassUtils;
import com.b2international.snowowl.core.store.query.Query;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.store.query.Query.QueryBuilder;
import com.b2international.snowowl.core.store.query.Query.SearchContextBuilder;
import com.b2international.snowowl.core.store.query.req.DefaultSearchExecutor;
import com.b2international.snowowl.core.store.query.req.DefaultSearchResponseProcessor;
import com.b2international.snowowl.core.store.query.req.ScanningSearchHitIterator;
import com.b2international.snowowl.core.store.query.req.SearchExecutor;

/**
 * General purpose index service implementation on top of Elasticsearch library.
 * 
 * @since 5.0
 */
public class DefaultIndex implements InternalIndex {

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
	public <T> T get(Class<T> type, String key) {
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
	public <T> void put(T object) {
		prepareIndex(getType(object.getClass()), null, object).setRefresh(true).get();
	}
	
	@Override
	public <T> void put(String key, T object) {
		put(getType(object.getClass()), key, object);
	}
	
	@Override
	public void put(String type, String key, Object object) {
		prepareIndex(type, key, object).setRefresh(true).get();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> toMap(Object object) {
		return admin().mappings().mapper().convertValue(object, Map.class);
	}

	@Override
	public <T> void putWithParent(String parentKey, String key, T object) {
		putWithParent(getType(object.getClass()), parentKey, key, object);
	}
	
	@Override
	public void putWithParent(String type, String parentKey, String key, Object object) {
		prepareIndexWithParent(type, parentKey, key, object).setRefresh(true).get();
	}

	@Override
	public IndexRequestBuilder prepareIndexWithParent(String type, String parentKey, String key, Object object) {
		final Map<String, Object> map = toMap(object);
		return this.client.prepareIndex(index, type, key).setParent(parentKey).setSource(map);
	}

	@Override
	public <T> void updateByScript(Class<T> type, String key, String script, Map<String, Object> params) {
		updateByScript(getType(type), key, script, params);
	}
	
	@Override
	public void updateByScript(String type, String key, String script, Map<String, Object> params) {
		prepareUpdateByScript(type, key, script, params).setRefresh(true).get();
	}

	@Override
	public UpdateRequestBuilder prepareUpdateByScript(String type, String key, String script, Map<String, Object> params) {
		return this.client.prepareUpdate(index, type, key).setScriptParams(params).setScript(script, ScriptType.INLINE);
	}
	
	@Override
	public <T> boolean remove(Class<T> type, String key) {
		return remove(getType(type), key);
	}
	
	@Override
	public boolean remove(String type, String key) {
		return prepareDelete(type, key).setRefresh(true).get().isFound();
	}
	
	@Override
	public IndexRequestBuilder prepareIndex(String type, String key, Object object) {
		final Map<String, Object> map = toMap(object);
		if (key != null) {
			return this.client.prepareIndex(index, type, key).setSource(map);
		} else {
			return this.client.prepareIndex(index, type).setSource(map);
		}
	}

	@Override
	public DeleteRequestBuilder prepareDelete(String type, String key) {
		return this.client.prepareDelete(index, type, key);
	}

	@Override
	public <T> String getType(Class<T> type) {
		return mapping(type).getType();
	}

	@Override
	public String name() {
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
	public QueryBuilder query() {
		return Query.builder();
	}
	
	@Override
	public <T> Iterable<T> search(AfterWhereBuilder query, Class<T> type) {
		final SearchContextBuilder context = ClassUtils.checkAndCast(query, SearchContextBuilder.class);
		final MappingStrategy<T> mapping = mapping(type);
		final String typeName = mapping.getType();
		final SearchRequestBuilder req = this.client.prepareSearch(index).setTypes(typeName);
		final SearchExecutor executor = getExecutor(context);
		return executor.execute(req, query, type);
	}

	protected SearchExecutor getExecutor(final SearchContextBuilder context) {
		SearchExecutor executor = context.executor();
		if (executor == null) {
			executor = new DefaultSearchExecutor(new DefaultSearchResponseProcessor(admin().mappings().mapper()));
		}
		return executor;
	}
	
	@Override
	public Iterator<SearchHit> scan(org.elasticsearch.index.query.QueryBuilder queryBuilder) {
		return new ScanningSearchHitIterator(client, queryBuilder, index, 15000);
	}
	
	@Override
	public Client client() {
		return client;
	}
	
}
