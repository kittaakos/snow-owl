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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.store.index.BulkIndex;
import com.b2international.snowowl.core.store.index.DefaultBulkIndex;
import com.b2international.snowowl.core.store.index.IndexAdmin;
import com.b2international.snowowl.core.store.index.Mappings;
import com.b2international.snowowl.core.store.index.tx.DefaultTransactionalIndex;
import com.b2international.snowowl.core.store.index.tx.IndexTransaction;
import com.b2international.snowowl.core.store.index.tx.Revision;
import com.b2international.snowowl.core.store.index.tx.TransactionalIndex;
import com.b2international.snowowl.snomed.core.store.index.Concept;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;

/**
 * @since 5.0
 */
public class SnomedImporterTest {

	// Full 20150131 INT files
	private static final String INT_CONCEPT_FILE = "d:/release_v5.0.0/SnomedCT_RF2Release_INT_20150131/Full/Terminology/sct2_Concept_Full_INT_20150131.txt";
	
	// Full 20140131 INT Mini CT files
	private static final String MINI_CONCEPT_FILE = "d:/release_v5.0.0/MiniCT/RF2Release/Full/Terminology/sct2_Concept_Full_INT_20140131.txt";
	private static final String MINI_DESCRIPTION_FILE = "d:/release_v5.0.0/MiniCT/RF2Release/Full/Terminology/sct2_Description_Full-en_INT_20140131.txt";
	private static final String MINI_RELATIONSHIP_FILE = "d:/release_v5.0.0/MiniCT/RF2Release/Full/Terminology/sct2_Relationship_Full_INT_20140131.txt";
	
	private static final String SETTINGS_FILE = "snomed_settings.json";
	
	@Rule
	public final ESRule rule = new ESRule();

	private BranchManager manager = new MockBranchManager();
	private Branch main;
	
	private TransactionalIndex index;

	private final AtomicLong timestampProvider = new AtomicLong(0L);
	private final AtomicInteger commitIdProvider = new AtomicInteger(0);
	private SnomedBrowser browser;
	
	@Before
	public void givenIndex() throws Exception {
		main = manager.getMainBranch();
		
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NON_PRIVATE);
		mapper.setVisibility(PropertyAccessor.CREATOR, Visibility.NON_PRIVATE);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		final Map<String, Object> settings = mapper.readValue(Resources.toString(Resources.getResource(Concept.class, SETTINGS_FILE), Charsets.UTF_8), Map.class);
		
		final BulkIndex index = new DefaultBulkIndex(rule.client(), "snomed_ct", Mappings.of(mapper, Concept.class), settings);
		this.index = new DefaultTransactionalIndex(index, manager);
		final IndexAdmin admin = this.index.admin();
		admin.delete();
		admin.create();
		
		browser = new SnomedBrowser(this.index);
	}
	
	@Test
	public void testRf2ImportOnMAIN() throws Exception {
		final Multimap<String, String[]> conceptsByEffectiveTime = readRf2File(MINI_CONCEPT_FILE);
		final Multimap<String, String[]> descriptionsByEffectiveTime = readRf2File(MINI_DESCRIPTION_FILE);
		final Multimap<String, String[]> relationshipsByEffectiveTime = readRf2File(MINI_RELATIONSHIP_FILE);
		
		// index by effective time
		for (String et : Ordering.natural().sortedCopy(conceptsByEffectiveTime.keySet())) {
			final IndexTransaction tx = openTransaction(main);
			Loggers.REPOSITORY.log().info("Importing SNOMED CT version {}", et);
			new SnomedEffectiveTimeImporter(browser, tx, conceptsByEffectiveTime.get(et), descriptionsByEffectiveTime.get(et), relationshipsByEffectiveTime.get(et)).doImport();
			// single commit for the effective time
			tx.commit(String.format("Imported SNOMED CT '%s' version", et));
			// create a version for the effective time 
			createBranch(main.path(), et);
		}
		
		// few assertions
		final Concept concept20020131 = browser.getConcept("MAIN/20020131", "118225008");
		assertTrue(concept20020131.isActive());
		assertThat(concept20020131.getDescriptions()).hasSize(2);
		assertThat(concept20020131.getRelationshipGroups()).hasSize(1);
		assertThat(concept20020131.getRelationshipGroups().get(0).getRelationships()).hasSize(1);
		
		final Concept concept20050131 = browser.getConcept("MAIN/20050131", "118225008");
		assertFalse(concept20050131.isActive());
	}

	// basic importer implementation
	
	private Multimap<String, String[]> readRf2File(String filePath) throws IOException {
		return Files.readLines(new File(filePath), Charsets.UTF_8, new LineProcessor<Multimap<String, String[]>>() {
			
			private Multimap<String, String[]> linesByEffectiveTime = HashMultimap.create();
			
			@Override
			public boolean processLine(String line) throws IOException {
				if (!line.startsWith("id")) {
					final String[] values = line.split("\t");
					linesByEffectiveTime.put(values[1], values);
				}
				return true;
			}

			@Override
			public Multimap<String, String[]> getResult() {
				return linesByEffectiveTime;
			}
		});
	}
	
	
	// test utilities
	
	private IndexTransaction openTransaction(final Branch branch) {
		final int commitId = commitIdProvider.incrementAndGet();
		final long commitTimestamp = timestampProvider.incrementAndGet();
		final IndexTransaction original = index.transaction(commitId, commitTimestamp, branch.path());
		return new IndexTransaction() {
			
			@Override
			public void add(long storageKey, Revision object) {
				original.add(storageKey, object);				
			}
			
			@Override
			public <T extends Revision> void delete(long storageKey, Class<T> type) {
				original.delete(storageKey, type);				
			}
			
			@Override
			public void commit(String commitMessage) {
				original.commit(commitMessage);
				// make commit available in the branch as timestamp
				when(branch.headTimestamp()).thenReturn(commitTimestamp);
			}
			
			@Override
			public TransactionalIndex index() {
				return original.index();
			}
			
			@Override
			public String branch() {
				return original.branch();
			}
		};
	}
	
	private Branch createBranch(String parent, String name) {
		return manager.getBranch(parent).createChild(name);
	}
	
}
