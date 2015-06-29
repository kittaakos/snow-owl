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
package com.b2international.snowowl.snomed.core.store.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import com.b2international.snowowl.snomed.core.store.index.Concept;
import com.b2international.snowowl.snomed.core.store.index.ConcreteDomain;
import com.b2international.snowowl.snomed.core.store.index.Description;
import com.b2international.snowowl.snomed.core.store.index.Membership;
import com.b2international.snowowl.snomed.core.store.index.ReferenceSetType;
import com.b2international.snowowl.snomed.core.store.index.RelationshipGroup;
import com.b2international.snowowl.snomed.core.store.index.SimpleTypeMembership;
import com.b2international.snowowl.snomed.core.store.index.Concept.SubclassDisjointedness;
import com.b2international.snowowl.snomed.core.store.index.comparators.ConceptComparator;
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
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConceptSerializationTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void serializationRoundtrip() throws Exception {
		Concept concept = new Concept();
		concept.setActive(true);
		concept.setAncestorIds(Collections.<String> singletonList("11111111"));
		concept.setConcreteDomains(Collections.<ConcreteDomain> singletonList(new ConcreteDomain()));
		concept.setDefinitionStatusId("22222222");
		concept.setDescriptions(Collections.<Description> singletonList(new Description()));
		concept.setEffectiveTime(new Date());
		concept.setId("33333333");
		concept.setMapTargetType(12);
		concept.setMemberships(Collections.<Membership> singletonList(new SimpleTypeMembership()));
		concept.setModuleId("44444444");
		concept.setParentIds(Collections.<String> singletonList("55555555"));
		concept.setReferencedComponentType(23);
		concept.setReferenceSetType(ReferenceSetType.SIMPLE);
		concept.setRelationshipGroups(Collections.<RelationshipGroup> singletonList(new RelationshipGroup()));
		concept.setReleased(true);
		concept.setSubclassDisjointedness(SubclassDisjointedness.NON_DISJOINT);
		// serialize
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, concept);
		// deserialize
		Concept deserializedConcept = mapper.readValue(writer.toString(), Concept.class);
		// assert
		assertThat(concept).usingComparator(new ConceptComparator()).isEqualTo(deserializedConcept);
	}
}
