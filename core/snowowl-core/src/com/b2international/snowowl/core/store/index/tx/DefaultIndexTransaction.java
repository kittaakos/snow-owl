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
package com.b2international.snowowl.core.store.index.tx;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.b2international.snowowl.core.store.index.Mapping;
import com.b2international.snowowl.core.terminology.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 5.0
 */
class DefaultIndexTransaction implements IndexTransaction {

	private TransactionalIndex index;
	private int commitId;
	private long commitTimestamp;
	private ObjectMapper mapper;
	private String branchPath;

	public DefaultIndexTransaction(TransactionalIndex index, int commitId, long commitTimestamp, String branchPath, ObjectMapper mapper) {
		this.commitId = commitId;
		this.commitTimestamp = commitTimestamp;
		this.branchPath = branchPath;
		this.index = checkNotNull(index, "index");
		this.mapper = checkNotNull(mapper, "mapper");
	}
	
	@Override
	public void add(Component object) {
		index.addRevision(commitId, commitTimestamp, branchPath, getType(object), mapper.convertValue(object, Map.class));
	}
	
	@Override
	public void delete(String type, String id) {
		index.remove(commitId, commitTimestamp, branchPath, type, id);
	}
	
	@Override
	public void commit(String commitMessage) {
		this.index.commit(commitId, commitTimestamp, branchPath, commitMessage);
	}
	
	private String getType(Component object) {
		return object.getClass().getAnnotation(Mapping.class).type();
	}
	
}
