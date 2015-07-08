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
package com.b2international.snowowl.core.store.mem;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import com.b2international.snowowl.core.store.BaseStore;
import com.b2international.snowowl.core.store.query.Expression;
import com.b2international.snowowl.core.store.query.PrefixPredicate;
import com.b2international.snowowl.core.store.query.Query;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.store.query.Query.QueryBuilder;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.MapMaker;

/**
 * @since 4.1
 */
public class MemStore<T> extends BaseStore<T> {

	private final ConcurrentMap<String, T> values = new MapMaker().makeMap();
	
	public MemStore(Class<T> type) {
		super(type);
	}
	
	@Override
	public String getName() {
		return String.format("Mem[%s]", getTypeClass().getSimpleName());
	}

	@Override
	protected void doPut(String id, T value) {
		values.put(id, value);
	}
	
	@Override
	public T get(String key) {
		return values.get(key);
	}

	@Override
	public T remove(String key) {
		return values.remove(key);
	}

	@Override
	public Collection<T> values() {
		return values.values();
	}
	
	@Override
	public void clear() {
		values.clear();
	}
	
	@Override
	public QueryBuilder query() {
		return Query.builder();
	}

	@Override
	public Iterable<T> search(AfterWhereBuilder queryBuilder) {
		Query query = queryBuilder.build();
		Expression expression = query.getWhere();
		if (expression instanceof PrefixPredicate) {
			PrefixPredicate prefixPredicate = (PrefixPredicate) expression;
			final String argument = prefixPredicate.getArgument();
			return FluentIterable.from(values.entrySet()).skip(query.getOffset()).limit(query.getLimit()).filter(new Predicate<Entry<String, T>>() {
				@Override
				public boolean apply(Entry<String, T> input) {
					return input.getKey().startsWith(argument);
				}
			}).transform(new Function<Entry<String, T>, T>() {
				@Override
				public T apply(Entry<String, T> input) {
					return input.getValue();
				}
			});
		} else {
			// TODO: implement other predicates as needed
			return Collections.emptySet();
		}
	}
	
}
