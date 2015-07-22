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
package com.b2international.snowowl.core.internal.repository.cp;

import java.util.Collection;

import org.eclipse.emf.cdo.internal.server.TransactionCommitContext;

import com.b2international.snowowl.core.repository.cp.ChangeProcessor;
import com.b2international.snowowl.core.repository.cp.IEClassProvider;

/**
 * @since 5.0
 */
class LockingRepositoryBranchChangeProcessor extends RepositoryBranchChangeProcessor {

//	private IOperationLockTarget lockTarget;
	
	LockingRepositoryBranchChangeProcessor(TransactionCommitContext context, IEClassProvider eClassProvider, Collection<ChangeProcessor> changeProcessors) {
		super(context, eClassProvider, changeProcessors);
	}
	
	@Override
	protected void handleBeforeCommit() throws RuntimeException {
//		lockBranch();
		super.handleBeforeCommit();
	}
	
	@Override
	protected void handleAfterCommit() {
		try {
			super.handleAfterCommit();
		} finally {
//			unlockBranch();
		}
	}

//	private void lockBranch() {
//
//		final IOperationLockTarget target = createLockTarget();
//		final DatastoreLockContext lockContext = createLockContext();
//		
//		try {
//			lockTarget = null;
//			getLockManager().lock(lockContext, IOperationLockManager.IMMEDIATE, target);
//		} catch (final DatastoreOperationLockException dle) {
//			throw createRepositoryLockException(target, dle.getContext(target));
//		} catch (final OperationLockException le) {
//			throw createRepositoryLockException(target);
//		} catch (final InterruptedException e) {
//			throw SnowowlRuntimeException.wrap(e);
//		}
//		lockTarget = target;
//	}
	
//	private SingleRepositoryAndBranchLockTarget createLockTarget() {
//		return new SingleRepositoryAndBranchLockTarget(repositoryUuid, branchPath);
//	}
//
//	private DatastoreLockContext createLockContext() {
//		return new DatastoreLockContext(commitChangeSet.getUserId(), DatastoreLockContextDescriptions.PROCESS_CHANGES, DatastoreLockContextDescriptions.COMMIT);
//	}
//
//	private RepositoryLockException createRepositoryLockException(final IOperationLockTarget lockTarget) {
//		return new RepositoryLockException("Write access to " + lockTarget + " was denied; please try again later.");
//	}
//	
//	private RepositoryLockException createRepositoryLockException(final IOperationLockTarget lockTarget, final DatastoreLockContext context) {
//		if (null == context) {
//			return createRepositoryLockException(lockTarget);
//		}
//		
//		return new RepositoryLockException("Write access to " + lockTarget + " was denied because " + context.getUserId() + " is " + context.getDescription() + ". Please try again later.");
//	}
//	private void unlockBranch(final RuntimeException caughtException) {
//
//		// Check first if we even managed to get the lock
//		if (null == lockTarget) {
//			return;
//		}
//		
//		try {
//			final DatastoreLockContext lockContext = createLockContext();
//			getLockManager().unlock(lockContext, lockTarget);
//			lockTarget = null;
//		} catch (final OperationLockException le) {
//			if (null != caughtException) {
//				caughtException.addSuppressed(createUnlockException());
//				throw caughtException;
//			} else {
//				throw createUnlockException();
//			}
//		}
//	}
//	private RepositoryLockException createUnlockException() {
//		return new RepositoryLockException("Could not unlock " + lockTarget + ".");
//	}
//	private IDatastoreOperationLockManager getLockManager() {
//		return getApplicationContext().getServiceChecked(IDatastoreOperationLockManager.class);
//	}
	
}
