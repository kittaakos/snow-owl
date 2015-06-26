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
package com.b2international.snowowl.core.repository;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchManager;
import org.eclipse.emf.cdo.server.IRepository;
import org.eclipse.emf.cdo.transaction.CDOTransaction;

import com.b2international.snowowl.core.conflict.ICDOConflictProcessor;

/**
 * @since 5.0
 */
public interface Repository {

	// TODO remove these methods or move them to InternalRepository interface 
	CDOBranch getCdoMainBranch();
	IRepository getCdoRepository();
	CDOBranchManager getCdoBranchManager();
	CDOTransaction createTransaction(CDOBranch branch);

	// TODO is this the proper place for this method???
	ICDOConflictProcessor getConflictProcessor();
	
}
