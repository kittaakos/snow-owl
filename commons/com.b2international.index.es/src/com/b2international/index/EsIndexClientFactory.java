/*
 * Copyright 2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.index;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.elasticsearch.node.Node;

import com.b2international.index.admin.EsIndexAdmin;
import com.b2international.index.mapping.Mappings;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 5.10
 */
public final class EsIndexClientFactory implements IndexClientFactory {

	@Override
	public IndexClient createClient(String name, ObjectMapper mapper, Mappings mappings, Map<String, Object> settings) {
		final boolean persistent = settings.containsKey(DATA_DIRECTORY);
		final Object dir = persistent ? settings.get(DATA_DIRECTORY) : new File("target");
		final Object configDir = settings.containsKey(CONFIG_DIRECTORY) ? settings.get(CONFIG_DIRECTORY) : Paths.get("target");
		final File dataDirectory = dir instanceof File ? (File) dir : new File((String) dir);
		final Path configDirectory = configDir instanceof Path ? (Path) configDir : Paths.get((String) configDir);
		final Node node = EsNode.getInstance(configDirectory, dataDirectory, persistent);
		return new EsIndexClient(new EsIndexAdmin(node.client(), name, mappings, settings), mapper);
	}

	
	
}
