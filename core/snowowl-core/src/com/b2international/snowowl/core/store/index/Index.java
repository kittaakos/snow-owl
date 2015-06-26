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

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;

/**
 * Generic interface for an elasticsearch index.
 * 
 * @since 5.0
 */
public interface Index extends MappingProvider {

	/**
	 * Fetch an object by type and key from the index.
	 * 
	 * @param type
	 *            - the object's type to retrieve
	 * @param key
	 *            - the unique identifier of the object
	 * @return a {@link Map} of String, Object pairs representing the object
	 */
	Map<String, Object> get(String type, String key);

	/**
	 * Store/Put an object represented by the given {@link Map} of String, Object pairs in this index under the given type with a generated ID and
	 * parent key.
	 * 
	 * @param type
	 *            - the object's type
	 * @param parentKey
	 *            - the document id of the parent document
	 * @param obj
	 *            - the {@link Map} representation of the object
	 */
	void putWithParent(String type, String parentKey, Map<String, Object> obj);

	/**
	 * Store/Put an object represented by the given {@link Map} of String, Object pairs in this index under the given type, identified with the given
	 * key.
	 * 
	 * @param type
	 *            - the object's type
	 * @param key
	 *            - the unique identifier of the object
	 * @param obj
	 *            - the {@link Map} representation of the object
	 */
	void put(String type, String key, Map<String, Object> obj);

	/**
	 * Remove a document from the index from the given types with the given id.
	 * 
	 * @param type
	 *            - the object's type
	 * @param id
	 *            - the unique identifier of the object
	 */
	void remove(String type, String id);

	/**
	 * Execute the given query among all stored documents with the given type.
	 * 
	 * @param type
	 *            - the type to restrict the execution of the query
	 * @param query
	 *            - the query to execute and return hits for
	 * @return - the search hits
	 */
	SearchHits search(String type, QueryBuilder query);

	/**
	 * Execute the given query among all stored documents with the given type. The size of the search hits will be restricted to the given limit
	 * starting from the given offset.
	 * 
	 * @param type
	 *            - the type to restrict execution of the query
	 * @param query
	 *            - the query to execute and return hits for
	 * @param offset
	 *            - the offset to use
	 * @param limit
	 *            - the result set limit to use
	 * @return - the search hits
	 */
	SearchHits search(String type, QueryBuilder query, int offset, int limit);

	/**
	 * Returns the name of the index.
	 * 
	 * @return
	 */
	String name();

	/**
	 * Returns the administrator interface for this index.
	 * 
	 * @return
	 */
	IndexAdmin admin();

	/**
	 * Returns a query builder scoped to the given type.
	 * 
	 * @param type
	 *            - the type to restrict the execution of the query
	 * @return
	 */
	IndexQueryBuilder query(String type);

	/**
	 * Execute the given query among all stored documents with the given type.
	 * 
	 * @param type
	 *            - the type to restrict execution of the query
	 * @param indexQueryBuilder
	 *            - the builder defining all aspects of your query
	 * @return - the search hits
	 */
	SearchHits search(IndexQueryBuilder indexQueryBuilder);

}
