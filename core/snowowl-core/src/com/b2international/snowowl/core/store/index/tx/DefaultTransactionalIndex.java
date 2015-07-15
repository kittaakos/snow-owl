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
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;

import com.b2international.commons.ClassUtils;
import com.b2international.commons.exceptions.FormattedRuntimeException;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.store.index.BulkIndex;
import com.b2international.snowowl.core.store.index.IndexAdmin;
import com.b2international.snowowl.core.store.index.InternalIndex;
import com.b2international.snowowl.core.store.index.MappingStrategy;
import com.b2international.snowowl.core.store.query.Expressions;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.store.query.req.BranchAwareSearchExecutor;
import com.b2international.snowowl.core.store.query.req.DefaultSearchResponseProcessor;
import com.b2international.snowowl.core.terminology.Component;
import com.google.common.collect.Iterables;

/**
 * @since 5.0
 */
public class DefaultTransactionalIndex implements TransactionalIndex {
	
	private static final Logger LOG = Loggers.REPOSITORY.log();

	private final BulkIndex index;
	private final BranchManager branchManager;
	private final AtomicInteger internalBulks = new AtomicInteger(0);

	public DefaultTransactionalIndex(BulkIndex index, BranchManager branchManager) {
		this.index = checkNotNull(index, "index");
		this.branchManager = checkNotNull(branchManager, "branchManager");
	}

	@Override
	public <T extends Revision> T loadRevision(Class<T> type, String branchPath, long storageKey) {
		try {
			final Iterable<T> revisions = search(query()
					.on(branchPath)
					.selectAll()
					.where(Expressions.exactMatch(Revision.STORAGE_KEY, storageKey))
					.limit(1), type);
			if (Iterables.isEmpty(revisions)) {
				throw new RevisionNotFoundException(branchPath, getType(type), String.valueOf(storageKey));
			}
			return Iterables.getFirst(revisions, null);
		} catch (ElasticsearchException e) {
			throw new FormattedRuntimeException("Failed to retrieve '%s' from branch '%s' in index %s/%s", storageKey, branchPath, index.name(), type, e);
		}
	}
	
	@Override
	public <T extends Revision> void updateRevision(int commitId, Class<T> type, long storageKey, String branchPath, long commitTimestamp) {
		updateRevisions(commitId, type, Collections.singleton(storageKey), branchPath, commitTimestamp);
	}
	
	@Override
	public <T extends Revision> void updateRevisions(int commitId, Class<T> type, Collection<Long> storageKeys, String branchPath, long commitTimestamp) {
		final Map<String, Object> scriptParams = createUpdateRevisionScriptParams(commitId, branchPath, commitTimestamp);
		final Iterator<SearchHit> hitIterator = ((InternalIndex) index).scan(createStorageKeyFilter(storageKeys));
		while (hitIterator.hasNext()) {
			final SearchHit next = hitIterator.next();
			index.updateByScript(type, next.getId(), Revision.UPDATE_VISIBLE_IN_TO_SCRIPT, scriptParams);
		}		
	}
	
	private QueryBuilder createStorageKeyFilter(Collection<Long> storageKeys) {
		final OrFilterBuilder or = FilterBuilders.orFilter();
		for (Long storageKey : storageKeys) {
			or.add(FilterBuilders.termFilter(Revision.STORAGE_KEY, storageKey));
		}
		return QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), or);
	}

	private Map<String, Object> createUpdateAllRevisionScriptParams(int commitId, String branch, long commitTimestamp) {
		final Map<String, Object> params = newHashMap();
		params.put(Revision.COMMIT_ID, commitId);
		params.put("newVisibleInEntry", admin().mappings().mapper().convertValue(new VisibleIn(branch, commitTimestamp), Map.class));
		return params;
	}
	
	private Map<String, Object> createUpdateRevisionScriptParams(int commitId, String branchPath, long commitTimestamp) {
		final Map<String, Object> params = newHashMap();
		// TODO remove after bulk index refactor
		params.put(Revision.COMMIT_ID, commitId);
		params.put("branchPath", branchPath);
		params.put("commitTimestamp", commitTimestamp);
		return params;
	}
	
	@Override
	public void updateAllRevisions(String parentBranch, String childBranch, long commitTimestamp) {
		final int bulkId = internalBulks.decrementAndGet();
		index.create(bulkId);
		final Map<String, Object> params = createUpdateAllRevisionScriptParams(bulkId, childBranch, commitTimestamp);
		final Iterator<SearchHit> scan = ((InternalIndex) index).scan(VisibleIn.createVisibleFromQuery(parentBranch, commitTimestamp));
		while (scan.hasNext()) {
			final SearchHit next = scan.next();
			index.updateByScript(next.getType(), next.getId(), Revision.UPDATE_VISIBLE_IN_ADD_SCRIPT, params);
		}
		index.flush(bulkId);
	}

	@Override
	public void addRevision(String branchPath, Component revision) {
		this.index.put(revision);
	}
	
	@Override
	public void commit(int commitId, long commitTimestamp, String branchPath, String commitMessage) {
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
		final DefaultTransactionalQueryBuilder context = ClassUtils.checkAndCast(query, DefaultTransactionalQueryBuilder.class);
		context.executeWith(new BranchAwareSearchExecutor(new DefaultSearchResponseProcessor(admin().mappings().mapper()), branchManager));
		return this.index.search(context, type);
	}
	
	private <T> String getType(Class<T> type) {
		return mapping(type).getType();
	}
	
}
