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

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.AdminClient;

import com.b2international.commons.exceptions.FormattedRuntimeException;

/**
 * @since 5.0
 */
public class DefaultIndexAdmin implements IndexAdmin {

	private AdminClient admin;
	private String index;

	public DefaultIndexAdmin(AdminClient admin, String index) {
		this.admin = admin;
		this.index = index;
	}
	
	@Override
	public boolean exists() {
		return this.admin.indices().prepareExists(index).get().isExists();
	}

	@Override
	public void create(Mappings mappings) {
		if (!exists()) {
			final CreateIndexRequestBuilder create = this.admin.indices().prepareCreate(index);
			for (MappingStrategy<?> mapping : mappings.getMappings()) {
				create.addMapping(mapping.getType(), mapping.getMapping());
			}
			final CreateIndexResponse response = create.get();
			if (response.isAcknowledged()) {
				System.out.println(String.format("Created index '%s'", index));
			} else {
				throw new FormattedRuntimeException("Failed to create index '%s'", index);
			}
		}
	}

	@Override
	public void delete() {
		if (exists()) {
			this.admin.indices().prepareDelete(index).get();
		}
	}

	@Override
	public void clear(String type) {
		this.admin.indices().prepareDeleteMapping(index).setType(type).get();
	}

}
