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

import com.b2international.snowowl.snomed.core.index.Description;
import com.b2international.snowowl.snomed.core.index.Membership;

/**
 * @since 5.0
 */
public class DescriptionComparator extends SnomedComponentComparator<Description> {

	final private ListComparator<Membership> membershipListComparator = new ListComparator<>(new MembershipComparator<>());

	@Override
	public int compare(Description o1, Description o2) {
		int snomedComponentComparison = super.compare(o1, o2);
		if (snomedComponentComparison != 0)
			return snomedComponentComparison;
		int termComparison = CompareUtils.nullSafeCompare(o1.getTerm(), o2.getTerm());
		if (termComparison != 0)
			return termComparison;
		int typeIdComparison = CompareUtils.nullSafeCompare(o1.getTypeId(), o2.getTypeId());
		if (typeIdComparison != 0)
			return typeIdComparison;
		int caseSensitivityComparison = CompareUtils.nullSafeCompare(o1.getCaseSensitivityId(), o2.getCaseSensitivityId());
		if (caseSensitivityComparison != 0)
			return caseSensitivityComparison;
		int languageCodeComparison = CompareUtils.nullSafeCompare(o1.getLanguageCode(), o2.getLanguageCode());
		if (languageCodeComparison != 0)
			return languageCodeComparison;
		int membershipComparison = membershipListComparator.compare(o1.getMemberships(), o2.getMemberships());
		if (membershipComparison != 0)
			return membershipComparison;
		return 0;
	}

}
