/*
 * Copyright 2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.datastore.request;

import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.store.SnomedComponents;
import com.b2international.snowowl.snomed.snomedrefset.SnomedComplexMapRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSet;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;

/**
 * @since 5.0
 */
final class SnomedComplexMapMemberCreateDelegate extends SnomedRefSetMemberCreateDelegate {

	SnomedComplexMapMemberCreateDelegate(SnomedRefSetMemberCreateRequest request) {
		super(request);
	}

	@Override
	public String execute(SnomedRefSet refSet, TransactionContext context) {
		checkRefSetType(refSet, SnomedRefSetType.COMPLEX_MAP);
		checkReferencedComponentId(refSet);
		checkNonEmptyProperty(refSet, SnomedRf2Headers.FIELD_MAP_TARGET);
		checkHasProperty(refSet, SnomedRf2Headers.FIELD_MAP_GROUP);
		checkHasProperty(refSet, SnomedRf2Headers.FIELD_MAP_PRIORITY);
		checkHasProperty(refSet, SnomedRf2Headers.FIELD_MAP_RULE);
		checkHasProperty(refSet, SnomedRf2Headers.FIELD_MAP_ADVICE);
		checkNonEmptyProperty(refSet, SnomedRf2Headers.FIELD_CORRELATION_ID);

		SnomedComplexMapRefSetMember member = SnomedComponents.newComplexMapMember()
				.withActive(isActive())
				.withReferencedComponent(getReferencedComponentId())
				.withModule(getModuleId())
				.withRefSet(getReferenceSetId())
				.withMapTargetId(getProperty(SnomedRf2Headers.FIELD_MAP_TARGET))
				.withGroup(getProperty(SnomedRf2Headers.FIELD_MAP_GROUP, Byte.class))
				.withPriority(getProperty(SnomedRf2Headers.FIELD_MAP_PRIORITY, Byte.class))
				.withMapRule(getProperty(SnomedRf2Headers.FIELD_MAP_RULE))
				.withMapAdvice(getProperty(SnomedRf2Headers.FIELD_MAP_ADVICE))
				.withCorrelationId(getProperty(SnomedRf2Headers.FIELD_CORRELATION_ID))
				.addTo(context);

		return member.getUuid();
	}

}