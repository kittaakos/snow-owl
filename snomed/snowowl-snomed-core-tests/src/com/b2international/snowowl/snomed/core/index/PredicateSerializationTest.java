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

import org.junit.Test;

import com.b2international.snowowl.snomed.core.index.ConcreteDomain.Type;
import com.b2international.snowowl.snomed.core.index.Predicate.RelationshipGroupRule;
import com.b2international.snowowl.snomed.core.index.comparators.PredicateComparator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PredicateSerializationTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void serializationRoundtrip() throws Exception {
		Predicate predicate = new Predicate();
		predicate.setConcreteDomainDisplayName("foo");
		predicate.setConcreteDomainLabel("bar");
		predicate.setConcreteDomainType(Type.DECIMAL);
		predicate.setDescriptionTypeId("11111111");
		predicate.setDomainExpression("fizz");
		predicate.setId("22222222");
		predicate.setMax(42);
		predicate.setMin(8);
		predicate.setRelationshipCharacteristicTypeExpression("buzz");
		predicate.setRelationshipGroupRule(RelationshipGroupRule.MULTIPLE_GROUPS);
		predicate.setRelationshipTypeExpression("boink");
		predicate.setRelationshipValueExpression("bzzt");
		predicate.setType(Predicate.Type.DESCRIPTION);
		// serialize
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, predicate);
		// deserialize
		Predicate deserializedPredicate = mapper.readValue(writer.toString(), Predicate.class);
		// assert
		assertThat(predicate).usingComparator(new PredicateComparator()).isEqualTo(deserializedPredicate);
	}
}
