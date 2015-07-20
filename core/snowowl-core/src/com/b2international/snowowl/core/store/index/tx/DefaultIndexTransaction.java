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

import java.util.Collection;

import com.b2international.snowowl.core.terminology.Component;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @since 5.0
 */
class DefaultIndexTransaction implements IndexTransaction {

	private TransactionalIndex index;
	private int commitId;
	private long commitTimestamp;
	private String branchPath;
	private Multimap<String, Long> deletedStorageKeyByType = HashMultimap.create();

	public DefaultIndexTransaction(TransactionalIndex index, int commitId, long commitTimestamp, String branchPath) {
		this.commitId = commitId;
		this.commitTimestamp = commitTimestamp;
		this.branchPath = checkNotNull(branchPath, "branchPath");
		this.index = checkNotNull(index, "index");
	}
	
	@Override
	public void add(long storageKey, Component revision) {
		delete(storageKey, revision.getClass());
		revision.setStorageKey(storageKey);
		revision.setBranchPath(branchPath);
		revision.setCommitTimestamp(commitTimestamp);
		index.addRevision(commitId, revision);
	}
	
	@Override
	public <T extends Component> void delete(long storageKey, Class<T> type) {
		final String typeName = index.mapping(type).getType();
		deletedStorageKeyByType.put(typeName, storageKey);
	}
	
	@Override
	public void commit(String commitMessage) {
		for (String type : deletedStorageKeyByType.keySet()) {
			final Collection<Long> storageKeys = deletedStorageKeyByType.get(type);
			this.index.updateRevisions(commitId, type, storageKeys, branchPath, commitTimestamp);
		}
		this.index.commit(commitId, commitTimestamp, branchPath, commitMessage);
	}
	
	@Override
	public String branch() {
		return branchPath;
	}
	
}
