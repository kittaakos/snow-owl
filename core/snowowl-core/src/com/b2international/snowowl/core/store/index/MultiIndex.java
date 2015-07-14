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

import java.util.LinkedList;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.client.Client;

import com.b2international.snowowl.core.store.query.Query.SearchContextBuilder;
import com.b2international.snowowl.core.store.query.req.AsyncMultiIndexSearchExecutor;
import com.b2international.snowowl.core.store.query.req.SearchExecutor;

/**
 * @since 5.0
 */
public class MultiIndex extends DefaultIndex {

	private LinkedList<String> indexes;

	public MultiIndex(Client client, Mappings mappings, Map<String, Object> settings, final LinkedList<String> indexes) {
		super(client, indexes.get(0), mappings, settings);
		this.indexes = indexes;
	}
	
	@Override
	public Map<String, Object> get(String type, String key) {
		final MultiGetRequestBuilder multiGet = this.client().prepareMultiGet();
		for (String index : indexes) {
			multiGet.add(index, type, key);
		}
		multiGet.add(super.name(), type, key);
		final MultiGetResponse response = multiGet.get();
		for (MultiGetItemResponse item : response) {
			final GetResponse getResponse = item.getResponse();
			if (getResponse.isExists()) {
				return getResponse.getSource();
			}
		}
		return null;
	}
	
	@Override
	protected SearchExecutor getExecutor(SearchContextBuilder context) {
		if (context.executor() == null) {
			return new AsyncMultiIndexSearchExecutor(client(), indexes, admin().mappings().mapper());
		}
		return super.getExecutor(context);
	}
	
}
