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

import com.b2international.snowowl.snomed.core.index.comparators.AssociationTypeMembershipComparator;
import com.b2international.snowowl.snomed.core.index.comparators.AttributeValueTypeMembershipComparator;
import com.b2international.snowowl.snomed.core.index.comparators.ComplexMapTypeMembershipComparator;
import com.b2international.snowowl.snomed.core.index.comparators.DescriptionTypeMembershipComparator;
import com.b2international.snowowl.snomed.core.index.comparators.ExtendedMapTypeMembershipComparator;
import com.b2international.snowowl.snomed.core.index.comparators.LanguageTypeMembershipComparator;
import com.b2international.snowowl.snomed.core.index.comparators.ModuleDependencyTypeMembershipComparator;
import com.b2international.snowowl.snomed.core.index.comparators.QueryTypeMembershipComparator;
import com.b2international.snowowl.snomed.core.index.comparators.SimpleMapTypeMembershipComparator;
import com.b2international.snowowl.snomed.core.index.comparators.SimpleTypeMembershipComparator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MembershipSerializationTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void associationTypeSerializationRoundtrip() throws Exception {
		// serialize
		AssociationTypeMembership membership = new AssociationTypeMembership();
		membership.setActive(true);
		membership.setEffectiveTime(new Date(1435245348));
		membership.setId("11111111");
		membership.setModuleId("22222222");
		membership.setReferencedComponentId("33333333");
		membership.setReferenceSetId("44444444");
		membership.setReleased(true);
		membership.setTargetComponentId("555555555");
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, membership);
		// deserialize
		Membership deserializedMembership = mapper.readValue(writer.toString(), Membership.class);
		// assert
		assertThat(membership).isInstanceOf(AssociationTypeMembership.class);
		assertThat(membership).usingComparator(new AssociationTypeMembershipComparator()).isEqualTo(deserializedMembership);
	}

	@Test
	public void attributeValueTypeSerializationRoundtrip() throws Exception {
		// serialize
		AttributeValueTypeMembership membership = new AttributeValueTypeMembership();
		membership.setActive(true);
		membership.setEffectiveTime(new Date(1435245348));
		membership.setId("11111111");
		membership.setModuleId("22222222");
		membership.setReferencedComponentId("33333333");
		membership.setReferenceSetId("44444444");
		membership.setReleased(true);
		membership.setValueId("55555555");
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, membership);
		// deserialize
		Membership deserializedMembership = mapper.readValue(writer.toString(), Membership.class);
		// assert
		assertThat(membership).isInstanceOf(AttributeValueTypeMembership.class);
		assertThat(membership).usingComparator(new AttributeValueTypeMembershipComparator()).isEqualTo(deserializedMembership);
	}

	@Test
	public void complexMapTypeSerializationRoundtrip() throws Exception {
		// serialize
		ComplexMapTypeMembership membership = new ComplexMapTypeMembership();
		membership.setActive(true);
		membership.setEffectiveTime(new Date(1435245348));
		membership.setId("11111111");
		membership.setModuleId("22222222");
		membership.setReferencedComponentId("33333333");
		membership.setReferenceSetId("44444444");
		membership.setReleased(true);
		membership.setCorrelationId("55555555");
		membership.setMapAdvice("66666666");
		membership.setMapGroup(new Integer(1).byteValue());
		membership.setMapPriority(new Integer(2).byteValue());
		membership.setMapRule("77777777");
		membership.setMapTarget("88888888");
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, membership);
		// deserialize
		Membership deserializedMembership = mapper.readValue(writer.toString(), Membership.class);
		// assert
		assertThat(membership).isInstanceOf(ComplexMapTypeMembership.class);
		assertThat(membership).usingComparator(new ComplexMapTypeMembershipComparator()).isEqualTo(deserializedMembership);
	}

	@Test
	public void descriptionTypeSerializationRoundtrip() throws Exception {
		// serialize
		DescriptionTypeMembership membership = new DescriptionTypeMembership();
		membership.setActive(true);
		membership.setEffectiveTime(new Date(1435245348));
		membership.setId("11111111");
		membership.setModuleId("22222222");
		membership.setReferencedComponentId("33333333");
		membership.setReferenceSetId("44444444");
		membership.setReleased(true);
		membership.setDescriptionFormat("55555555");
		membership.setDescriptionLength(123);
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, membership);
		// deserialize
		Membership deserializedMembership = mapper.readValue(writer.toString(), Membership.class);
		// assert
		assertThat(membership).isInstanceOf(DescriptionTypeMembership.class);
		assertThat(membership).usingComparator(new DescriptionTypeMembershipComparator()).isEqualTo(deserializedMembership);
	}

	@Test
	public void extendedMapTypeSerializationRoundtrip() throws Exception {
		// serialize
		ExtendedMapTypeMembership membership = new ExtendedMapTypeMembership();
		membership.setActive(true);
		membership.setEffectiveTime(new Date(1435245348));
		membership.setId("11111111");
		membership.setModuleId("22222222");
		membership.setReferencedComponentId("33333333");
		membership.setReferenceSetId("44444444");
		membership.setReleased(true);
		membership.setCorrelationId("55555555");
		membership.setMapAdvice("66666666");
		membership.setMapCategoryId("77777777");
		membership.setMapGroup(new Integer(1).byteValue());
		membership.setMapPriority(new Integer(2).byteValue());
		membership.setMapRule("88888888");
		membership.setMapTarget("99999999");
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, membership);
		// deserialize
		Membership deserializedMembership = mapper.readValue(writer.toString(), Membership.class);
		// assert
		assertThat(membership).isInstanceOf(ExtendedMapTypeMembership.class);
		assertThat(membership).usingComparator(new ExtendedMapTypeMembershipComparator()).isEqualTo(deserializedMembership);
	}

	@Test
	public void languageTypeSerializationRoundtrip() throws Exception {
		// serialize
		LanguageTypeMembership membership = new LanguageTypeMembership();
		membership.setActive(true);
		membership.setEffectiveTime(new Date(1435245348));
		membership.setId("11111111");
		membership.setModuleId("22222222");
		membership.setReferencedComponentId("33333333");
		membership.setReferenceSetId("44444444");
		membership.setReleased(true);
		membership.setAcceptabilityId("55555555");
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, membership);
		// deserialize
		Membership deserializedMembership = mapper.readValue(writer.toString(), Membership.class);
		// assert
		assertThat(membership).isInstanceOf(LanguageTypeMembership.class);
		assertThat(membership).usingComparator(new LanguageTypeMembershipComparator()).isEqualTo(deserializedMembership);
	}

	@Test
	public void moduleDependencyTypeSerializationRoundtrip() throws Exception {
		// serialize
		ModuleDependencyTypeMembership membership = new ModuleDependencyTypeMembership();
		membership.setActive(true);
		membership.setEffectiveTime(new Date(1435245348));
		membership.setId("11111111");
		membership.setModuleId("22222222");
		membership.setReferencedComponentId("33333333");
		membership.setReferenceSetId("44444444");
		membership.setReleased(true);
		membership.setSourceEffectiveDate(new Date(1435245348));
		membership.setTargetEffectiveDate(new Date(1435255348));
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, membership);
		// deserialize
		Membership deserializedMembership = mapper.readValue(writer.toString(), Membership.class);
		// assert
		assertThat(membership).isInstanceOf(ModuleDependencyTypeMembership.class);
		assertThat(membership).usingComparator(new ModuleDependencyTypeMembershipComparator()).isEqualTo(deserializedMembership);
	}

	@Test
	public void queryTypeSerializationRoundtrip() throws Exception {
		// serialize
		QueryTypeMembership membership = new QueryTypeMembership();
		membership.setActive(true);
		membership.setEffectiveTime(new Date(1435245348));
		membership.setId("11111111");
		membership.setModuleId("22222222");
		membership.setReferencedComponentId("33333333");
		membership.setReferenceSetId("44444444");
		membership.setReleased(true);
		membership.setQuery("foo");
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, membership);
		// deserialize
		Membership deserializedMembership = mapper.readValue(writer.toString(), Membership.class);
		// assert
		assertThat(membership).isInstanceOf(QueryTypeMembership.class);
		assertThat(membership).usingComparator(new QueryTypeMembershipComparator()).isEqualTo(deserializedMembership);
	}

	@Test
	public void simpleMapTypeSerializationRoundtrip() throws Exception {
		// serialize
		SimpleMapTypeMembership membership = new SimpleMapTypeMembership();
		membership.setActive(true);
		membership.setEffectiveTime(new Date(1435245348));
		membership.setId("11111111");
		membership.setModuleId("22222222");
		membership.setReferencedComponentId("33333333");
		membership.setReferenceSetId("44444444");
		membership.setReleased(true);
		membership.setMapTarget("55555555");
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, membership);
		// deserialize
		Membership deserializedMembership = mapper.readValue(writer.toString(), Membership.class);
		// assert
		assertThat(membership).isInstanceOf(SimpleMapTypeMembership.class);
		assertThat(membership).usingComparator(new SimpleMapTypeMembershipComparator()).isEqualTo(deserializedMembership);
	}

	@Test
	public void simpleTypeSerializationRoundtrip() throws Exception {
		// serialize
		SimpleTypeMembership membership = new SimpleTypeMembership();
		membership.setActive(true);
		membership.setEffectiveTime(new Date(1435245348));
		membership.setId("11111111");
		membership.setModuleId("22222222");
		membership.setReferencedComponentId("33333333");
		membership.setReferenceSetId("44444444");
		membership.setReleased(true);
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, membership);
		// deserialize
		Membership deserializedMembership = mapper.readValue(writer.toString(), Membership.class);
		// assert
		assertThat(membership).isInstanceOf(SimpleTypeMembership.class);
		assertThat(membership).usingComparator(new SimpleTypeMembershipComparator()).isEqualTo(deserializedMembership);
	}
}
