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
package com.b2international.snowowl.snomed.core.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.util.Collections;

import org.junit.Test;

import com.b2international.snowowl.snomed.core.index.comparators.RelationshipGroupComparator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RelationshipGroupSerializationTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void serializationRoundtrip() throws Exception {
		RelationshipGroup relationshipGroup = new RelationshipGroup();
		relationshipGroup.setGroup(42);
		relationshipGroup.setRelationships(Collections.<Relationship> singletonList(new Relationship()));
		// serialize
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, relationshipGroup);
		// deserialize
		RelationshipGroup deserializedRelationshipGroup = mapper.readValue(writer.toString(), RelationshipGroup.class);
		// assert
		assertThat(relationshipGroup).usingComparator(new RelationshipGroupComparator()).isEqualTo(deserializedRelationshipGroup);
	}
}
