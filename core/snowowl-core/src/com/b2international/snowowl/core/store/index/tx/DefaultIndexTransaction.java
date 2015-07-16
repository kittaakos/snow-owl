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

import java.util.Collections;

import com.b2international.snowowl.core.terminology.Component;

/**
 * @since 5.0
 */
class DefaultIndexTransaction implements IndexTransaction {

	private TransactionalIndex index;
	private int commitId;
	private long commitTimestamp;
	private String branchPath;

	public DefaultIndexTransaction(TransactionalIndex index, int commitId, long commitTimestamp, String branchPath) {
		this.commitId = commitId;
		this.commitTimestamp = commitTimestamp;
		this.branchPath = checkNotNull(branchPath, "branchPath");
		this.index = checkNotNull(index, "index");
	}
	
	@Override
	public void add(long storageKey, Component revision) {
		delete(storageKey, revision.getClass());
		revision.setCommitId(commitId);
		revision.setStorageKey(storageKey);
		revision.setVisibleIns(Collections.singleton(new VisibleIn(branchPath, commitTimestamp)));
		index.addRevision(branchPath, revision);
	}
	
	@Override
	public <T extends Component> void delete(long storageKey, Class<T> type) {
		// update the current revision to be unavailable after this commit by setting the VisibleIn.to to the commitTimestamp
		index.updateRevision(commitId, type, storageKey, branchPath, commitTimestamp);
	}
	
	@Override
	public void commit(String commitMessage) {
		this.index.commit(commitId, commitTimestamp, branchPath, commitMessage);
	}
	
	@Override
	public String branch() {
		return branchPath;
	}
	
}
