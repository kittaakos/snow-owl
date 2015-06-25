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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;

import com.b2international.snowowl.core.store.index.MappingStrategy;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * @since 5.0
 */
public class Mappings {

	private Table<String, String, String> indexTypeToMappingTable = HashBasedTable.create();
	private Table<String, String, MappingStrategy<?>> indexTypeToMappingStrategyTable = HashBasedTable.create();
	
	public void addMapping(String index, String type, String mapping) {
		indexTypeToMappingTable.put(index, type, mapping);
	}
	
	public void addMappingStrategy(String index, String type, MappingStrategy<?> strategy) {
		indexTypeToMappingStrategyTable.put(index, type, strategy);
	}
	
	public String getMapping(String index, String type) {
		checkArgument(indexTypeToMappingTable.contains(index, type), "No mapping defined for %s/%s", index, type);
		return indexTypeToMappingTable.get(index, type); 
	}
	
	public MappingStrategy<?> getMappingStrategy(String index, String type) {
		checkArgument(indexTypeToMappingStrategyTable.contains(index, type), "No mapping strategy defined for %s/%s", index, type);
		return indexTypeToMappingStrategyTable.get(index, type);
	}

	public Map<String, String> getMappings(String index) {
		return indexTypeToMappingTable.row(index);
	}
	
}
