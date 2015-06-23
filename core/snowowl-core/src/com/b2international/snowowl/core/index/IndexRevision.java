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

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * @since 5.0
 */
public class IndexRevision {

	private Object data;
	private int commitId;
	private long commitTimestamp;
	private boolean deleted = false;
	
	IndexRevision(IndexCommit commit, Object data) {
		this(commit, data, false);
	}
	
	IndexRevision(IndexCommit commit, Object data, boolean deleted) {
		this(commit.getTransactionId(), commit.getCommitTimestamp(), data, deleted);
	}
	
	@JsonCreator
	public IndexRevision(@JsonProperty int commitId, @JsonProperty long commitTimestamp, @JsonProperty Object data, @JsonProperty boolean deleted) {
		this.commitId = commitId;
		this.commitTimestamp = commitTimestamp;
		this.data = checkNotNull(data, "Data may not be null");
	}

	@JsonUnwrapped
	public Object getData() {
		return data;
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

	/*
	 * TODO move to somewhere else?
	 */
	String type() {
		return getData().getClass().getSimpleName().toLowerCase();
	}
	
}
