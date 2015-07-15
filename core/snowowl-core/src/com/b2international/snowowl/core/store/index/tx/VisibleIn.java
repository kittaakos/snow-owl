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

import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.nestedFilter;
import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;

import java.util.Objects;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @since 5.0
 */
public final class VisibleIn {

	private String branchPath;
	private long from;
	private long to;
	
	VisibleIn(String branchPath, long from) {
		this(branchPath, from, Long.MAX_VALUE);
	}
	
	@JsonCreator
	VisibleIn(@JsonProperty("branchPath") String branchPath, @JsonProperty("from") long from, @JsonProperty("to") long to) {
		this.branchPath = branchPath;
		this.from = from;
		this.to = to;
	}
	
	public String getBranchPath() {
		return branchPath;
	}
	
	public long getFrom() {
		return from;
	}
	
	public long getTo() {
		return to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((branchPath == null) ? 0 : branchPath.hashCode());
		result = prime * result + (int) (from ^ (from >>> 32));
		result = prime * result + (int) (to ^ (to >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		VisibleIn other = (VisibleIn) obj;
		return Objects.equals(branchPath, other.branchPath) && Objects.equals(from, other.from) && Objects.equals(to, other.to); 
	}

	public static QueryBuilder createVisibleFromQuery(String branchPath, long commitTimestamp) {
		return nestedQuery("visibleIns", visibleFromFilter(branchPath, commitTimestamp));
	}
	
	public static FilterBuilder createVisibleFromFilter(String branchPath, long commitTimestamp) {
		return nestedFilter("visibleIns", visibleFromFilter(branchPath, commitTimestamp));
	}
	
	private static FilterBuilder visibleFromFilter(String branchPath, long commitTimestamp) {
		return andFilter(termFilter("visibleIns.branchPath", branchPath), rangeFilter("visibleIns.from").lte(commitTimestamp), rangeFilter("visibleIns.to").gt(commitTimestamp));
	}
	
}
