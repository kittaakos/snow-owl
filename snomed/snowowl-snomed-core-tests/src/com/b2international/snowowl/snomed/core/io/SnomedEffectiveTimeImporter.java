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
import static com.google.common.collect.Sets.newHashSet;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;

import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.store.index.tx.IndexTransaction;
import com.b2international.snowowl.snomed.core.io.SnomedImporterTest.RelationshipEdge;
import com.b2international.snowowl.snomed.core.store.index.Concept;
import com.b2international.snowowl.snomed.core.store.index.Description;
import com.b2international.snowowl.snomed.core.store.index.Relationship;
import com.b2international.snowowl.snomed.core.store.index.RelationshipGroup;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.io.Files;

/**
 * @since 5.0
 */
public class SnomedEffectiveTimeImporter {

	private static final AtomicLong storageKeys = new AtomicLong(0L);
	
	private IndexTransaction tx;
	
	private Collection<String[]> concepts;
	private Multimap<String, String[]> conceptDescriptions;
	private Multimap<String, String[]> conceptRelationships;

	private SnomedBrowser browser;

	private DirectedGraph<String, RelationshipEdge> graph;
	
	public SnomedEffectiveTimeImporter(final SnomedBrowser browser, DirectedGraph<String, RelationshipEdge> graph, final IndexTransaction tx, File concepts, File descriptions, File relationships) {
		this.browser = browser;
		this.graph = graph;
		this.tx = tx;
		this.concepts = readLines(concepts);
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
		Loggers.REPOSITORY.log().info("Importing components [Concept: {}, Description: {}, Relationship: {}]", this.concepts.size(), descriptionLines.size(), relationshipLines.size());
	}
	
	private Collection<String[]> readLines(File file) {
		try {
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
		processConcepts();
		processRemainingComponents();
	}

	private void processRemainingComponents() {
		final Collection<String> unprocessedConcepts = newHashSet(conceptDescriptions.keySet());
		unprocessedConcepts.addAll(conceptRelationships.keySet());
		for (String unprocessedConceptId : unprocessedConcepts) {
			final Concept concept = browser.getConcept(tx.branch(), unprocessedConceptId);
			checkNotNull(concept != null, "Concept '%s' should exists at this point", unprocessedConceptId);
			appendDescriptions(concept);
			appendRelationships(concept);
			// due to relationship changes reset the parent list
			computeParents(concept);
			tx.add(concept.getStorageKey(), concept);
		}
	}

	private void computeParents(Concept concept) {
		if (concept.isActive()) {
			final Collection<String> directParents = getSuperTypes(concept.getId());
			final Set<String> ancestorIds = newHashSet();
			populateAncestors(directParents, ancestorIds);
			concept.getParentIds().addAll(directParents);
			concept.getAncestorIds().addAll(ancestorIds);
		} else {
			concept.getParentIds().clear();
			concept.getAncestorIds().clear();
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

	private void processConcepts() {
		for (String[] conceptLine : newArrayList(concepts)) {
			final String conceptId = conceptLine[0];
			Concept concept = null;
			long storageKey = -1L;
			try {
				concept = browser.getConcept(tx.branch(), conceptId);
				storageKey = concept.getStorageKey();
			} catch (NotFoundException e) {
				storageKey = storageKeys.incrementAndGet();
			}
			// apply changes, by creating a completely new Concept from the current line
			concept = Concept.of(conceptLine);

			computeParents(concept);
			appendDescriptions(concept);
			appendRelationships(concept);
			conceptDescriptions.removeAll(conceptId);
			conceptRelationships.removeAll(conceptId);
			
			tx.add(storageKey, concept);
			concepts.remove(conceptLine);
		}
	}
	
	private void appendRelationships(Concept concept) {
		final Multimap<Integer, String[]> relationshipGroups = Multimaps.index(conceptRelationships.get(concept.getId()), new Function<String[], Integer>() {
			@Override
			public Integer apply(String[] input) {
				return Integer.parseInt(input[6]);
			}
		});
		for (Integer groupId : relationshipGroups.keySet()) {
			final RelationshipGroup group = RelationshipGroup.of(groupId);
			for (String[] conceptRelationship : relationshipGroups.get(groupId)) {
				group.getRelationships().add(Relationship.of(conceptRelationship));
				conceptRelationships.remove(concept.getId(), conceptRelationship);
			}
			concept.getRelationshipGroups().add(group);
		}
	}
	
	private void appendDescriptions(Concept concept) {
		for (String[] conceptDescription : newArrayList(conceptDescriptions.get(concept.getId()))) {
			concept.getDescriptions().add(Description.of(conceptDescription));
			conceptDescriptions.remove(concept.getId(), conceptDescription);
		}		
	}

}
