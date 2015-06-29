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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @since 5.0
 */
@SuiteClasses({ ConceptLabelSerializationTest.class, ConceptSerializationTest.class, ConcreteDomainSerializationTest.class,
		DescriptionSerializationTest.class, LabelSerializationTest.class, MembershipSerializationTest.class, PredicateSerializationTest.class,
		RelationshipGroupSerializationTest.class, RelationshipSerializationTest.class })
@RunWith(Suite.class)
public class SerializationSuite {

}
