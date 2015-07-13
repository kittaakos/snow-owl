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
package com.b2international.snowowl.core.store.query.req;

import java.util.LinkedList;

import org.elasticsearch.action.search.SearchRequestBuilder;

import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;

/**
 * @since 5.0
 */
public class MultiIndexSearchExecutor extends DefaultSearchExecutor {

	private LinkedList<String> indexes;

	public MultiIndexSearchExecutor(SearchResponseProcessor processor, LinkedList<String> indexes) {
		super(processor);
		this.indexes = indexes;
	}
	
	@Override
	public <T> Iterable<T> execute(SearchRequestBuilder req, AfterWhereBuilder builder, Class<T> resultType) {
		// search explicitly on all branch indexes
		req.setIndices(indexes.toArray(new String[indexes.size()]));
		return super.execute(req, builder, resultType);
	}
	
}
