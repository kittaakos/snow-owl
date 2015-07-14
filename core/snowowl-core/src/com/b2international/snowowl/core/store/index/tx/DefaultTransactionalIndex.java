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

import static com.google.common.base.Preconditions.checkNotNull;

import org.elasticsearch.ElasticsearchException;
import org.slf4j.Logger;

import com.b2international.commons.exceptions.FormattedRuntimeException;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.store.index.BulkIndex;
import com.b2international.snowowl.core.store.index.Index;
import com.b2international.snowowl.core.store.index.IndexAdmin;
import com.b2international.snowowl.core.store.index.MappingStrategy;
import com.b2international.snowowl.core.store.query.Expressions;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.terminology.Component;
import com.google.common.collect.Iterables;

/**
 * Adds transaction support to an existing {@link Index} using a combination of components with revision properties and commit groups, the index can
 * act as a transactional store.
 * 
 * @since 5.0
 */
public class DefaultTransactionalIndex implements TransactionalIndex {
	
	private static final Logger LOG = Loggers.REPOSITORY.log();
	
	private BulkIndex index;

	private BranchManager branchManager;

	public DefaultTransactionalIndex(BulkIndex index, BranchManager branchManager) {
		this.index = checkNotNull(index, "index");
//		this.index.admin().mappings().addMapping(IndexCommit.class);
		this.branchManager = checkNotNull(branchManager, "branchManager");
	}

	@Override
	public <T extends Component> T loadRevision(Class<T> type, String branchPath, long storageKey) {
		try {
			final Iterable<T> revisions = search(query()
					.on(branchPath)
					.selectAll()
					.where(Expressions.exactMatch(Revision.STORAGE_KEY, storageKey))
					.limit(1), type);
			if (Iterables.isEmpty(revisions)) {
				// TODO add branchPath to exception message
				throw new NotFoundException(getType(type), String.valueOf(storageKey));
			}
			return Iterables.getFirst(revisions, null);
		} catch (ElasticsearchException e) {
			throw new FormattedRuntimeException("Failed to retrieve '%s' from branch '%s' in index %s/%s", storageKey, branchPath, index.name(), type, e);
		}
	}
	
	@Override
	public void addRevision(String branchPath, Component revision) {
		this.index.put(String.valueOf(revision.getStorageKey()), revision);
	}
	
	@Override
	public void commit(int commitId, long commitTimestamp, String branchPath, String commitMessage) {
//		final IndexCommit commit = new IndexCommit(commitId, commitTimestamp, branchPath, commitMessage);
//		this.index.put(String.valueOf(commitId), commit);
		this.index.flush(commitId);
		LOG.info("Committed transaction '{}' on '{}' with message '{}'", commitId, branchPath, commitMessage);
	}
	
	@Override
	public IndexTransaction transaction(int commitId, long commitTimestamp, String branchPath) {
		this.index.create(commitId);
		return new DefaultIndexTransaction(this, commitId, commitTimestamp, branchPath);
	}
	
	@Override
	public IndexAdmin admin() {
		return index.admin();
	}
	
	@Override
	public <T> MappingStrategy<T> mapping(Class<T> type) {
		return admin().mappings().getMapping(type);
	}
	
	@Override
	public TransactionalQueryBuilder query() {
		return new DefaultTransactionalQueryBuilder();
	}
	
	@Override
	public <T> Iterable<T> search(AfterWhereBuilder query, Class<T> type) {
//		final DefaultTransactionalQueryBuilder context = ClassUtils.checkAndCast(query, DefaultTransactionalQueryBuilder.class);
//		final Branch branch = branchManager.getBranch(context.getBranchPath());
//		context.executeWith(new MultiIndexSearchExecutor(new MultiIndexSearchProcessor(admin().mappings().mapper()), branch));
//		context.executeWith(new AggregatingBranchSearchExecutor(new AggregationSearchResponseProcessor(admin().mappings().mapper()), branchManager));
		return this.index.search(query, type);
	}
	
	private <T> String getType(Class<T> type) {
		return mapping(type).getType();
	}
	
}
