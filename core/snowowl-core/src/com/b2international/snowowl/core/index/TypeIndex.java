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
package com.b2international.snowowl.core.index;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.b2international.snowowl.core.index.mapping.DefaultMappingStrategy;
import com.b2international.snowowl.core.index.mapping.Mapping;
import com.b2international.snowowl.core.index.mapping.MappingStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Index of a particular type under a given {@link Index}.
 * 
 * @since 5.0
 */
public class TypeIndex<T> {

	private Index index;
	private MappingStrategy<T> mapping;
	private String type;
	private String idField;

	public TypeIndex(Index index, ObjectMapper mapper, Class<T> typeClass) {
		this.index = index;
		this.mapping = new DefaultMappingStrategy<>(mapper, typeClass);
		this.type = getType(typeClass);
		this.idField = getIdField(typeClass);
	}
	
	private static String getType(Class<?> type) {
		checkMapping(type);
		return type.getAnnotation(Mapping.class).type();
	}
	
	private static String getIdField(Class<?> type) {
		checkMapping(type);
		return type.getAnnotation(Mapping.class).id();
	}
	
	private static void checkMapping(Class<?> type) {
		if (!type.isAnnotationPresent(Mapping.class)) {
			throw new IllegalArgumentException(String.format("%s should define Mapping annotation to make it work in TypeIndex", type.getName()));
		}
	}

	// TODO create index mapping aka prepare mapping when creating index
	
	public void put(T obj) {
		final Map<String, Object> map = mapping().convert(obj);
		index.put(this.type, (String) map.get(idField), map);
	}
	
	public void remove(String id) {
		index.remove(type, id);
	}
	
	public T get(String id) {
		return mapping().convert(index.get(type, id));
	}
	
	public void clear() {
		index.clear(type);
	}
	
	public List<T> search(QueryBuilder query, int offset, int limit) {
		final SearchHits hits = index.search(type, query, offset, limit);
		final List<T> result = newArrayList();
		for (SearchHit hit : hits.getHits()) {
			result.add(mapping().convert(hit.getSource()));
		}
		return result;
	}
	
	protected Index index() {
		return index;
	}
	
	protected MappingStrategy<T> mapping() {
		return this.mapping;
	}
	
	public String getName() {
		return index.getName() + "/" + type;
	}

}
