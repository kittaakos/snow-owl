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

import com.b2international.snowowl.core.index.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 5.0
 */
public class ComponentMappingStrategy<T extends Component> {

	private ObjectMapper mapper;
	private Class<T> type;

	public ComponentMappingStrategy(ObjectMapper mapper, Class<T> type) {
		this.mapper = checkNotNull(mapper, "mapper");
		this.type = checkNotNull(type, "type");
	}
	
	public final T fromJSON(Map<String, Object> json) {
		return mapper.convertValue(json, type);
	}

}
