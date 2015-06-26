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

import com.b2international.snowowl.snomed.core.index.ExtendedMapTypeMembership;

/**
 * @since 5.0
 */
public class ExtendedMapTypeMembershipComparator extends MembershipComparator<ExtendedMapTypeMembership> {
	@Override
	public int compare(ExtendedMapTypeMembership o1, ExtendedMapTypeMembership o2) {
		int membershipComparison = super.compare(o1, o2);
		if (membershipComparison != 0)
			return membershipComparison;
		int correlationIdComparison = CompareUtils.nullSafeCompare(o1.getCorrelationId(), o2.getCorrelationId());
		if (correlationIdComparison != 0)
			return correlationIdComparison;
		int mapGroupComparison = Byte.compare(o1.getMapGroup(), o2.getMapGroup());
		if (mapGroupComparison != 0)
			return mapGroupComparison;
		int mapPriorityComparison = Byte.compare(o1.getMapPriority(), o2.getMapPriority());
		if (mapPriorityComparison != 0)
			return mapPriorityComparison;
		int mapRuleComparison = CompareUtils.nullSafeCompare(o1.getMapRule(), o2.getMapRule());
		if (mapRuleComparison != 0)
			return mapRuleComparison;
		int mapAdviceComparison = CompareUtils.nullSafeCompare(o1.getMapAdvice(), o2.getMapAdvice());
		if (mapAdviceComparison != 0)
			return mapAdviceComparison;
		int mapTargetComparison = CompareUtils.nullSafeCompare(o1.getMapTarget(), o2.getMapTarget());
		if (mapTargetComparison != 0)
			return mapTargetComparison;
		int mapCategoryIdComparison = CompareUtils.nullSafeCompare(o1.getMapCategoryId(), o2.getMapCategoryId());
		if (mapCategoryIdComparison != 0)
			return mapCategoryIdComparison;
		return 0;
	}
}