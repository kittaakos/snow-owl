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

import static com.google.common.base.Preconditions.checkArgument;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfo;
import org.eclipse.emf.cdo.common.commit.CDOCommitInfoHandler;
import org.eclipse.emf.cdo.server.IRepository;
import org.eclipse.emf.cdo.transaction.CDOMerger;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;

import com.b2international.snowowl.core.Metadata;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.branch.BranchMergeException;
import com.b2international.snowowl.core.exceptions.SnowOwlException;
import com.b2international.snowowl.core.internal.repository.InternalRepository;
import com.b2international.snowowl.core.repository.RepositorySession;
import com.b2international.snowowl.core.session.SessionContext;
import com.b2international.snowowl.core.store.Store;

/**
 * {@link BranchManager} implementation based on {@link CDOBranch} functionality.
 *
 * @since 4.1
 */
public class CDOBranchManagerImpl extends BranchManagerImpl {

	private final IRepository cdoRepository;
    private final InternalRepository repository;
	
    public CDOBranchManagerImpl(final InternalRepository repository, final Store<InternalBranch> branchStore) {
        super(branchStore, getBasetimestamp(repository.getCdoRepository().getBranchManager().getMainBranch()));
        this.repository = repository;
        this.cdoRepository = repository.getCdoRepository();
        registerCommitListener(this.cdoRepository);
    }

    @Override
	protected void initMainBranch(InternalBranch main) {
        super.initMainBranch(new CDOMainBranchImpl(main.baseTimestamp(), main.headTimestamp()));
    }

    CDOBranch getCDOBranch(Branch branch) {
        checkArgument(!branch.isDeleted(), "Branches cannot be used after deletion");
        final Integer branchId = ((InternalCDOBasedBranch) branch).cdoBranchId();
        if (branchId != null) {
            return loadCDOBranch(branchId);
        }
        throw new SnowOwlException("Missing registered CDOBranch identifier for branch at path: %s", branch.path());
    }

    private CDOBranch loadCDOBranch(Integer branchId) {
        return cdoRepository.getBranchManager().getBranch(branchId);
    }

    @Override
    protected InternalBranch applyChangeSet(InternalBranch target, InternalBranch source, boolean dryRun, String commitMessage) {
    	final RepositorySession repositorySession = SessionContext.getRepositorySession(repository);
    	
        CDOBranch targetBranch = getCDOBranch(target);
        CDOBranch sourceBranch = getCDOBranch(source);
        CDOTransaction targetTransaction = null;
        
        try {
            targetTransaction = repositorySession.openTransaction(targetBranch);

            CDOBranchMerger merger = new CDOBranchMerger(repository.getConflictProcessor());
            targetTransaction.merge(sourceBranch.getHead(), merger);
            merger.postProcess(targetTransaction);

            targetTransaction.setCommitComment(commitMessage);

            if (!dryRun) {
	            CDOCommitInfo commitInfo = targetTransaction.commit();
	            return target.withHeadTimestamp(commitInfo.getTimeStamp());
            } else {
            	return target;
            }

        } catch (CDOMerger.ConflictException e) {
            throw new BranchMergeException("Could not resolve all conflicts while applying changeset on '%s' from '%s'.", target.path(), source.path(), e);
        } catch (CommitException e) {
            throw new BranchMergeException("Failed to apply changeset on '%s' from '%s'.", target.path(), source.path(), e);
        } finally {
            if (targetTransaction != null) {
                targetTransaction.close();
            }
        }
    }

    @Override
    protected InternalBranch reopen(InternalBranch parent, String name, Metadata metadata) {
        final CDOBranch childCDOBranch = createCDOBranch(parent, name);
        final CDOBranchPoint[] basePath = childCDOBranch.getBasePath();
        final int[] cdoBranchPath = new int[basePath.length];
        cdoBranchPath[basePath.length - 1] = childCDOBranch.getID();
        
        for (int i = 1; i < basePath.length; i++) {
        	cdoBranchPath[i - 1] = basePath[i].getBranch().getID();
        }

        final long timeStamp = basePath[basePath.length - 1].getTimeStamp();
		return reopen(parent, name, metadata, timeStamp, childCDOBranch.getID());
    }

    private InternalBranch reopen(InternalBranch parent, String name, Metadata metadata, long baseTimestamp, int id) {
        final InternalBranch branch = new CDOBranchImpl(name, parent.path(), baseTimestamp, id);
        branch.metadata(metadata);
        registerBranch(branch);
        return branch;
    }

    private CDOBranch createCDOBranch(InternalBranch parent, String name) {
        return getCDOBranch(parent).createBranch(name);
    }

    @SuppressWarnings("restriction")
    private void registerCommitListener(IRepository repository) {
        repository.addCommitInfoHandler(new CDOCommitInfoHandler() {
			@Override
            public void handleCommitInfo(CDOCommitInfo commitInfo) {
                if (!(commitInfo instanceof org.eclipse.emf.cdo.internal.common.commit.FailureCommitInfo)) {
                    handleCommit((InternalBranch) getBranch(commitInfo.getBranch().getPathName()), commitInfo.getTimeStamp());
                }
            }
        });
    }

    private static long getBasetimestamp(CDOBranch branch) {
        return branch.getBase().getTimeStamp();
    }
}
