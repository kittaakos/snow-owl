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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.elasticsearch.client.Client;

import com.b2international.snowowl.core.Metadata;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.store.Store;
import com.b2international.snowowl.core.store.index.DefaultIndex;
import com.b2international.snowowl.core.store.index.Index;
import com.b2international.snowowl.core.store.index.Mappings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

/**
 * @since 5.0
 */
public class IndexBranchManagerImpl extends BranchManagerImpl {

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
		final String indexName = createIndex(main.path(), main.baseTimestamp());
		main.metadata(updateBranchIndexSetMetadata(null, main.metadata(), indexName));
		registerBranch((InternalBranch) main);
	}
	
	private Metadata updateBranchIndexSetMetadata(Branch parent, Metadata metadata, String indexName) {
		checkNotNull(metadata, "metadata");
		final Builder<String> builder = ImmutableSet.builder();
		// append parent branch indexes
		if (parent != null) {
			final Collection<String> branches = parent.metadata().get("branches", Collection.class);
			builder.addAll(branches);
		}
		// add current indexes
		final Collection<String> currentBranches = metadata.get("branches", Collection.class);
		if (currentBranches != null) {
			builder.addAll(currentBranches);
		}
		builder.add(indexName);
		metadata.put("branches", builder.build());
		return metadata;
	}

	private String createIndex(String branchPath, long baseTimestamp) {
		final String indexName = String.format("%s_%s_%s", repositoryId, branchPath.replaceAll("/", "_"), baseTimestamp).toLowerCase();
		final Index index = new DefaultIndex(client, indexName, mappings, settings);
		// create the index immediately
		index.admin().create();
		return indexName;
	}

	@Override
	protected InternalBranch reopen(InternalBranch parent, String name, Metadata metadata) {
		final long baseTimestamp = clock.incrementAndGet();
		final InternalBranch branch = new BranchImpl(name, parent.path(), baseTimestamp);
		branch.setBranchManager(this);
		final String indexName = createIndex(branch.path(), baseTimestamp);
		final String newParentIndex = createIndex(parent.path(), baseTimestamp);
		branch.metadata(updateBranchIndexSetMetadata(parent, metadata == null ? branch.metadata() : metadata, indexName));
		parent.metadata(updateBranchIndexSetMetadata(parent.parent(), parent.metadata(), newParentIndex));
		registerBranch(branch);
		registerBranch(parent);
		return branch;
	}

	@Override
	protected InternalBranch applyChangeSet(InternalBranch target, InternalBranch source, boolean dryRun, String commitMessage) {
		throw new UnsupportedOperationException();
	}

}
