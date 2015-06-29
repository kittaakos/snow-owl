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

import org.junit.Test;

import com.b2international.snowowl.snomed.core.store.index.ConceptLabel;
import com.b2international.snowowl.snomed.core.store.index.Label;
import com.b2international.snowowl.snomed.core.store.index.comparators.ConceptLabelComparator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConceptLabelSerializationTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void serializationRoundtrip() throws Exception {
		ConceptLabel conceptLabel = new ConceptLabel();
		conceptLabel.setConceptId("11111111");
		conceptLabel.setIconId("22222222");
		conceptLabel.setLabels(Collections.singletonList(new Label()));
		// serialize
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, conceptLabel);
		// deserialize
		ConceptLabel deserializedConceptLabel = mapper.readValue(writer.toString(), ConceptLabel.class);
		// assert
		assertThat(conceptLabel).usingComparator(new ConceptLabelComparator()).isEqualTo(deserializedConceptLabel);
	}

}
