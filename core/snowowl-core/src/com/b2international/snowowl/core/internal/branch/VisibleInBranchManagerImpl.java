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
package com.b2international.snowowl.core.internal.branch;

import java.util.concurrent.atomic.AtomicLong;

import com.b2international.snowowl.core.Metadata;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.store.Store;
import com.b2international.snowowl.core.store.index.tx.TransactionalIndex;

/**
 * TODO remove the need for index, instead convert this to listenable branch manager with events and index should listen on branch creation attempts
 * to initialize revisions
 * 
 * @since 5.0
 */
public class VisibleInBranchManagerImpl extends LocalBranchManagerImpl {

	private TransactionalIndex index;

	public VisibleInBranchManagerImpl(Store<InternalBranch> branchStore, AtomicLong clock) {
		super(branchStore, clock);
	}

	public void setIndex(TransactionalIndex index) {
		this.index = index;
	}

	@Override
	protected InternalBranch reopen(InternalBranch parent, String name, Metadata metadata) {
		final String childBranchPath = parent.path().concat(Branch.SEPARATOR).concat(name);
		this.index.updateAllRevisions(parent.path(), childBranchPath, parent.headTimestamp());
		return super.reopen(parent, name, metadata);
	}

}
