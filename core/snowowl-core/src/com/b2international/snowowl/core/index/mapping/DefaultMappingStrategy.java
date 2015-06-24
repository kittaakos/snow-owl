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
package com.b2international.snowowl.core.index.mapping;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 5.0
 */
public class DefaultMappingStrategy<T> implements MappingStrategy<T> {

	private ObjectMapper mapper;
	private Class<T> type;

	public DefaultMappingStrategy(ObjectMapper mapper, Class<T> type) {
		this.mapper = checkNotNull(mapper, "mapper");
		this.type = checkNotNull(type, "type");
	}
	
	@Override
	public T convert(Map<String, Object> map) {
		return mapper.convertValue(map, type);
	}
	
	@Override
	public Map<String, Object> convert(T t) {
		return mapper.convertValue(t, Map.class);
	}

}
