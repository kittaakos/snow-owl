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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.slf4j.Logger;

import com.b2international.snowowl.core.exceptions.SnowOwlException;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.store.index.tx.IndexTransaction;
import com.b2international.snowowl.snomed.core.io.SnomedImporterTest.RelationshipEdge;
import com.b2international.snowowl.snomed.core.store.index.Concept;
import com.b2international.snowowl.snomed.core.store.index.Description;
import com.b2international.snowowl.snomed.core.store.index.Relationship;
import com.b2international.snowowl.snomed.core.store.index.RelationshipGroup;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

/**
 * @since 5.0
 */
public class SnomedEffectiveTimeImporter {

	private static final Logger LOG = Loggers.REPOSITORY.log();
	private static final AtomicLong STORAGE_KEYS = new AtomicLong(0L);
	private static final Map<String, Long> STORAGEKEY_MAP = newHashMap();
	private static final Map<String, Concept> LATEST_CONCEPT_REVISION_MAP = newHashMap();
	
	private IndexTransaction tx;
	
	private File concepts;
	private Multimap<String, String[]> conceptDescriptions;
	private Multimap<String, String[]> conceptRelationships;

	private DirectedGraph<String, RelationshipEdge> graph;
	private Collection<String> parentageConceptChanges;
	
	public SnomedEffectiveTimeImporter(DirectedGraph<String, RelationshipEdge> graph, final IndexTransaction tx, File concepts, File descriptions, File relationships, Collection<String> parentageConceptChanges) {
		this.graph = graph;
		this.tx = tx;
		final Stopwatch watch = Stopwatch.createStarted();
		this.concepts = concepts;
		this.parentageConceptChanges = parentageConceptChanges;
		final Collection<String[]> descriptionLines = readLines(descriptions);
		final Collection<String[]> relationshipLines = readLines(relationships);
		this.conceptDescriptions = HashMultimap.create(Multimaps.index(descriptionLines, new Function<String[], String>() {
			@Override
			public String apply(String[] input) {
				return input[4];
			}
		}));
		this.conceptRelationships = HashMultimap.create(Multimaps.index(relationshipLines, new Function<String[], String>() {
			@Override
			public String apply(String[] input) {
				return input[4];
			}
		}));
		LOG.info("Read sliced RF2 files in {}", watch);
		LOG.info("Importing components [Concept: ?, Description: {}, Relationship: {}]", descriptionLines.size(), relationshipLines.size());
	}
	
	private Collection<String[]> readLines(File file) {
		if (file == null) {
			return Collections.emptySet();
		}
		try {
			LOG.info("Reading file {}", file);
			return Collections2.transform(Files.readLines(file, Charsets.UTF_8), new Function<String, String[]>() {
				@Override
				public String[] apply(String input) {
					return input.split("\t");
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void doImport() {
		try {
			processConcepts();
			processRemainingComponents();
			processParentageConceptChanges();
		} catch (Exception e) {
			throw new SnowOwlException("Failed to process effective time", e);
		}
	}

	private void processParentageConceptChanges() {
		if (parentageConceptChanges.isEmpty()) {
			return;
		}
		LOG.info("Processing {} number of concepts due to parentage change", parentageConceptChanges.size());
		for (String id : parentageConceptChanges) {
			final Concept concept = LATEST_CONCEPT_REVISION_MAP.get(id);
			checkNotNull(concept != null, "Concept '%s' should exists at this point", id);
			computeParents(concept);
			tx.add(concept.getStorageKey(), concept);
		}
	}

	private void processRemainingComponents() {
		LOG.info("Processing remaining SNOMED CT components [Descriptions: {}, Relationships: {}]", conceptDescriptions.keySet().size(), conceptRelationships.keySet().size());
		final Collection<String> unprocessedConcepts = newHashSet(conceptDescriptions.keySet());
		unprocessedConcepts.addAll(conceptRelationships.keySet());
		for (String unprocessedConceptId : unprocessedConcepts) {
			final Concept concept = LATEST_CONCEPT_REVISION_MAP.get(unprocessedConceptId);
			checkNotNull(concept != null, "Concept '%s' should exists at this point", unprocessedConceptId);
			appendDescriptions(concept);
			appendRelationships(concept);
			// due to relationship changes reset the parent list
			computeParents(concept);
			
			LATEST_CONCEPT_REVISION_MAP.put(concept.getId(), concept);
			tx.add(concept.getStorageKey(), concept);
			parentageConceptChanges.remove(concept.getId());
		}
	}

	private void computeParents(Concept concept) {
		concept.getParentIds().clear();
		concept.getAncestorIds().clear();
		if (concept.isActive()) {
			final Collection<String> directParents = getSuperTypes(concept.getId());
			final Set<String> ancestorIds = newHashSet();
			populateAncestors(directParents, ancestorIds);
			concept.getParentIds().addAll(directParents);
			concept.getAncestorIds().addAll(ancestorIds);
		}
	}

	private Collection<String> getSuperTypes(final String id) {
		return Graphs.successorListOf(graph, id);
	}
	
	private void populateAncestors(final Collection<String> parents, final Set<String> ancestorIds) {
		for (String parent : parents) {
			if (ancestorIds.add(parent)) {
				populateAncestors(getSuperTypes(parent), ancestorIds);
			}
		}
	}

	private void processConcepts() throws IOException {
		if (concepts == null) {
			return;
		}
		LOG.info("Processing SNOMED CT concepts");
		Files.readLines(concepts, Charsets.UTF_8, new LineProcessor<Boolean>() {
			@Override
			public boolean processLine(String line) throws IOException {
				final String[] conceptLine = line.split("\t");
				final String conceptId = conceptLine[0];
				Concept concept = Concept.of(conceptLine);
				computeParents(concept);
				appendDescriptions(concept);
				appendRelationships(concept);
				
				if (!STORAGEKEY_MAP.containsKey(conceptId)) {
					STORAGEKEY_MAP.put(conceptId, STORAGE_KEYS.incrementAndGet());
				}
				long storageKey = STORAGEKEY_MAP.get(conceptId);
				LATEST_CONCEPT_REVISION_MAP.put(conceptId, concept);
				tx.add(storageKey, concept);
				parentageConceptChanges.remove(conceptId);
				return true;
			}

			@Override
			public Boolean getResult() {
				return true;
			}
		});
	}
	
	private void appendRelationships(Concept concept) {
		final Collection<RelationshipGroup> previousRelationshipGroups = newArrayList(concept.getRelationshipGroups());
		concept.getRelationshipGroups().clear();
		final Multimap<Integer, String[]> relationshipGroups = Multimaps.index(conceptRelationships.get(concept.getId()), new Function<String[], Integer>() {
			@Override
			public Integer apply(String[] input) {
				return Integer.parseInt(input[6]);
			}
		});
		final Map<String, Relationship> addedRelationships = newHashMap();
		final Map<Integer, RelationshipGroup> addedRelationshipGroups = newHashMap(); 
		for (Integer groupId : relationshipGroups.keySet()) {
			final RelationshipGroup group = RelationshipGroup.of(groupId);
			for (String[] conceptRelationship : relationshipGroups.get(groupId)) {
				final Relationship relationship = Relationship.of(conceptRelationship);
				group.getRelationships().add(relationship);
				addedRelationships.put(relationship.getId(), relationship);
			}
			concept.getRelationshipGroups().add(group);
			addedRelationshipGroups.put(groupId, group);
		}
		conceptRelationships.removeAll(concept.getId());
		// append unchanged relationships from previous revision
		for (RelationshipGroup previousGroup : previousRelationshipGroups) {
			if (addedRelationshipGroups.containsKey(previousGroup.getGroup())) {
				// added as changed
				final RelationshipGroup addedGroup = addedRelationshipGroups.get(previousGroup.getGroup());
				for (Relationship previousRelationship : previousGroup.getRelationships()) {
					// check if this relationship is already added
					if (!addedRelationships.containsKey(previousRelationship.getId())) {
						// if not added, then add to the current group
						addedGroup.getRelationships().add(previousRelationship);
					}
				}
			} else {
				// missing group, add only if at least one relationship is missing from the addedRelationships
				Collection<Relationship> previousRelationships = newArrayList(previousGroup.getRelationships());
				previousGroup.getRelationships().clear();
				for (Relationship previousRelationship : previousRelationships) {
					if (!addedRelationships.containsKey(previousRelationship.getId())) {
						previousGroup.getRelationships().add(previousRelationship);
					}
				}
				// add back previous group if at least one relationship is in it
				if (!previousGroup.getRelationships().isEmpty()) {
					concept.getRelationshipGroups().add(previousGroup);
				}
			}
		}
	}
	
	private void appendDescriptions(Concept concept) {
		// make sure we have only one description for each ID, and that's the latest one
		final List<Description> previousDescriptionRevisions = newArrayList(concept.getDescriptions());
		concept.getDescriptions().clear();
		final Collection<String> addedDescriptions = newHashSet();
		for (String[] conceptDescription : newArrayList(conceptDescriptions.get(concept.getId()))) {
			concept.getDescriptions().add(Description.of(conceptDescription));
			addedDescriptions.add(conceptDescription[0]);
		}
		conceptDescriptions.removeAll(concept.getId());
		// append unchanged descriptions from previous revision
		for (Description description : previousDescriptionRevisions) {
			if (!addedDescriptions.contains(description.getId())) {
				concept.getDescriptions().add(description);
			}
		}
	}

}
