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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

/**
 * @since 5.0
 */
final class DefaultIndexRevision implements IndexRevision {

	private Map<String, Object> data;
	private long storageKey;
	private int commitId;
	private long commitTimestamp;
	private boolean deleted = false;
	
	@JsonCreator
	DefaultIndexRevision(@JsonProperty("commitId") int commitId, @JsonProperty("commitTimestamp") long commitTimestamp, @JsonProperty("storageKey") long storageKey, @JsonProperty("deleted") boolean deleted) {
		this(commitId, commitTimestamp, storageKey, deleted, Maps.<String, Object>newHashMap());
	}
	
	DefaultIndexRevision(int commitId, long commitTimestamp, long storageKey, boolean deleted, Map<String, Object> data) {
		this.commitId = commitId;
		this.commitTimestamp = commitTimestamp;
		this.storageKey = storageKey;
		this.deleted = deleted;
		this.data = checkNotNull(data, "data");
	}
	
	@JsonAnySetter
	void setData(String key, Object value) {
		this.data.put(key, value);
	}

	@JsonAnyGetter
	@Override
	public Map<String, Object> getData() {
		return data;
	}
	
	@Override
	public int getCommitId() {
		return commitId;
	}
	
	@Override
	public long getCommitTimestamp() {
		return commitTimestamp;
	}
	
	@Override
	public boolean isDeleted() {
		return deleted;
	}
	
	@Override
	public long getStorageKey() {
		return storageKey;
	}

	@Override
	public int hashCode() {
		return Objects.hash(commitId, commitTimestamp, data, deleted);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultIndexRevision other = (DefaultIndexRevision) obj;
		return Objects.equals(commitId, other.commitId) && Objects.equals(commitTimestamp, other.commitTimestamp) && Objects.equals(deleted, other.deleted) && Objects.equals(data, other.data);
	}
	
}
