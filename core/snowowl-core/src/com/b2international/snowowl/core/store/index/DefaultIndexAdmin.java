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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.cluster.tasks.PendingClusterTasksRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.cluster.service.PendingClusterTask;
import org.elasticsearch.common.base.Strings;
import org.slf4j.Logger;

import com.b2international.commons.exceptions.FormattedRuntimeException;
import com.b2international.snowowl.core.log.Loggers;

/**
 * @since 5.0
 */
public class DefaultIndexAdmin implements IndexAdmin {

	private static Logger LOG = Loggers.REPOSITORY.log();
	private AdminClient admin;
	private String index;
	private Mappings mappings;
	private Map<String, Object> settings;

	protected DefaultIndexAdmin(AdminClient admin, String index, Mappings mappings) {
		this(admin, index, mappings, null);
	}
	
	protected DefaultIndexAdmin(AdminClient admin, String index, Mappings mappings, Map<String, Object> settings) {
		this.admin = checkNotNull(admin, "admin");
		this.index = checkNotNull(index, "index");
		this.mappings = checkNotNull(mappings, "mappings");
		this.settings = settings == null ? Collections.<String, Object>emptyMap() : settings;
	}
	
	@Override
	public boolean exists() {
		return this.admin.indices().prepareExists(index).get().isExists();
	}

	@Override
	public void create() {
		if (!exists()) {
			final CreateIndexRequestBuilder create = this.admin.indices().prepareCreate(index);
			create.setSettings(settings);
			for (MappingStrategy<?> mapping : mappings.getMappings()) {
				final String mappingJson = mapping.getMapping();
				if (!Strings.isNullOrEmpty(mappingJson)) {
					create.addMapping(mapping.getType(), mapping.getMapping());
				}
			}
			final CreateIndexResponse response = create.get();
			if (response.isAcknowledged()) {
				LOG.info("Created index '{}'", index);
			} else {
				throw new FormattedRuntimeException("Failed to create index '%s'", index);
			}
			awaitEndOfInitialization();
		}
	}
	
	@Override
	public void delete() {
		if (exists()) {
			final DeleteIndexRequestBuilder req = this.admin.indices().prepareDelete(index);
			final DeleteIndexResponse response = req.get();
			if (response.isAcknowledged()) {
				LOG.info("Deleted index '{}'", index);
			} else {
				throw new FormattedRuntimeException("Failed to delete index '%s'", index);
			}
		}
	}

	@Override
	public <T> void clear(Class<T> type) {
		final String typeName = mappings().getMapping(type).getType();
		this.admin.indices().prepareDeleteMapping(index).setType(typeName).get();
	}
	
	@Override
	public Mappings mappings() {
		return mappings;
	}
	
	@Override
	public Map<String, Object> settings() {
		return settings;
	}
	
	@Override
	public AdminClient client() {
		return admin;
	}
	
	@Override
	public String name() {
		return index;
	}
	
	private void awaitEndOfInitialization() {
		int pendingTaskCount = 0;
		do {
			final List<PendingClusterTask> pendingTasks = admin.cluster().pendingClusterTasks(new PendingClusterTasksRequest()).actionGet()
					.getPendingTasks();
			pendingTaskCount = pendingTasks.size();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		} while (pendingTaskCount > 0);
	}

}
