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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.b2international.snowowl.core.index.Index;
import com.b2international.snowowl.core.index.TypeIndex;
import com.b2international.snowowl.core.store.query.Clause;
import com.b2international.snowowl.core.store.query.EqualsWhere;
import com.b2international.snowowl.core.store.query.PrefixWhere;
import com.b2international.snowowl.core.store.query.Where;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 4.1
 */
public class IndexStore<T> implements Store<T> {

	private TypeIndex<T> index;

	/**
	 * Creates a new {@link Index} based {@link Store}.
	 * 
	 * @param index
	 *            - the index to use
	 * @param type
	 *            - the value's type
	 */
	public IndexStore(Index index, Class<T> type) {
		this(index, new ObjectMapper(), type);
	}
	
	public IndexStore(Index index, ObjectMapper objectMapper, Class<T> type) {
		this.index = new TypeIndex<>(index, objectMapper, type);
	}
	
	@Override
	public void put(String key, T value) {
		index.put(value);
	}

	@Override
	public T get(String key) {
		return index.get(key);
	}

	@Override
	public T remove(String key) {
		final T t = get(key);
		index.remove(key);
		return t;
	}

	@Override
	public boolean replace(String key, T oldValue, T newValue) {
		checkNotNull(oldValue, "oldValue");
		checkNotNull(newValue, "newValue");
		if (oldValue.equals(newValue) || !oldValue.equals(get(key))) {
			return false;
		} else {
			put(key, newValue);
			return true;
		}
	}

	@Override
	public Collection<T> values() {
		return searchIndex(QueryBuilders.matchAllQuery());
	}
	
	@Override
	public Collection<T> search(com.b2international.snowowl.core.store.query.Query query) {
		return search(query, 0, Integer.MAX_VALUE);
	}
	
	@Override
	public Collection<T> search(com.b2international.snowowl.core.store.query.Query query, int offset, int limit) {
		return searchIndex(convert(query), offset, limit);
	}

	@Override
	public void clear() {
		index.clear();
	}
	
	private List<T> searchIndex(final QueryBuilder query) {
		return searchIndex(query, Integer.MAX_VALUE);
	}

	private List<T> searchIndex(final QueryBuilder query, final int limit) {
		return searchIndex(query, 0, limit);
	}
	
	private List<T> searchIndex(final QueryBuilder query, final int offset, final int limit) {
		return this.index.search(query, offset, limit);
	}
	
	private QueryBuilder convert(com.b2international.snowowl.core.store.query.Query query) {
		final BoolQueryBuilder result = QueryBuilders.boolQuery();
		for (Clause clause : query.clauses()) {
			if (clause instanceof Where) {
				final String property = ((Where) clause).property();
				final String value = ((Where) clause).value();
				if (clause instanceof EqualsWhere) {
					result.must(QueryBuilders.termQuery(property, value));
				} else if (clause instanceof PrefixWhere) {
					result.must(QueryBuilders.prefixQuery(property, value));
				}
			}
		}
		return result;
	}
	
}
