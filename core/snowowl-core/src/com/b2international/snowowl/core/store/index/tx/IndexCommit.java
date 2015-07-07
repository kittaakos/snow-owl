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

import java.util.Objects;

import com.b2international.snowowl.core.store.index.Mapping;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @since 5.0
 */
@Mapping(type = "commit", mapping = "commit_mapping.json")
public class IndexCommit {
	
	public static final String COMMIT_TYPE = "commit";
	public static final String COMMIT_ID_FIELD = "commitId";
	public static final String BRANCH_PATH_FIELD = "branchPath";
	public static final String COMMIT_TIMESTAMP_FIELD = "commitTimestamp";
	
	private int commitId;
	private long commitTimestamp;
	private String branchPath;
	private String commitMessage;
	
	@JsonCreator
	IndexCommit(@JsonProperty("commitId") int commitId, @JsonProperty("commitTimestamp") long commitTimestamp, @JsonProperty("branchPath") String branchPath, @JsonProperty("commitMessage") String commitMessage) {
		this.branchPath = branchPath;
		this.commitId = commitId;
		this.commitTimestamp = commitTimestamp;
		this.commitMessage = commitMessage;
	}
	
	public int getCommitId() {
		return commitId;
	}
	
	public String getBranchPath() {
		return branchPath;
	}
	
	public long getCommitTimestamp() {
		return commitTimestamp;
	}
	
	public String getCommitMessage() {
		return commitMessage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(commitId, commitTimestamp, branchPath, commitMessage);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		IndexCommit other = (IndexCommit) obj;
		return Objects.equals(commitId, other.commitId) && Objects.equals(commitTimestamp, other.commitTimestamp) && Objects.equals(branchPath, other.branchPath) && Objects.equals(commitMessage, other.commitMessage);
	}
	
}
