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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.b2international.snowowl.core.DefaultObjectMapper;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.internal.branch.InternalBranch;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.store.Store;
import com.b2international.snowowl.core.store.index.DefaultBulkIndex;
import com.b2international.snowowl.core.store.index.DefaultIndex;
import com.b2international.snowowl.core.store.index.Index;
import com.b2international.snowowl.core.store.index.IndexAdmin;
import com.b2international.snowowl.core.store.index.Mappings;
import com.b2international.snowowl.core.store.index.tx.DefaultTransactionalIndex;
import com.b2international.snowowl.core.store.index.tx.IndexTransaction;
import com.b2international.snowowl.core.store.index.tx.Revision;
import com.b2international.snowowl.core.store.index.tx.TransactionalIndex;
import com.b2international.snowowl.core.store.mem.MemStore;
import com.b2international.snowowl.snomed.core.store.index.Concept;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Ordering;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;

/**
 * @since 5.0
 */
public class SnomedImporterTest {

	private static final String ISA = "116680003";

	// Full 20150131 INT files
	private static final String INT_CONCEPT_FILE = "d:/release_v5.0.0/SnomedCT_RF2Release_INT_20150131/Full/Terminology/sct2_Concept_Full_INT_20150131.txt";
	private static final String INT_DESCRIPTION_FILE = "d:/release_v5.0.0/SnomedCT_RF2Release_INT_20150131/Full/Terminology/sct2_Description_Full-en_INT_20150131.txt";
	private static final String INT_RELATIONSHIP_FILE = "d:/release_v5.0.0/SnomedCT_RF2Release_INT_20150131/Full/Terminology/sct2_Relationship_Full_INT_20150131.txt";
	
	// Full 20140131 INT Mini CT files
	private static final String MINI_CONCEPT_FILE = "resources/MiniCT/RF2Release/Full/Terminology/sct2_Concept_Full_INT_20140131.txt";
	private static final String MINI_DESCRIPTION_FILE = "resources/MiniCT/RF2Release/Full/Terminology/sct2_Description_Full-en_INT_20140131.txt";
	private static final String MINI_RELATIONSHIP_FILE = "resources/MiniCT/RF2Release/Full/Terminology/sct2_Relationship_Full_INT_20140131.txt";
	
	private static final String SETTINGS_FILE = "snomed_settings.json";
	
	@Rule
	public final ESLocalNodeRule rule = new ESLocalNodeRule();

	@Rule
	public final TemporaryDirectory tmpDir = new TemporaryDirectory("import");
	
	private TransactionalIndex txIndex;
	
	private Store<InternalBranch> branchStore;
	private LocalBranchManagerImpl branching;
	private Branch main;
	
	private final AtomicLong clock = new AtomicLong(0L);
	private final AtomicInteger commitIdProvider = new AtomicInteger(0);
	private SnomedBrowser browser;

	private static final boolean debug = false;
	
	@Before
	public void givenIndex() throws Exception {
		final ObjectMapper mapper = new DefaultObjectMapper();
		final Map<String, Object> settings = mapper.readValue(Resources.toString(Resources.getResource(Concept.class, SETTINGS_FILE), Charsets.UTF_8), Map.class);
		
		final Index index = new DefaultIndex(rule.client(), "snomed_ct", Mappings.of(mapper, Concept.class), settings);
		this.branchStore = new MemStore<>(InternalBranch.class);
		this.branching = new LocalBranchManagerImpl(branchStore, clock);
		this.main = branching.getMainBranch();
		
		this.txIndex = new DefaultTransactionalIndex(new DefaultBulkIndex(index), branching);
		
		final IndexAdmin admin = this.txIndex.admin();
		admin.delete();
		admin.create();
		
		browser = new SnomedBrowser(this.txIndex);
	}
	
	@Test
	public void testRf2ImportOnMAIN() throws Exception {
		final Map<String, File> conceptFilesByEffectiveTime = readRf2File(MINI_CONCEPT_FILE);
		final Map<String, File> descriptionFilesByEffectiveTime = readRf2File(MINI_DESCRIPTION_FILE);
		final Map<String, File> relationshipFilesByEffectiveTime = readRf2File(MINI_RELATIONSHIP_FILE);
		
		final DirectedGraph<String, RelationshipEdge> graph = DirectedAcyclicGraph.<String, RelationshipEdge>builder(RelationshipEdge.class).build();
		
		// index by effective time
		for (String et : Ordering.natural().sortedCopy(conceptFilesByEffectiveTime.keySet())) {
			final IndexTransaction tx = openTransaction(main);
			Loggers.REPOSITORY.log().info("Importing SNOMED CT version {}", et);
			final File concepts = conceptFilesByEffectiveTime.get(et);
			final File descriptions = descriptionFilesByEffectiveTime.get(et);
			final File relationships = relationshipFilesByEffectiveTime.get(et);
			
			Files.readLines(relationships, Charsets.UTF_8, new LineProcessor<Boolean>() {
				@Override
				public boolean processLine(String line) throws IOException {
					final String[] relationship = line.split("\t");
					if (ISA.equals(relationship[7])) {
						final String relationshipId = relationship[0];
						final boolean relationshipActive = "1".equals(relationship[2]);
						final String source = relationship[4];
						final String target = relationship[5];
						
						graph.addVertex(source);
						graph.addVertex(target);
						// check if relationship is already in the graph
						if (graph.containsEdge(source, target) && !relationshipActive) {
							final RelationshipEdge edge = graph.removeEdge(source, target);
							if (debug) {
								System.out.println("ISA remove from graph: " + edge);
							}
						} else if (!graph.containsEdge(source, target) && relationshipActive) {
							final RelationshipEdge edge = new RelationshipEdge(relationshipId);
							checkState(graph.addEdge(source, target, edge), "Can't add ISA to graph: %s: %s->%s", relationshipId, source, target);
							if (debug) {
								System.out.println("ISA added to graph: " + edge);
							}
						}
					}
					return true;
				}

				@Override
				public Boolean getResult() {
					return true;
				}
			});
			
			// pass taxonomy to effective time importer
			new SnomedEffectiveTimeImporter(browser, graph, tx, concepts, descriptions, relationships).doImport();
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
		// one direct parent 
		assertThat(concept20020131.getParentIds()).hasSize(1);
		assertThat(concept20020131.getAncestorIds()).hasSize(4);
		
		final Concept concept20050131 = browser.getConcept("MAIN/20050131", "118225008");
		assertFalse(concept20050131.isActive());
		assertThat(concept20050131.getParentIds()).hasSize(0);
		assertThat(concept20050131.getAncestorIds()).hasSize(0);
		
	}
	
	static class RelationshipEdge extends DefaultEdge {

		private static final long serialVersionUID = 2072878661906449829L;
		
		private String id;
		
		public RelationshipEdge(String id) {
			this.id = id;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(id);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass())
				return false;
			RelationshipEdge other = (RelationshipEdge) obj;
			return Objects.equals(id, other.id);
		}

		@Override
		public String toString() {
			return String.format("%s: %s->%s", id, getSource(), getTarget());
		}
		
	}
	
	private Map<String, File> readRf2File(String filePath) throws IOException {
		final File original = new File(filePath);
		Loggers.REPOSITORY.log().info("Slicing file {}", original);
		final Map<String, File> result = Files.readLines(original, Charsets.UTF_8, new LineProcessor<Map<String, File>>() {
			
			private Map<String, Writer> effectiveTimeWriters = newHashMap();
			private Map<String, File> effectiveTimeFiles = newHashMap();
			
			@Override
			public boolean processLine(String line) throws IOException {
				if (!line.startsWith("id")) {
					final String et = line.split("\t")[1];
					if (!effectiveTimeFiles.containsKey(et)) {
						final File effectiveTimeSlice = new File(tmpDir.getTmpDir(), et + "-" +original.getName());
						effectiveTimeSlice.createNewFile();
						effectiveTimeFiles.put(et, effectiveTimeSlice);
						Writer writer = Files.asCharSink(effectiveTimeSlice, Charsets.UTF_8, FileWriteMode.APPEND).openBufferedStream();
						effectiveTimeWriters.put(et, writer);
					}
					effectiveTimeWriters.get(et).write(line.concat("\r\n"));
				}
				return true;
			}

			@Override
			public Map<String, File> getResult() {
				// flush and close all writes
				for (Writer writer : effectiveTimeWriters.values()) {
					try {
						writer.flush();
						writer.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				return effectiveTimeFiles;
			}
		});
		Loggers.REPOSITORY.log().info("Completed slicing of file {}", original);
		return result;
	}
	
	@After
	public void after() {
		for (Branch branch : branching.getBranches()) {
			System.out.println(String.format("Branch[%s, base=%s, head=%s, deleted=%s, state=%s]", branch.path(), branch.baseTimestamp(), branch.headTimestamp(), branch.isDeleted(), branch.state()));
		}
	}
	
	// test utilities
	
	private IndexTransaction openTransaction(final Branch branch) {
		final int commitId = commitIdProvider.incrementAndGet();
		final long commitTimestamp = clock.incrementAndGet();
		final IndexTransaction original = txIndex.transaction(commitId, commitTimestamp, branch.path());
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
				branching.handleCommit(branch, commitTimestamp);
			}
			
			@Override
			public String branch() {
				return original.branch();
			}
		};
	}
	
	private Branch createBranch(String parent, String name) {
		return branching.getBranch(parent).createChild(name);
	}
	
}
