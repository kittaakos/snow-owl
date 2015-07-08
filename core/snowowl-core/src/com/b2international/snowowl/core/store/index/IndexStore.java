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

import com.b2international.snowowl.core.store.BaseStore;
import com.b2international.snowowl.core.store.query.MatchAll;
import com.b2international.snowowl.core.store.query.Query;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.store.query.Query.QueryBuilder;
import com.b2international.snowowl.core.store.query.Select;

/**
 * @since 4.1
 */
public class IndexStore<T> extends BaseStore<T> {

	private Index index;

	public IndexStore(Index index, Class<T> type) {
		super(type);
		this.index = checkNotNull(index, "index");
	}
	
	@Override
	protected void doPut(String key, T value) {
		this.index.put(key, value);
	}

	@Override
	public T get(String key) {
		return index.get(getTypeClass(), key);
	}

	@Override
	public T remove(String key) {
		final T t = get(key);
		index.remove(getTypeClass(), key);
		return t;
	}

	@Override
	public Collection<T> values() {
		return newArrayList(search(query().select(Select.all()).where(new MatchAll())));
	}
	
	@Override
	public void clear() {
		index.admin().clear(getTypeClass());
	}
	
	@Override
	public String getName() {
		return String.format("IndexStore[%s/%s]", index.name(), index.mapping(getTypeClass()).getType());
	}
	
	@Override
	public QueryBuilder query() {
		return Query.builder();
	}

	@Override
	public Iterable<T> search(AfterWhereBuilder query) {
		return index.search(query, getTypeClass());
	}
	
}
