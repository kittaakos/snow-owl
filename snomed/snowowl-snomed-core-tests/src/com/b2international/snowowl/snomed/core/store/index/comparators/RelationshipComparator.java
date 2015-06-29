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
package com.b2international.snowowl.snomed.core.store.index.comparators;

import com.b2international.snowowl.snomed.core.store.index.ConcreteDomain;
import com.b2international.snowowl.snomed.core.store.index.Membership;
import com.b2international.snowowl.snomed.core.store.index.Relationship;
import com.google.common.collect.Ordering;

/**
 * @since 5.0
 */
public class RelationshipComparator extends SnomedComponentComparator<Relationship> {

	final private ListComparator<Membership> membershipListComparator = new ListComparator<>(new MembershipComparator<>());
	final private ListComparator<ConcreteDomain> concreteDomainListComparator = new ListComparator<>(new ConcreteDomainComparator());
	final private ListComparator<String> stringListComparator = new ListComparator<>(Ordering.<String> natural().nullsFirst());

	@Override
	public int compare(Relationship o1, Relationship o2) {
		int snomedComponentComparison = super.compare(o1, o2);
		if (snomedComponentComparison != 0)
			return snomedComponentComparison;
		int characteristicTypeComparison = CompareUtils.nullSafeCompare(o1.getCharacteristicTypeId(), o2.getCharacteristicTypeId());
		if (characteristicTypeComparison != 0)
			return characteristicTypeComparison;
		int concreteDomainComparison = concreteDomainListComparator.compare(o1.getConcreteDomains(), o2.getConcreteDomains());
		if (concreteDomainComparison != 0)
			return concreteDomainComparison;
		int destinationAllAncestorIdsComparison = stringListComparator.compare(o1.getDestinationAllAncestorIds(), o2.getDestinationAllAncestorIds());
		if (destinationAllAncestorIdsComparison != 0)
			return destinationAllAncestorIdsComparison;
		int destinationIdComparison = CompareUtils.nullSafeCompare(o1.getDestinationId(), o2.getDestinationId());
		if (destinationIdComparison != 0)
			return destinationIdComparison;
		int groupComparison = CompareUtils.nullSafeCompare(o1.getGroup(), o2.getGroup());
		if (groupComparison != 0)
			return groupComparison;
		int membershipComparison = membershipListComparator.compare(o1.getMemberships(), o2.getMemberships());
		if (membershipComparison != 0)
			return membershipComparison;
		int modifierIdComparison = CompareUtils.nullSafeCompare(o1.getModifierId(), o2.getModifierId());
		if (modifierIdComparison != 0)
			return modifierIdComparison;
		int typeAllAncestorIdsComparison = stringListComparator.compare(o1.getTypeAllAncestorIds(), o2.getTypeAllAncestorIds());
		if (typeAllAncestorIdsComparison != 0)
			return typeAllAncestorIdsComparison;
		int typeIdComparison = CompareUtils.nullSafeCompare(o1.getTypeId(), o2.getTypeId());
		if (typeIdComparison != 0)
			return typeIdComparison;
		int unionGroupComparison = CompareUtils.nullSafeCompare(o1.getUnionGroup(), o2.getUnionGroup());
		if (unionGroupComparison != 0)
			return unionGroupComparison;
		return 0;
	}

}
