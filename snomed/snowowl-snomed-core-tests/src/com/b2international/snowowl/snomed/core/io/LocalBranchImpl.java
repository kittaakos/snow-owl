package com.b2international.snowowl.snomed.core.io;

import com.b2international.snowowl.core.internal.branch.BranchImpl;

class LocalBranchImpl extends BranchImpl {

	LocalBranchImpl(String name, String parentPath, long baseTimestamp) {
		super(name, parentPath, baseTimestamp, baseTimestamp);
	}

}