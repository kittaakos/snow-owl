/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.rest.components;

import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.createComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.getComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedRefSetRestRequests.executeMemberAction;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewRelationship;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewRefSet;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.lastPathSegment;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedApiTest;
import com.b2international.snowowl.snomed.api.rest.SnomedComponentType;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.google.common.collect.ImmutableMap;

/**
 * @since 4.5
 */
public class SnomedRefSetMemberApiTest extends AbstractSnomedApiTest {

	@Test
	public void getMemberNonExistingBranch() throws Exception {
		// UUID is the language reference set member for the SNOMED CT root concept's FSN
		getComponent(BranchPathUtils.createPath("MAIN/x/y/z"), SnomedComponentType.MEMBER, "e606c375-501d-5db6-821f-f03d8a12ad1c").statusCode(404);
	}

	@Test
	public void getMemberNonExistingIdentifier() throws Exception {
		getComponent(branchPath, SnomedComponentType.MEMBER, "00001111-0000-0000-0000-000000000000").statusCode(404);
	}

	@Test
	public void executeInvalidAction() throws Exception {
		String queryRefSetId = createNewRefSet(branchPath, SnomedRefSetType.QUERY);
		String simpleRefSetId = createNewRefSet(branchPath);

		final Map<?, ?> memberRequest = ImmutableMap.<String, Object>builder()
				.put(SnomedRf2Headers.FIELD_MODULE_ID, Concepts.MODULE_SCT_CORE)
				.put("referenceSetId", queryRefSetId)
				.put(SnomedRf2Headers.FIELD_REFERENCED_COMPONENT_ID, simpleRefSetId)
				.put(SnomedRf2Headers.FIELD_QUERY, "<" + Concepts.REFSET_ROOT_CONCEPT)
				.put("commitComment", "Created new query reference set member")
				.build();

		final String memberId = lastPathSegment(createComponent(branchPath, SnomedComponentType.MEMBER, memberRequest)
				.statusCode(201)
				.extract().header("Location"));

		getComponent(branchPath, SnomedComponentType.MEMBER, memberId).statusCode(200);

		final Map<?, ?> invalidActionRequest = ImmutableMap.<String, Object>builder()
				.put("action", "invalid")
				.put("commitComment", "Executed invalid action on reference set member")
				.build();

		executeMemberAction(branchPath, memberId, invalidActionRequest).statusCode(400)
		.body("message", CoreMatchers.equalTo("Invalid action type 'invalid'."));
	}

	@Test
	public void executeSyncAction() throws Exception {
		String queryRefSetId = createNewRefSet(branchPath, SnomedRefSetType.QUERY);
		String simpleRefSetId = createNewRefSet(branchPath);

		String parentId = createNewConcept(branchPath);
		List<String> conceptIds = newArrayList();
		for (int i = 0; i < 3; i++) {
			String conceptId = createNewConcept(branchPath, parentId);
			conceptIds.add(conceptId);
			// Need to add an inferred IS A counterpart, as query evaluation uses inferred relationships
			createNewRelationship(branchPath, conceptId, Concepts.IS_A, parentId, CharacteristicType.INFERRED_RELATIONSHIP);
		}

		final Map<?, ?> memberRequest = ImmutableMap.<String, Object>builder()
				.put(SnomedRf2Headers.FIELD_MODULE_ID, Concepts.MODULE_SCT_CORE)
				.put("referenceSetId", queryRefSetId)
				.put(SnomedRf2Headers.FIELD_REFERENCED_COMPONENT_ID, simpleRefSetId)
				.put(SnomedRf2Headers.FIELD_QUERY, "<" + parentId)
				.put("commitComment", "Created new query reference set member")
				.build();

		final String memberId = lastPathSegment(createComponent(branchPath, SnomedComponentType.MEMBER, memberRequest)
				.statusCode(201)
				.extract().header("Location"));

		getComponent(branchPath, SnomedComponentType.MEMBER, memberId).statusCode(200);

		// Since we have used an existing simple type refset, it will have no members initially, so sync first 
		executeSyncAction(memberId);
		checkReferencedComponentIds(conceptIds, simpleRefSetId);

		// Add a new concept that matches the query, then sync again
		String extraConceptId = createNewConcept(branchPath, parentId);
		conceptIds.add(extraConceptId);
		createNewRelationship(branchPath, extraConceptId, Concepts.IS_A, parentId, CharacteristicType.INFERRED_RELATIONSHIP);

		executeSyncAction(memberId);
		checkReferencedComponentIds(conceptIds, simpleRefSetId);
	}

	private void executeSyncAction(final String memberId) {
		final Map<?, ?> syncActionRequest = ImmutableMap.<String, Object>builder()
				.put("action", "sync")
				.put(SnomedRf2Headers.FIELD_MODULE_ID, Concepts.MODULE_SCT_CORE)
				.put("commitComment", "Executed sync action on reference set member")
				.build();

		executeMemberAction(branchPath, memberId, syncActionRequest).statusCode(200);
	}

	private void checkReferencedComponentIds(List<String> conceptIds, String simpleRefSetId) {
		List<String> referencedComponentIds = getComponent(branchPath, SnomedComponentType.REFSET, simpleRefSetId, "members()")
				.statusCode(200)
				.extract().path("members.items.referencedComponent.id");

		assertEquals(conceptIds.size(), referencedComponentIds.size());
		assertTrue(referencedComponentIds.containsAll(conceptIds));
	}
}
