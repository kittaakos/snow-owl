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
package com.b2international.snowowl.snomed.core.index.comparators;

import com.b2international.snowowl.snomed.core.index.Concept;
import com.b2international.snowowl.snomed.core.index.ConcreteDomain;
import com.b2international.snowowl.snomed.core.index.Description;
import com.b2international.snowowl.snomed.core.index.Membership;
import com.b2international.snowowl.snomed.core.index.RelationshipGroup;
import com.google.common.collect.Ordering;

/**
 * @since 5.0
 */
public class ConceptComparator extends SnomedComponentComparator<Concept> {

	final private ListComparator<Membership> membershipListComparator = new ListComparator<>(new MembershipComparator<>());
	final private ListComparator<ConcreteDomain> concreteDomainListComparator = new ListComparator<>(new ConcreteDomainComparator());
	final private ListComparator<Description> descriptionListComparator = new ListComparator<>(new DescriptionComparator());
	final private ListComparator<RelationshipGroup> relationshipGroupListComparator = new ListComparator<>(new RelationshipGroupComparator());
	final private ListComparator<String> stringListComparator = new ListComparator<>(Ordering.<String> natural().nullsFirst());

	@Override
	public int compare(Concept o1, Concept o2) {
		int snomedComponentComparison = super.compare(o1, o2);
		if (snomedComponentComparison != 0)
			return snomedComponentComparison;
		int ancestorIdsComparison = stringListComparator.compare(o1.getAncestorIds(), o2.getAncestorIds());
		if (ancestorIdsComparison != 0)
			return ancestorIdsComparison;
		int concreteDomainsComparison = concreteDomainListComparator.compare(o1.getConcreteDomains(), o2.getConcreteDomains());
		if (concreteDomainsComparison != 0)
			return concreteDomainsComparison;
		int defStatusComparison = CompareUtils.nullSafeCompare(o1.getDefinitionStatusId(), o2.getDefinitionStatusId());
		if (defStatusComparison != 0)
			return defStatusComparison;
		int descriptionsComparison = descriptionListComparator.compare(o1.getDescriptions(), o2.getDescriptions());
		if (descriptionsComparison != 0)
			return descriptionsComparison;
		int mapTargetTypeComparison = CompareUtils.nullSafeCompare(o1.getMapTargetType(), o2.getMapTargetType());
		if (mapTargetTypeComparison != 0)
			return mapTargetTypeComparison;
		int membershipComparison = membershipListComparator.compare(o1.getMemberships(), o2.getMemberships());
		if (membershipComparison != 0)
			return membershipComparison;
		int parentIdsComparison = stringListComparator.compare(o1.getParentIds(), o2.getParentIds());
		if (parentIdsComparison != 0)
			return parentIdsComparison;
		int refComponentTypeComparison = CompareUtils.nullSafeCompare(o1.getReferencedComponentType(), o2.getReferencedComponentType());
		if (refComponentTypeComparison != 0)
			return refComponentTypeComparison;
		int refSetTypeComparison = CompareUtils.nullSafeCompare(o1.getReferenceSetType(), o2.getReferenceSetType());
		if (refSetTypeComparison != 0)
			return refSetTypeComparison;
		int relationshipGroupsComparison = relationshipGroupListComparator.compare(o1.getRelationshipGroups(), o2.getRelationshipGroups());
		if (relationshipGroupsComparison != 0)
			return relationshipGroupsComparison;
		int subclassDisjointednessComparison = CompareUtils.nullSafeCompare(o1.getSubclassDisjointedness(), o2.getSubclassDisjointedness());
		if (subclassDisjointednessComparison != 0)
			return subclassDisjointednessComparison;
		return 0;
	}

}
