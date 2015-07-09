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
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.store.index.tx.IndexTransaction;
import com.b2international.snowowl.snomed.core.store.index.Concept;
import com.b2international.snowowl.snomed.core.store.index.Description;
import com.b2international.snowowl.snomed.core.store.index.Relationship;
import com.b2international.snowowl.snomed.core.store.index.RelationshipGroup;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

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
	
	public SnomedEffectiveTimeImporter(final SnomedBrowser browser, final IndexTransaction tx, Collection<String[]> concepts, Collection<String[]> descriptions, Collection<String[]> relationships) {
		this.browser = browser;
		this.tx = tx;
		this.concepts = concepts;
		this.conceptDescriptions = HashMultimap.create(Multimaps.index(descriptions, new Function<String[], String>() {
			@Override
			public String apply(String[] input) {
				return input[4];
			}
		}));
		
		this.conceptRelationships = HashMultimap.create(Multimaps.index(relationships, new Function<String[], String>() {
			@Override
			public String apply(String[] input) {
				return input[4];
			}
		}));
		Loggers.REPOSITORY.log().info("Importing components [Concept: {}, Description: {}, Relationship: {}]", concepts.size(), descriptions.size(), relationships.size());
	}
	
	public void doImport() {
		processConcepts();
		// process remaining descriptions and relationships
		processRemainingComponents();
		checkState(concepts.isEmpty(), "At the end of effective time import, all concepts should be processed");
		checkState(conceptDescriptions.isEmpty(), "At the end of effective time import, all descriptions should be processed");
		checkState(conceptRelationships.isEmpty(), "At the end of effective time import, all relationships should be processed");
	}

	private void processRemainingComponents() {
		final Collection<String> unprocessedConcepts = newHashSet(conceptDescriptions.keySet());
		unprocessedConcepts.addAll(conceptRelationships.keySet());
		for (String unprocessedConceptId : unprocessedConcepts) {
			final Concept concept = browser.getConcept(tx.branch(), unprocessedConceptId);
			checkNotNull(concept != null, "Concept '%s' should exists at this point", unprocessedConceptId);
			appendDescriptions(concept);
			appendRelationships(concept);
			tx.add(concept.getStorageKey(), concept);
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
			
			appendDescriptions(concept);
			
			// append relationship group by group
			appendRelationships(concept);
			
			// remove processed descriptions and relationships
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
