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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.b2international.commons.FileUtils;
import com.b2international.snowowl.core.DefaultObjectMapper;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.store.Store;
import com.b2international.snowowl.core.store.index.Mappings;
import com.b2international.snowowl.core.store.mem.MemStore;
import com.b2international.snowowl.core.tests.ESRule;
import com.b2international.snowowl.core.tests.person.Person;
import com.b2international.snowowl.core.tests.person.PersonFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 5.0
 */
public class IndexBranchManagerImplTest {

	@Rule
	public final ESRule rule = new ESRule();
	
	private Client client;
	private AtomicLong clock = new AtomicLong(0L);
	private Store<InternalBranch> store = new MemStore<>(InternalBranch.class);
	private BranchManager manager;

	@Before
	public void givenIndexBranchManager() {
		// clear out data folder
		FileUtils.deleteDirectory(new File("data"));
		final ObjectMapper mapper = new DefaultObjectMapper();
		this.client = rule.client();
		manager = new IndexBranchManagerImpl(store, clock.incrementAndGet(), PersonFixtures.REPO_NAME, client, Mappings.of(mapper, Person.class), null, clock);
	}
	
	@Test
	public void initializedBranchManagerShouldCreateMAINBranchOnBaseTimestamp() throws Exception {
		final Collection<String> mainBranches = getBranchIndexSet(manager.getMainBranch());
		assertThat(mainBranches).hasSize(1);
		checkIndexes(mainBranches);
	}
	
	private Collection<String> getBranchIndexSet(Branch branch) {
		return branch.metadata().get(IndexBranchManagerImpl.BRANCH_INDEXES, Collection.class);
	}

	@Test
	public void initializeIndexOnBranchCreation() throws Exception {
		final Branch mainA = manager.getMainBranch().createChild("a");
		final Collection<String> mainABranches = getBranchIndexSet(mainA);
		assertThat(mainABranches).containsExactly("person_main_a_2", "person_main_1");
		checkIndexes(mainABranches);
		final Collection<String> mainBranches = getBranchIndexSet(manager.getMainBranch());
		assertThat(mainBranches).containsExactly("person_main_2", "person_main_1");
		checkIndexes(mainBranches);
	}
	
	@Test
	public void initializeIndexOnNestedBranchCreation() throws Exception {
		final Branch a = manager.getMainBranch().createChild("a");
		final Branch b = a.createChild("b");
		final Collection<String> bBranches = getBranchIndexSet(b);
		assertThat(bBranches).containsExactly("person_main_a_b_3", "person_main_a_2", "person_main_1");
		final Collection<String> aBranches = getBranchIndexSet(a);
		assertThat(aBranches).containsExactly("person_main_a_3", "person_main_a_2", "person_main_1");
		final Collection<String> mainBranches = getBranchIndexSet(manager.getMainBranch());
		assertThat(mainBranches).containsExactly("person_main_2", "person_main_1");
	}

	private void checkIndexes(Collection<String> indexNames) {
		final IndicesAdminClient admin = client.admin().indices();
		for (String indexName : indexNames) {
			assertTrue("Index should be created: " + indexName, admin.prepareExists(indexName).get().isExists());
		}
	}
	
}
