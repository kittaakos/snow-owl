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
package com.b2international.snowowl.snomed.core.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import com.b2international.snowowl.core.DefaultObjectMapper;
import com.b2international.snowowl.core.internal.branch.InternalBranch;
import com.b2international.snowowl.core.internal.branch.VisibleInBranchManagerImpl;
import com.b2international.snowowl.core.store.Store;
import com.b2international.snowowl.core.store.index.DefaultBulkIndex;
import com.b2international.snowowl.core.store.index.DefaultIndex;
import com.b2international.snowowl.core.store.index.Index;
import com.b2international.snowowl.core.store.index.IndexStore;
import com.b2international.snowowl.core.store.index.Mappings;
import com.b2international.snowowl.core.store.index.tx.DefaultTransactionalIndex;
import com.b2international.snowowl.core.store.index.tx.TransactionalIndex;
import com.b2international.snowowl.snomed.core.store.index.Concept;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;

/**
 * @since 5.0
 */
public class SnomedQueryTest {

	private static final String SETTINGS_FILE = "snomed_settings.json";
	
	@ClassRule
	public static final ESTransportClientRule rule = new ESTransportClientRule("mczotter");
	
	private Index index;
	private Store<InternalBranch> branchStore;
	private VisibleInBranchManagerImpl branching;
	
	private ObjectMapper mapper = new DefaultObjectMapper();
	private AtomicLong clock = new AtomicLong(0L);

	private TransactionalIndex txIndex;
	private SnomedBrowser browser;
	
	@Before
	public void givenIndex() throws Exception {
		final Map<String, Object> settings = mapper.readValue(Resources.toString(Resources.getResource(Concept.class, SETTINGS_FILE), Charsets.UTF_8), Map.class);
		
		this.index = new DefaultIndex(rule.client(), "snomed_ct", new Mappings(mapper));
		this.branchStore = new IndexStore<>(index, InternalBranch.class);
		this.branching = new VisibleInBranchManagerImpl(branchStore, clock);
		this.txIndex = new DefaultTransactionalIndex(new DefaultBulkIndex(index), branching);
		this.browser = new SnomedBrowser(txIndex);
		this.branching.setIndex(txIndex);
	}
	
	@Test
	public void queryChildrenOfRootOnMAIN() throws Exception {
		final String branch = "MAIN";
		final Stopwatch watch = Stopwatch.createStarted();
		final Iterable<Concept> result = browser.getChildren(branch, "138875005");
		System.out.println("RootChildren took: " + watch);
		assertThat(result).hasSize(19);
	}
	
	@Ignore
	@Test
	public void queryChildrenOfClinicalFindingOnMAIN() throws Exception {
		final String branch = "MAIN";
		final Stopwatch watch = Stopwatch.createStarted();
		final Iterable<Concept> result = browser.getDescendants(branch, "404684003");
		System.out.println("ClinicalFinding Descendants took: " + watch);
		assertThat(result).hasSize(102217);
	}
	
}
