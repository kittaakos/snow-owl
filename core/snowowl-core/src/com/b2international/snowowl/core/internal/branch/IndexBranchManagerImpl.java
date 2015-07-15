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

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.elasticsearch.client.Client;

import com.b2international.snowowl.core.Metadata;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.store.Store;
import com.b2international.snowowl.core.store.index.DefaultIndex;
import com.b2international.snowowl.core.store.index.Index;
import com.b2international.snowowl.core.store.index.IndexAdmin;
import com.b2international.snowowl.core.store.index.Mappings;
import com.google.common.collect.Ordering;

/**
 * @since 5.0
 */
@Deprecated
public class IndexBranchManagerImpl extends BranchManagerImpl {

	static final String INDEX_NAME_TEMPLATE = "%s_%s_%s";
	public static final String BRANCH_INDEXES = "indexes";
	private Client client;
	private Mappings mappings;
	private Map<String, Object> settings;
	private String repositoryId;
	private AtomicLong clock;
	
	public IndexBranchManagerImpl(Store<InternalBranch> branchStore, long mainBranchTimestamp, String repositoryId, Client client, Mappings mappings, Map<String, Object> settings, AtomicLong clock) {
		super(branchStore, mainBranchTimestamp);
		this.repositoryId = repositoryId;
		this.client = client;
		this.mappings = mappings;
		this.settings = settings;
		this.clock = clock;
		// init MAIN branch index
		final Branch main = getMainBranch();
		createIndex(main);
		registerBranch((InternalBranch) main);
	}
	
	@Override
	protected void initMainBranch(InternalBranch main) {
		try {
			InternalBranch mainBranch = (InternalBranch) getMainBranch();
			super.initMainBranch(mainBranch);
		} catch (NotFoundException e) {
			super.initMainBranch(main);
		}
	}
	
	private void updateBranchIndexSetMetadata(Branch parent, Branch current, String newWriteableIndexName) {
		final Metadata metadata = current.metadata();
		final LinkedList<String> indexes = newLinkedList();
		indexes.add(newWriteableIndexName);
		final Map<Long, String> branchesByBaseTimestamp = newHashMap();
		if (parent != null) {
			addIndexes(parent.metadata(), branchesByBaseTimestamp, current.baseTimestamp());
		}
		addIndexes(metadata, branchesByBaseTimestamp, -1L);
		for (Long time : Ordering.natural().reverse().sortedCopy(branchesByBaseTimestamp.keySet())) {
			final String val = branchesByBaseTimestamp.get(time);
			if (!indexes.contains(val)) {
				indexes.add(val);
			}
		}
		metadata.put(BRANCH_INDEXES, indexes);
	}

	private void addIndexes(Metadata metadata, Map<Long, String> map, long maxBase) {
		final Collection<String> branchIndexes = metadata.get(BRANCH_INDEXES, Collection.class);
		if (branchIndexes != null) {
			for (String index : branchIndexes) {
				final long base = parseBaseTimestamp(index);
				if (maxBase != -1L) {
					if (base <= maxBase) {
						map.put(base, index);
					}
				} else {
					map.put(base, index);
				}
			}
		}
	}

	private long parseBaseTimestamp(String index) {
		return Long.parseLong(index.substring(index.lastIndexOf("_") + 1));
	}

	private void createIndex(Branch branch) {
		createIndex(branch, branch.baseTimestamp());
	}
	
	private void createIndex(Branch branch, long baseTimestamp) {
		final String branchPath = branch.path();
		final String indexName = String.format(INDEX_NAME_TEMPLATE, repositoryId, branchPath.replaceAll("/", "_"), baseTimestamp).toLowerCase();
		final Index index = new DefaultIndex(client, indexName, mappings, settings);
		// create the index immediately
		final IndexAdmin admin = index.admin();
		if (!admin.exists()) {
			admin.create();
			updateBranchIndexSetMetadata(branch.parent() == branch ? null : branch.parent(), branch, indexName);		
		}
	}

	@Override
	protected InternalBranch reopen(InternalBranch parent, String name, Metadata metadata) {
		final long baseTimestamp = clock.incrementAndGet();
		final InternalBranch branch = new BranchImpl(name, parent.path(), baseTimestamp);
		branch.setBranchManager(this);
		if (metadata != null) {
			branch.metadata(metadata);
		}
		createIndex(branch);
		createIndex(parent, baseTimestamp);
		registerBranch(branch);
		registerBranch(parent);
		return branch;
	}

	@Override
	protected InternalBranch applyChangeSet(InternalBranch target, InternalBranch source, boolean dryRun, String commitMessage) {
		throw new UnsupportedOperationException();
	}
	
	public void handleCommit(Branch branch, long commitTimestamp) {
		handleCommit((InternalBranch)branch, commitTimestamp);
	}
	
}
