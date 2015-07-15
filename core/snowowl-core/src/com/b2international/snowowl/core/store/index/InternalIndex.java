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

import java.util.Iterator;
import java.util.Map;

import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;

/**
 * TODO move to internal package
 * 
 * @since 5.0
 */
public interface InternalIndex extends Index {

	Client client();

	/**
	 * @param type
	 *            - the type of the document
	 * @param key
	 *            - the unique identifier of the document, may be <code>null</code>
	 * @param object
	 *            - the document object
	 * @return
	 */
	IndexRequestBuilder prepareIndex(String type, String key, Object object);

	IndexRequestBuilder prepareIndexWithParent(String type, String parentKey, String key, Object object);

	DeleteRequestBuilder prepareDelete(String type, String key);

	UpdateRequestBuilder prepareUpdateByScript(String type, String key, String script, Map<String, Object> params);
	
	<T> String getType(Class<T> type);

	Iterator<SearchHit> scan(QueryBuilder queryBuilder);


}
