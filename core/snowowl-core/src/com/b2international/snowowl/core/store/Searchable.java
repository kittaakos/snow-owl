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
package com.b2international.snowowl.core.store;

import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.store.query.Query.QueryBuilder;

/**
 * @since 5.0
 */
public interface Searchable {

	/**
	 * Returns a query builder to build and run a query against this searchable object.
	 * 
	 * @return a {@link QueryBuilder} instance
	 */
	QueryBuilder query();

	/**
	 * Execute the given query among all stored items.
	 * 
	 * @param query
	 *            - the query builder
	 * @param type
	 *            - the type to restrict the execution of the query
	 * @return - an iterable of matching values
	 */
	<T> Iterable<T> search(AfterWhereBuilder query, Class<T> type);

}
