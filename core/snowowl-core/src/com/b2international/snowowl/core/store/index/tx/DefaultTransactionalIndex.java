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
import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.hasParentFilter;
import static org.elasticsearch.index.query.FilterBuilders.orFilter;
import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.Map;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;

import com.b2international.commons.exceptions.FormattedRuntimeException;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.store.index.Index;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Adds transaction support to an existing {@link Index} using a combination of components with revision properties and commit groups, the index can
 * act as a transactional store.
 * 
 * Each {@link TransactionalIndex} works on a single branch and wraps the same {@link Index} instance to communicate with the index via a single
 * service object.
 * 
 * @since 5.0
 */
public class DefaultTransactionalIndex implements TransactionalIndex {
	
	private static final String ID_FIELD = "id";
	
	private Index index;
	private ObjectMapper mapper;

	public DefaultTransactionalIndex(Index index, ObjectMapper mapper) {
		this.index = checkNotNull(index, "index");
		this.mapper = checkNotNull(mapper, "mapper");
	}

	@Override
	public Map<String, Object> loadRevision(String type, String branchPath, String key) {
		try {
			final SearchHits hits = index.search(type, toBranchQuery(branchPath, termQuery(ID_FIELD, key)), 0, 1);
			if (hits.totalHits() <= 0) {
				// TODO add branchPath to exception message
				throw new NotFoundException(type, key);
			}
			return this.mapper.convertValue(hits.hits()[0].getSource(), IndexRevision.class).getData();
		} catch (ElasticsearchException e) {
			throw new FormattedRuntimeException("Failed to retrieve '%s' from branch '%s' in index %s/%s", key, branchPath, index.name(), type, e);
		}
	}

	@Override
	public void addRevision(int commitId, long commitTimestamp, String branchPath, String type, Map<String, Object> data) {
		checkNotNull(data.get(ID_FIELD), "Revision of %s should have an ID field, %s", type, data);
		final Map<String, Object> revData = this.mapper.convertValue(new IndexRevision(commitId, commitTimestamp, false, data), Map.class);
		this.index.putWithParent(type, String.valueOf(commitId), revData);
	}
	
	@Override
	public void remove(int commitId, long commitTimestamp, String branchPath, String type, String key) {
		final Map<String, Object> revData = loadRevision(type, branchPath, key);
		final Map<String, Object> revision = this.mapper.convertValue(new IndexRevision(commitId, commitTimestamp, true, revData), Map.class);
		this.index.putWithParent(type, String.valueOf(commitId), revision);
	}
	
	@Override
	public void commit(int commitId, long commitTimestamp, String branchPath, String commitMessage) {
		this.index.put(IndexCommit.COMMIT_TYPE, String.valueOf(commitId), this.mapper.convertValue(new IndexCommit(commitId, commitTimestamp, branchPath, commitMessage), Map.class));
	}
	
	@Override
	public IndexTransaction transaction(int commitId, long commitTimestamp, String branchPath) {
		return new DefaultIndexTransaction(this, commitId, commitTimestamp, branchPath, mapper);
	}
	
	@Override
	public TransactionalIndexAdmin admin() {
		return new DefaultTransactionalIndexAdmin(index.admin());
	}
	
	private QueryBuilder toBranchQuery(String branchPath, QueryBuilder query) {
		return filteredQuery(query, branchFilter(branchPath));
	}

	private FilterBuilder branchFilter(String branchPath) {
		final String[] segments = branchPath.split("/");
		if (/*!branchOnly && */(segments.length > 1 /*|| additionalFilter != null*/)) {
			final OrFilterBuilder or = orFilter();
			String prev = "";
			for (int i = 0; i < segments.length; i++) {
				final String segment = segments[i];
				// we need the current segment + prevSegment to make it full path and the next one to restrict head timestamp on current based on base of the next one
				String current = "";
				String next = null;
				if (!Branch.MAIN_PATH.equals(segment)) {
					current = prev.concat("/");
				}
				// if not the last segment, compute next one
				current = current.concat(segment);
				if (!segments[segments.length - 1].equals(segment)) {
					if (!current.endsWith("/")) {
						next = current.concat("/");
					}
					next = next.concat(segments[i+1]);
				}
				or.add(hasParentFilter(IndexCommit.COMMIT_TYPE, andFilter(termFilter(IndexCommit.BRANCH_PATH_FIELD, current), next == null ? timestampFilter(current) : timestampFilter(current, next))));
				prev = current;
			}
			return or;
		} else {
			return hasParentFilter(IndexCommit.COMMIT_TYPE, andFilter(termFilter(IndexCommit.BRANCH_PATH_FIELD, branchPath), timestampFilter(branchPath)));
		}
	}
	
	private FilterBuilder timestampFilter(String parentBranchPath, String childToRestrict) {
		// restrict given branchPath's HEAD to baseTimestamp of child
		final long baseTimestamp = 0L; //this.index.getBaseTimestamp(childToRestrict);
		return timestampFilter(parentBranchPath, baseTimestamp);
	}
	
	private FilterBuilder timestampFilter(String branchPath) {
		final long headTimestamp = 0L; //this.index.getHeadTimestamp(branchPath);
		return timestampFilter(branchPath, headTimestamp);
	}
	
	private FilterBuilder timestampFilter(String branchPath, long headTimestamp) {
		final long baseTimestamp = 0L; //this.index.getBaseTimestamp(branchPath);
		return rangeFilter(IndexCommit.COMMIT_TIMESTAMP_FIELD).gte(baseTimestamp).lte(headTimestamp);
	}
	
//	private static final String TRANSACTION_MAPPING_JSON = "commit_mapping.json";
//	
//	static final String MAIN_BRANCH = "MAIN";
//
//	static final String ID_FIELD = "id";
//	static final String BRANCH_PATH_FIELD = "branchPath";
//	static final String DELETED_FIELD = "deleted";
//	static final String COMMIT_ID_FIELD = "commitId";
//	static final String COMMIT_TIMESTAMP_FIELD = "commitTimestamp";
//	static final String COMMIT_TYPE = "commit";
//	static final String BASE_TIMESTAMP_FIELD = "baseTimestamp";
//	
//	private AtomicInteger transactionIds = new AtomicInteger(0);
//	private AtomicLong timestampProvider = new AtomicLong(0L);
//	private Client client;
//	private IndicesAdminClient indicesClient;
//	private String name;
//	private Mappings mappings;
//	private Map<String, Long> baseTimestampMap = new MapMaker().makeMap();
//	private Map<String, Long> headTimestampMap = new MapMaker().makeMap();
//
//	public TransactionalIndex(Client client, String name, Mappings mappings, ObjectMapper mapper) {
//		this.client = checkNotNull(client, "client");
//		this.indicesClient = this.client.admin().indices();
//		this.name = name;
//		this.mappings = mappings;
//		createBranch(null, MAIN_BRANCH);
//		try {
//			final String mapping = Resources.toString(Resources.getResource(ElasticsearchIndex.class, TRANSACTION_MAPPING_JSON), Charsets.UTF_8);
//			this.mappings.addMapping(name, COMMIT_TYPE, mapping);
//			this.mappings.addMappingStrategy(name, COMMIT_TYPE, new DefaultMappingStrategy<>(mapper, IndexCommit.class));
//		} catch (Exception e) {
//			throw new RuntimeException("Failed to read transaction mapping", e);
//		}
//	}
//	
//	public IndexCommit openTransaction(String branchPath) {
//		return new IndexCommit(this, branchPath, transactionIds.getAndIncrement(), timestampProvider.getAndIncrement());
//	}
//	
//	public IndexQueryBuilder newQuery(String branchPath) {
//		return new IndexQueryBuilder(this, branchPath);
//	}
//	
//	public IndexQueryBuilder newQuery(String type, String branchPath) {
//		return new IndexQueryBuilder(this, type, branchPath);
//	}
//
//	public void create() throws IOException {
//		if (!exists()) {
//			final CreateIndexRequestBuilder newIndex = this.indicesClient.prepareCreate(name);
//			// disable refresh, we manually refresh the index during commits
//			newIndex.setSettings(ImmutableSettings.builder().put("refresh_interval", -1).build());
//			// add mappings
//			for (Entry<String, String> entry : mappings.getMappings(name).entrySet()) {
//				LOG.info(String.format("Adding %s mapping:\n%s", entry.getKey(), entry.getValue()));
//				newIndex.addMapping(entry.getKey(), entry.getValue());
//			}
//			LOG.info("Creating index: " + name);
//			newIndex.get();
//		}
//	}
//
//	public boolean exists() {
//		LOG.info("Checking existence of {}", name);
//		return this.indicesClient.prepareExists(name).get().isExists();
//	}
//	
//	public void delete() {
//		if (exists()) {
//			LOG.info("Deleting index: {}", name);
//			this.indicesClient.prepareDelete(name).get();
//		}
//	}
//
//	IndexRequestBuilder prepareAdd(IndexRevision revision) {
//		try {
//			final String type = revision.type();
//			final Map<String, Object> json = getMappingStrategy(type).convert(revision);
//			return this.client.prepareIndex(name, type)
//					.setParent(String.valueOf(revision.getCommitId()))
//					.setSource(json);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
//	void commit(IndexCommit tx) {
//		try {
//			final Map<String, Object> json = getMappingStrategy(COMMIT_TYPE).convert(tx);
//			LOG.info("Committing transaction: {}", json);
//			final IndexResponse response = this.client.prepareIndex(name, COMMIT_TYPE)
//					.setId(String.valueOf(tx.getTransactionId()))
//					.setSource(json)
//					.setRefresh(true)
//					.get();
//			if (!response.isCreated()) {
//				throw new IllegalStateException("Failed to create transaction doc:" + tx.getTransactionId());
//			}
//			// update head timestamp
//			headTimestampMap.put(tx.getBranchPath(), tx.getCommitTimestamp());
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	SearchResponse search(QueryBuilder queryBuilder) {
//		return search(null, queryBuilder);
//	}
//	
//	SearchResponse search(String type, QueryBuilder queryBuilder) {
//		// TODO scrolling?
//		// TODO async?
//		LOG.info("Running query in {}/{}: {}", name, type, queryBuilder);
//		SearchRequestBuilder req = this.client.prepareSearch(name)
//				.setQuery(queryBuilder)
//				.setSize(Integer.MAX_VALUE) // TODO is it okay to have max int as size???
//				.addSort(ElasticsearchIndex.ID_FIELD, SortOrder.ASC)
//				.addSort(ElasticsearchIndex.COMMIT_TIMESTAMP_FIELD, SortOrder.DESC);
//		if (type != null) {
//			req.setTypes(type);
//		}
//		return req.get();
//	}
//
//	Component load(String type, String branchPath, String id) {
//		try {
//			final SearchResponse response = newQuery(type, branchPath).where(termQuery(ID_FIELD, id)).search();
//			final SearchHits hits = response.getHits();
//			if (hits.totalHits() <= 0) {
//				throw new FormattedRuntimeException("%s not found with identifier '%s' at branch '%s'", type, id, branchPath);
//			}
//			final SearchHit searchHit = hits.hits()[0];
//			return (Component) this.mappings.getMappingStrategy(name, type).convert(searchHit.getSource());
//		} catch (SearchPhaseExecutionException e) {
//			// TODO how to handle search phase exceptions
//			throw new FormattedRuntimeException("Missing index: %s/%s", name, type, e);
//		}
//	}
//
//	/*
//	 * Testing only, mostly for branching simulations, will be removed when branching support is moved to new snowowl-core
//	 */
//	void createBranch(String parent, String name) {
//		// simulate branch creation
//		final long timestamp = timestampProvider.getAndIncrement();
//		final String branchPath = !Strings.isNullOrEmpty(parent) ? parent + "/" + name : name;
//		baseTimestampMap.put(branchPath, timestamp);
//		headTimestampMap.put(branchPath, timestamp);
//	}
//
//	/*Rebase moves baseTimestamp to the current parent head, and creates a single commit without any changes for now*/
//	void rebase(String branchPath) {
//		final String parent = getParentBranchPath(branchPath);
//		// move basetimestamp to parent head
//		baseTimestampMap.put(branchPath, getHeadTimestamp(parent));
//		// simulate applyChangeset commit
//		headTimestampMap.put(branchPath, timestampProvider.getAndIncrement());
//	}
//	
//	void merge(String branchPath) {
//		final String parent = getParentBranchPath(branchPath);
//		// merge will make branch content available on parent
//		openTransaction(branchPath).changes();
//		// rebase is basically just a reopen implementation without actual applyChangeSet
//		rebase(branchPath);
//	}
//
//	private String getParentBranchPath(String branchPath) {
//		return branchPath.substring(0, branchPath.lastIndexOf("/"));
//	}
//	
//	long getBaseTimestamp(String branchPath) {
//		checkNotNull(branchPath, "branchPath");
//		return baseTimestampMap.get(branchPath);
//	}
//	
//	long getHeadTimestamp(String branchPath) {
//		checkNotNull(branchPath, "branchPath");
//		return headTimestampMap.get(branchPath);
//	}
//
//	<T> MappingStrategy<T> getMappingStrategy(String type) {
//		return (MappingStrategy<T>) mappings.getMappingStrategy(name, type);
//	}
//
//	Client client() {
//		return client;
//	}


}
