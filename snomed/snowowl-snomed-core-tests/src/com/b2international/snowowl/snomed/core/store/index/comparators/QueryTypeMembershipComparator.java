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

import com.b2international.snowowl.snomed.core.store.index.QueryTypeMembership;

/**
 * @since 5.0
 */
public class QueryTypeMembershipComparator extends MembershipComparator<QueryTypeMembership> {
	@Override
	public int compare(QueryTypeMembership o1, QueryTypeMembership o2) {
		int membershipComparison = super.compare(o1, o2);
		if (membershipComparison != 0)
			return membershipComparison;
		int queryComparison = CompareUtils.nullSafeCompare(o1.getQuery(), o2.getQuery());
		if (queryComparison != 0)
			return queryComparison;
		return 0;
	}
}