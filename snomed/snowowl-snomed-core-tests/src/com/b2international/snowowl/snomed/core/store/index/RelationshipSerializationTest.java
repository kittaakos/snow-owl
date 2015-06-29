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

import com.b2international.snowowl.snomed.core.store.index.ConcreteDomain;
import com.b2international.snowowl.snomed.core.store.index.Membership;
import com.b2international.snowowl.snomed.core.store.index.Relationship;
import com.b2international.snowowl.snomed.core.store.index.SimpleTypeMembership;
import com.b2international.snowowl.snomed.core.store.index.comparators.RelationshipComparator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RelationshipSerializationTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void serializationRoundtrip() throws Exception {
		Relationship relationship = new Relationship();
		relationship.setActive(true);
		relationship.setCharacteristicTypeId("11111111");
		relationship.setConcreteDomains(Collections.<ConcreteDomain> singletonList(new ConcreteDomain()));
		relationship.setDestinationAllAncestorIds(Collections.<String> singletonList("22222222"));
		relationship.setDestinationId("33333333");
		relationship.setEffectiveTime(new Date());
		relationship.setGroup(22);
		relationship.setId("44444444");
		relationship.setMemberships(Collections.<Membership> singletonList(new SimpleTypeMembership()));
		relationship.setModifierId("55555555");
		relationship.setModuleId("66666666");
		relationship.setReleased(true);
		relationship.setTypeAllAncestorIds(Collections.<String> singletonList("77777777"));
		relationship.setTypeId("88888888");
		relationship.setUnionGroup(33);
		// serialize
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, relationship);
		// deserialize
		Relationship deserializedRelationship = mapper.readValue(writer.toString(), Relationship.class);
		// assert
		assertThat(relationship).usingComparator(new RelationshipComparator()).isEqualTo(deserializedRelationship);
	}
}
