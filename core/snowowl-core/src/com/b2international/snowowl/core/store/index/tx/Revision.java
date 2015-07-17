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

	public static final String UPDATE_REVISION_TIMESTAMP_SCRIPT_KEY = "updateRevisionTimestampScript";
	public static final String UPDATE_REVISION_TIMESTAMP_SCRIPT = "ctx._source.visibleIns.find{it.branchPath == branchPath}.to = commitTimestamp";
	public static final String BRANCH_CREATE_TAG_SCRIPT_KEY = "branchCreateTagScript";
	public static final String BRANCH_CREATE_TAG_SCRIPT = "def parentTime = ctx._source.visibleIns.find{it.branchPath == parent}.from;ctx._source.visibleIns += [branchPath: child,from: parentTime,to:Long.MAX_VALUE]";
	public static final String STORAGE_KEY = "storageKey";

	private long storageKey;
	private Collection<VisibleIn> visibleIns = newHashSet();
	
	public void setStorageKey(long storageKey) {
		this.storageKey = storageKey;
	}
	
	public void setVisibleIns(Collection<VisibleIn> visibleIns) {
		this.visibleIns = visibleIns;
	}
	
	public long getStorageKey() {
		return storageKey;
	}
	
	public Collection<VisibleIn> getVisibleIns() {
		return ImmutableList.copyOf(visibleIns);
	}
	
}
