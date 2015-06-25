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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

/**
 * @since 5.0
 */
public class Mappings {

	private ObjectMapper mapper;
	private Map<String, MappingStrategy<?>> mappings;
	
	public Mappings(ObjectMapper mapper) {
		this(mapper, Maps.<String, MappingStrategy<?>>newHashMap());
	}
	
	Mappings(Mappings mappings) {
		this(mappings.mapper, newHashMap(mappings.mappings));
	}
	
	Mappings(ObjectMapper mapper, Map<String, MappingStrategy<?>> mappings) {
		this.mapper = mapper;
		this.mappings = mappings;
	}
	
	private void addMapping(Class<?> type) {
		final MappingStrategy<?> strategy = new DefaultMappingStrategy<>(mapper, type);
		mappings.put(strategy.getType(), strategy);
	}
	
	public String getMapping(String type) {
		checkArgument(mappings.containsKey(type), "No mapping defined for %s", type);
		return mappings.get(type).getMapping();
	}
	
	public MappingStrategy<?> getMappingStrategy(String type) {
		checkArgument(mappings.containsKey(type), "No mapping strategy defined for %s", type);
		return mappings.get(type);
	}
	
	public Collection<MappingStrategy<?>> getMappings() {
		return mappings.values();
	}

	@SafeVarargs
	public static Mappings of(ObjectMapper mapper, Class<?>...types) {
		final Mappings mappings = new Mappings(mapper);
		for (Class<?> clazz : types) {
			mappings.addMapping(clazz);
		}
		return mappings;
	}

	@SafeVarargs
	public static Mappings of(Mappings mappings, Class<?>...types) {
		final Mappings newMappings = new Mappings(mappings);
		for (Class<?> clazz : types) {
			newMappings.addMapping(clazz);
		}
		return newMappings;
	}
	
}
