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
package com.b2international.snowowl.core.internal.repository;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.transaction.CDOTransaction;

import com.b2international.snowowl.core.repository.RepositorySession;

/**
 * @since 5.0
 */
public class DefaultRepositorySession implements RepositorySession {

	private CDONet4jSession session;

	public DefaultRepositorySession(CDONet4jSession session) {
		this.session = checkNotNull(session, "session");
	}
	
	@Override
	public String getUser() {
		return session.getUserID();
	}

	@Override
	public CDOTransaction openTransaction(CDOBranch branch) {
		return session.openTransaction(branch);
	}
	
	@Override
	public CDOTransaction openTransaction(String branchPath) {
		return session.openTransaction(session.getBranchManager().getBranch(branchPath));
	}

}
