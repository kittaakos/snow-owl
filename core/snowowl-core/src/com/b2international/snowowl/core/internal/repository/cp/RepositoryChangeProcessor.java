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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.emf.cdo.internal.server.TransactionCommitContext;
import org.eclipse.emf.cdo.server.IRepository.WriteAccessHandler;
import org.eclipse.emf.cdo.server.IStoreAccessor.CommitContext;
import org.eclipse.emf.cdo.server.ITransaction;
import org.eclipse.net4j.util.om.monitor.OMMonitor;
import org.eclipse.net4j.util.om.monitor.OMMonitor.Async;
import org.elasticsearch.common.collect.MapMaker;
import org.slf4j.Logger;

import com.b2international.snowowl.core.exceptions.SnowOwlException;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.repository.cp.ChangeProcessor;
import com.b2international.snowowl.core.repository.cp.ChangeProcessorFactory;
import com.b2international.snowowl.core.repository.cp.IEClassProvider;

/**
 * Delegates to a {@link RepositoryBranchChangeProcessor} instance, based on the affected branch.
 * 
 * @see WriteAccessHandler
 */
@SuppressWarnings("restriction")
public class RepositoryChangeProcessor implements WriteAccessHandler {

	private static final Logger LOG = Loggers.REPOSITORY.log();
	private static final String COMMIT_CONTEXT_INFO_TEMPLATE = "[%s at %s/%s]"; // [info@b2international.com at snomed_ct/MAIN/a/b/c]

	private final String repositoryName;
	private final IEClassProvider eClassProvider;
	private final Collection<ChangeProcessorFactory> changeProcessorFactories;
	private final ConcurrentMap<TransactionCommitContext, RepositoryBranchChangeProcessor> activeChangeManagers = new MapMaker().makeMap();
	
	public RepositoryChangeProcessor(final String repositoryName, final IEClassProvider eClassProvider, final Collection<ChangeProcessorFactory> changeProcessorFactories) {
		this.repositoryName = repositoryName;
		this.eClassProvider = checkNotNull(eClassProvider, "eClassProvider");
		this.changeProcessorFactories = changeProcessorFactories == null ? Collections.<ChangeProcessorFactory>emptySet() : changeProcessorFactories;
	}

	@Override
	public void handleTransactionBeforeCommitting(ITransaction transaction, CommitContext commitContext, OMMonitor monitor) throws RuntimeException {
		Async async = null;
		try {
			async = monitor.forkAsync();
			monitor.begin();
			handleBeforeCommit((TransactionCommitContext) commitContext);
		} catch (Throwable t) {
			throw SnowOwlException.wrap(t);
		} finally {
			if (async != null) {
				async.stop();
			}
			monitor.done();
		}
	}
	
	private void handleBeforeCommit(TransactionCommitContext commitContext) {
		final String commitContextInfo = getCommitContextInfo(commitContext);
		LOG.info("Processing changes for semantic indexes... " + commitContextInfo);
		final RepositoryBranchChangeProcessor delegate = new RepositoryBranchChangeProcessor(commitContext, eClassProvider, createChangeProcessors());
		activeChangeManagers.put(commitContext, delegate);
		delegate.handleBeforeCommit();
		LOG.info("Semantic index change processing successfully finished. " + commitContextInfo);
		LOG.info("Persisting changes into repository... " + commitContextInfo);
	}

	private Collection<ChangeProcessor> createChangeProcessors() {
		final Collection<ChangeProcessor> changeProcessors = newHashSet();
		// create change processors
		for (ChangeProcessorFactory factory : changeProcessorFactories) {
			changeProcessors.add(factory.create());
		}
		return changeProcessors;
	}

	@Override
	public void handleTransactionAfterCommitted(ITransaction transaction, CommitContext commitContext, OMMonitor monitor) {
		final TransactionCommitContext context = (TransactionCommitContext) commitContext;
		try {
			final String contextInfo = getCommitContextInfo(context);
			LOG.info("Changes have been successfully persisted into repository. {}", contextInfo);
			LOG.info("Flushing changes into semantic indexes... {}", contextInfo);
			final RepositoryBranchChangeProcessor delegate = activeChangeManagers.remove(context);
			delegate.handleAfterCommit();
			LOG.info("Changes have been successfully persisted into semantic indexes. {}", contextInfo);
		} catch (final Throwable e) {
			throw SnowOwlException.wrap(e);
		}
	}

	@Override
	public void handleTransactionRollback(ITransaction transaction, CommitContext commitContext) {
		final TransactionCommitContext context = (TransactionCommitContext) commitContext;
		try {
			final String contextInfo = getCommitContextInfo(context);
			LOG.info("Discarding changes in semantic indexes... " + contextInfo);
			final RepositoryBranchChangeProcessor delegate = activeChangeManagers.remove(commitContext);
			if (null == delegate) {
				LOG.trace("No changes to discard in semantic indexes. Reason: no change managers were registered. " + contextInfo);
				return;
			}
			delegate.handleRollback();
			LOG.info("Changes have been successfully discarded in semantic indexes. " + contextInfo);
		} catch (final Throwable e) {
			throw SnowOwlException.wrap(e);
		}
	}
	
	String getCommitContextInfo(final TransactionCommitContext context) {
		return String.format(COMMIT_CONTEXT_INFO_TEMPLATE, context.getUserID(), repositoryName, getBranchPath(context));
	}

	private String getBranchPath(TransactionCommitContext context) {
		return context.getTransaction().getBranch().getPathName();
	}
	
}