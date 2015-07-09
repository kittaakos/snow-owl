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
package com.b2international.snowowl.snomed.core.io;

import java.util.concurrent.atomic.AtomicLong;

import com.b2international.snowowl.core.Metadata;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.internal.branch.BranchImpl;
import com.b2international.snowowl.core.internal.branch.BranchManagerImpl;
import com.b2international.snowowl.core.internal.branch.InternalBranch;
import com.b2international.snowowl.core.store.Store;

/**
 * @since 5.0
 */
class LocalBranchManagerImpl extends BranchManagerImpl {

	private AtomicLong clock;

	LocalBranchManagerImpl(Store<InternalBranch> branchStore, AtomicLong clock) {
		super(branchStore, clock.incrementAndGet());
		this.clock = clock;
	}

	@Override
	protected InternalBranch reopen(InternalBranch parent, String name, Metadata metadata) {
		final InternalBranch branch = new BranchImpl(name, parent.path(), parent.headTimestamp());
		branch.metadata(metadata);
		registerBranch(branch);
		return branch;
	}

	@Override
	protected InternalBranch applyChangeSet(InternalBranch target, InternalBranch source, boolean dryRun, String commitMessage) {
		return handleCommit(target, clock.incrementAndGet());
	}

	/*
	 * Testing purposes
	 */
	public void handleCommit(Branch branch, long commitTimestamp) {
		handleCommit((InternalBranch) branch, commitTimestamp);
	}

}