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

import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;

import com.google.common.collect.ImmutableList;


/**
 * @since 5.0
 */
public abstract class Revision {

	static final String UPDATE_VISIBLE_IN_TO_SCRIPT = "ctx._source.visibleIns.find{it.branchPath == branchPath}.to = commitTimestamp";
	public static final String STORAGE_KEY = "storageKey";
	// TODO remove commitId
	public static final String COMMIT_ID = "commitId";

	private int commitId;
	private long storageKey;
	private Collection<VisibleIn> visibleIns = newHashSet();
	
	protected void setStorageKey(long storageKey) {
		this.storageKey = storageKey;
	}
	
	protected void setCommitId(int commitId) {
		this.commitId = commitId;
	}
	
	protected void setVisibleIns(Collection<VisibleIn> visibleIns) {
		this.visibleIns = visibleIns;
	}
	
	public long getStorageKey() {
		return storageKey;
	}
	
	public Collection<VisibleIn> getVisibleIns() {
		return ImmutableList.copyOf(visibleIns);
	}
	
	public int getCommitId() {
		return commitId;
	}

}
