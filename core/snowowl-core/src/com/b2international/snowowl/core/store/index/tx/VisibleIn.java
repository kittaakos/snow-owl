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
	
	VisibleIn(String branchPath, long from, long to) {
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
	
	
	
}
