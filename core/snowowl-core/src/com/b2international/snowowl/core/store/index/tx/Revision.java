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


/**
 * @since 5.0
 */
public abstract class Revision {

	public static final String STORAGE_KEY = "storageKey";
	public static final String DELETED = "deleted";
	
	private long storageKey;
	private int commitId;
	private long commitTimestamp;
	private boolean deleted = false;
	
	void setCommitId(int commitId) {
		this.commitId = commitId;
	}
	
	void setCommitTimestamp(long commitTimestamp) {
		this.commitTimestamp = commitTimestamp;
	}
	
	void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	void setStorageKey(long storageKey) {
		this.storageKey = storageKey;
	}
	
	public long getStorageKey() {
		return storageKey;
	}

	public int getCommitId() {
		return commitId;
	}
	
	public long getCommitTimestamp() {
		return commitTimestamp;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	
}
