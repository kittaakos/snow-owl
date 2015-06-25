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
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.b2international.snowowl.core.store.BaseStore;
import com.b2international.snowowl.core.store.query.Clause;
import com.b2international.snowowl.core.store.query.EqualsWhere;
import com.b2international.snowowl.core.store.query.PrefixWhere;
import com.b2international.snowowl.core.store.query.Where;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 4.1
 */
public class IndexStore<T> extends BaseStore<T> {

	private Index index;
	private MappingStrategy<T> mapping;

	public IndexStore(Index index, ObjectMapper mapper, Class<T> type) {
		super(type);
		this.index = checkNotNull(index, "index");
		this.mapping = new DefaultMappingStrategy<>(mapper, type);
	}
	
	@Override
	protected void doPut(String key, T value) {
		this.index.put(getType(), key, mapping.convert(value));
	}

	private String getType() {
		return this.mapping.getType();
	}

	@Override
	public T get(String key) {
		return mapping.convert(index.get(getType(), key));
	}

	@Override
	public T remove(String key) {
		final T t = get(key);
		index.remove(getType(), key);
		return t;
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
		index.admin().clear(getType());
	}
	
	@Override
	public String getName() {
		return String.format("Index[%s/%s]", index.name(), getType());
	}
	
	private List<T> searchIndex(final QueryBuilder query) {
		return searchIndex(query, Integer.MAX_VALUE);
	}

	private List<T> searchIndex(final QueryBuilder query, final int limit) {
		return searchIndex(query, 0, limit);
	}
	
	private List<T> searchIndex(final QueryBuilder query, final int offset, final int limit) {
		final SearchHits hits = index.search(getType(), query, offset, limit);
		final List<T> result = newArrayList();
		for (SearchHit hit : hits.getHits()) {
			result.add(mapping.convert(hit.getSource()));
		}
		return result;
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
