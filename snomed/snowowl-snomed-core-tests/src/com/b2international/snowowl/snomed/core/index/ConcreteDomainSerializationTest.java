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
import java.util.Date;

import org.junit.Test;

import com.b2international.snowowl.snomed.core.index.ConcreteDomain.Type;
import com.b2international.snowowl.snomed.core.index.comparators.ConcreteDomainComparator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConcreteDomainSerializationTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void serializationRoundtrip() throws Exception {
		ConcreteDomain concreteDomain = new ConcreteDomain();
		concreteDomain.setActive(true);
		concreteDomain.setCharacteristicTypeId("11111111");
		concreteDomain.setEffectiveTime(new Date(1435245348));
		concreteDomain.setId("22222222");
		concreteDomain.setLabel("foo");
		concreteDomain.setMemberId("33333333");
		concreteDomain.setModuleId("44444444");
		concreteDomain.setOperatorId("55555555");
		concreteDomain.setReferenceSetId("66666666");
		concreteDomain.setReleased(true);
		concreteDomain.setType(Type.DECIMAL);
		concreteDomain.setUomId("77777777");
		concreteDomain.setValueBoolean(true);
		concreteDomain.setValueDecimal("88888888");
		concreteDomain.setValueString("bar");
		// serialize
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, concreteDomain);
		// deserialize
		ConcreteDomain deserializedConcreteDomain = mapper.readValue(writer.toString(), ConcreteDomain.class);
		// assert
		assertThat(concreteDomain).usingComparator(new ConcreteDomainComparator()).isEqualTo(deserializedConcreteDomain);
	}
}
