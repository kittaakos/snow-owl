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

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.branch.CDOBranchPoint;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.id.CDOIDUtil;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.CDORevisionProvider;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.internal.server.TransactionCommitContext;
import org.eclipse.emf.cdo.server.StoreThreadLocal;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevisionDelta;
import org.eclipse.emf.cdo.spi.server.InternalRepository;
import org.eclipse.emf.cdo.spi.server.InternalSession;
import org.eclipse.emf.cdo.spi.server.InternalTransaction;
import org.eclipse.emf.cdo.spi.server.InternalView;
import org.eclipse.emf.cdo.util.CDOUtil;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;

import com.b2international.commons.concurrent.equinox.ForkJoinUtils;
import com.b2international.commons.status.Statuses;
import com.b2international.snowowl.core.exceptions.SnowOwlException;
import com.b2international.snowowl.core.internal.CoreActivator;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.repository.cp.ChangeProcessor;
import com.b2international.snowowl.core.repository.cp.CommitChangeSet;
import com.b2international.snowowl.core.repository.cp.IEClassProvider;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Branch aware change processing dispatcher. Dispatches changed {@link CommitChangeSet}s to {@link ChangeProcessor}s.
 */
@SuppressWarnings("restriction")
class RepositoryBranchChangeProcessor {

	private static final Logger LOG = Loggers.REPOSITORY.log();

	private final String branchPath;
	private final CommitChangeSet commitChangeSet;
	private Collection<ChangeProcessor> changeProcessors;
	private CDOView view;
	private IEClassProvider eClassProvider;
	private TransactionCommitContext context;

	RepositoryBranchChangeProcessor(TransactionCommitContext context, IEClassProvider eClassProvider,
			final Collection<ChangeProcessor> changeProcessors) {
		this.context = context;
		this.branchPath = context.getTransaction().getBranch().getPathName();
		this.changeProcessors = changeProcessors == null ? Collections.<ChangeProcessor> emptySet() : changeProcessors;
		this.eClassProvider = eClassProvider;
		this.view = getView(context);
		this.commitChangeSet = new DefaultCommitChangeSet(view, context.getUserID(), getNewObjects(context, view), getDirtyObjects(context, view),
				getDetachedObjectTypes(branchPath, context.getDetachedObjects()), getRevisionDeltas(context), context.getTimeStamp());
	}

	/**
	 * Provides a way to handle transactions that are to be committed to the lightweight store.
	 * 
	 * @throws RuntimeException
	 *             to indicate that the commit operation must not be executed against the index store.
	 */
	protected void handleBeforeCommit() throws RuntimeException {

		try {
			final AtomicReference<InternalSession> session = new AtomicReference<InternalSession>();
			session.set(StoreThreadLocal.getSession());
			final Collection<Job> changeProcessingJobs = Sets.newHashSetWithExpectedSize(changeProcessors.size());
			for (final ChangeProcessor processor : changeProcessors) {
				final String name = processor.getClass().getSimpleName();
				changeProcessingJobs.add(new Job("Processing commit changes " + name) {
					@Override
					public IStatus run(final IProgressMonitor monitor) {
						try {
							StoreThreadLocal.setSession(session.get());
							processor.process(commitChangeSet);
							return Status.OK_STATUS;
						} catch (final SnowOwlException e) {
							final String message = String.format("Error while processing '%s' on branch '%s'", name, branchPath);
							return Statuses.error(CoreActivator.PLUGIN_ID, message, e);
						} finally {
							StoreThreadLocal.release();
						}
					}
				});
			}
			ForkJoinUtils.runJobsInParallelWithErrorHandling(changeProcessingJobs, null);
		} catch (final Exception e) {
			try {
				/*
				 * XXX (apeteri): we don't know if we got here via applyChanges or a CDO commit, so handleTransactionRollback() may be called once
				 * from here and then once again, separately.
				 */
				handleRollback();
			} catch (final Exception e2) {
				e.addSuppressed(e2);
			}
			throw new SnowOwlException("Error when executing change processors on branch: %s", branchPath, e);
		}
	}

	protected void handleRollback() {
		try {
			rollbackAll();
			changeProcessors = null;
		} catch (final Exception e) {
			throw new SnowOwlException("Error when rolling back change processors on branch: %s ", branchPath, e);
		} finally {
			closeView();
		}
	}

	/**
	 * Provides a way to handle transactions after they have been committed to the lightweight store.
	 * 
	 * @param monitor
	 */
	protected void handleAfterCommit() {
		try {
			final Iterable<ChangeProcessor> dirtyChangeProcessors = Iterables.filter(changeProcessors, new Predicate<ChangeProcessor>() {
				@Override
				public boolean apply(ChangeProcessor input) {
					return input.isDirty();
				}
			});
			final Collection<Job> commitJobs = Sets.newHashSetWithExpectedSize(Iterables.size(dirtyChangeProcessors));
			for (final ChangeProcessor processor : dirtyChangeProcessors) {
				final String name = processor.getClass().getSimpleName();
				commitJobs.add(new Job("Committing " + name) {

					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						try {
							processor.commit();
							return Status.OK_STATUS;
						} catch (final SnowOwlException e) {
							try {
								processor.rollback();
							} catch (final SnowOwlException ee) {
								ee.addSuppressed(e);
								return new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, "Error while rolling back changes in " + name
										+ " for branch: " + branchPath, ee);
							}
							return new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, "Error while committing changes with " + name + " for branch: "
									+ branchPath, e);
						}
					}
				});
			}
			ForkJoinUtils.runJobsInParallelWithErrorHandling(commitJobs, null);
		} catch (final Exception e) {
			throw new SnowOwlException("Error when committing change processors on branch: " + branchPath, e);
		} finally {
			closeView();
		}
	}

	/* performs a rollback in the lightweight stores held by the CDO change processor instances. */
	private void rollbackAll() {
		final List<Exception> exceptions = Lists.newArrayList();
		for (final ChangeProcessor processor : changeProcessors) {
			final String name = processor.getClass().getSimpleName();
			try {
				processor.rollback();
			} catch (final Exception e) {
				final SnowOwlException exception = new SnowOwlException("Error while rolling back changes in %s", name, e);
				exceptions.add(exception);
			}
		}
		if (exceptions.size() == 1) {
			throw new SnowOwlException("Error while rolling back changes.", exceptions.get(0));
		} else if (exceptions.size() > 1) {
			for (final Exception exception : exceptions) {
				LOG.error("Error while rolling back changes.", exception);
			}
			throw new SnowOwlException("Multiple errors occurred while rolling back changes. See log for details.");
		}
	}

	private Map<CDOID, CDORevisionDelta> getRevisionDeltas(final TransactionCommitContext commitContext) {
		final Map<CDOID, CDORevisionDelta> revisionDeltas = newHashMap();
		for (InternalCDORevisionDelta revisionDelta : commitContext.getDirtyObjectDeltas()) {
			revisionDeltas.put(revisionDelta.getID(), revisionDelta);
		}
		return revisionDeltas;
	}

	/* Returns with a map of CDOID,EClass pairs identifying the detached objects */
	private Map<CDOID, EClass> getDetachedObjectTypes(final String branchPath, final CDOID[] detachedObjects) {
		if (detachedObjects != null && detachedObjects.length > 0) {
			final Map<CDOID, EClass> detachedObjectMap = Maps.newHashMap();
			for (CDOID detachedObjectId : detachedObjects) {
				final EClass eClass = eClassProvider.getEClass(branchPath, CDOIDUtil.getLong(detachedObjectId));
				if (eClass != null) {
					detachedObjectMap.put(detachedObjectId, eClass);
				} else {
					// certain objects may not be necessary to track in change processor, but just in case we log a warning in debug mode
					if (LOG.isDebugEnabled()) {
						LOG.warn("EClass cannot be found for CDO ID {}", detachedObjectId);
					}
				}
			}
			return detachedObjectMap;
		} else {
			return Collections.emptyMap();
		}
	}

	private CDOView getView(final TransactionCommitContext commitContext) {
		final InternalTransaction transaction = commitContext.getTransaction();
		final InternalRepository repository = transaction.getRepository();
		final CDORevisionProvider sessionRevisionProvider = new RepositoryRevisionProvider(repository, commitContext.getBranchPoint());
		final CDODelegatingCommitContext delegatingCommitContext = new CDODelegatingCommitContext(commitContext, sessionRevisionProvider);
		final InternalView sessionView = transaction.getSession().openView(InternalSession.TEMP_VIEW_ID, transaction);
		return new ServerCDOView2(sessionView, transaction.getSession(), transaction, false, delegatingCommitContext);
	}

	private final EObject[] getNewObjects(final TransactionCommitContext commitContext, final CDOView view) {
		InternalCDORevision[] newRevisions = commitContext.getNewObjects();
		EObject[] newObjects = new EObject[newRevisions.length];
		for (int i = 0; i < newRevisions.length; i++) {
			InternalCDORevision newRevision = newRevisions[i];
			CDOObject newObject = view.getObject(newRevision.getID());
			newObjects[i] = CDOUtil.getEObject(newObject);
		}
		return newObjects;
	}

	private final EObject[] getDirtyObjects(final TransactionCommitContext commitContext, final CDOView view) {
		InternalCDORevision[] dirtyRevisions = commitContext.getDirtyObjects();
		EObject[] dirtyObjects = new EObject[dirtyRevisions.length];
		for (int i = 0; i < dirtyRevisions.length; i++) {
			InternalCDORevision dirtyRevision = dirtyRevisions[i];
			CDOObject dirtyObject = view.getObject(dirtyRevision.getID());
			dirtyObjects[i] = CDOUtil.getEObject(dirtyObject);
		}
		return dirtyObjects;
	}
	
	private void closeView() {
		if (!view.isClosed()) {
			view.close();
			final int sessionViewId = view.getViewID();
			if (sessionViewId < InternalSession.TEMP_VIEW_ID) {
				final InternalSession session = context.getTransaction().getSession();
				if (session != null) {
					final InternalView internalView = session.getView(sessionViewId);
					session.viewClosed(internalView);
				}
			}
		}
	}
	
	private static class ServerCDOView2 extends org.eclipse.emf.cdo.internal.server.ServerCDOView {

		private final InternalView internalView;

		ServerCDOView2(InternalView internalView, InternalSession session, CDOBranchPoint branchPoint, boolean legacyModeEnabled,
				CDORevisionProvider revisionProvider) {
			super(session, branchPoint, legacyModeEnabled, revisionProvider);
			this.internalView = internalView;
		}

		@Override
		public int getViewID() {
			return internalView.getViewID();
		}

	}

	private static class RepositoryRevisionProvider implements CDORevisionProvider {

		private final InternalRepository repository;
		private final CDOBranchPoint branchPoint;

		RepositoryRevisionProvider(final InternalRepository repository, final CDOBranchPoint branchPoint) {
			this.repository = repository;
			this.branchPoint = branchPoint;
		}

		@Override
		public CDORevision getRevision(final CDOID id) {
			final InternalCDORevision revision = repository.getRevisionManager().getRevision(id, branchPoint, CDORevision.UNCHUNKED, 0, true);
			repository.ensureChunks(revision);
			return revision;
		}
	}
}