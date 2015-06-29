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

import com.b2international.snowowl.snomed.core.store.index.Description;
import com.b2international.snowowl.snomed.core.store.index.Membership;
import com.b2international.snowowl.snomed.core.store.index.SimpleTypeMembership;
import com.b2international.snowowl.snomed.core.store.index.comparators.DescriptionComparator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DescriptionSerializationTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void serializationRoundtrip() throws Exception {
		Description description = new Description();
		description.setActive(true);
		description.setCaseSensitivityId("11111111");
		description.setEffectiveTime(new Date());
		description.setId("22222222");
		description.setLanguageCode("foo");
		description.setModuleId("33333333");
		description.setReleased(true);
		description.setTerm("bar");
		description.setTypeId("44444444");
		description.setMemberships(Collections.<Membership> singletonList(new SimpleTypeMembership()));
		// serialize
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, description);
		// deserialize
		Description deserializedDescription = mapper.readValue(writer.toString(), Description.class);
		// assert
		assertThat(description).usingComparator(new DescriptionComparator()).isEqualTo(deserializedDescription);
	}
}
