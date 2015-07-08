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
package com.b2international.snowowl.snomed.core.store.query;

import static com.google.common.base.Preconditions.checkNotNull;

import com.b2international.snowowl.core.store.query.NestedFeature;
import com.b2international.snowowl.core.store.query.NestedPath;
import com.b2international.snowowl.snomed.core.store.index.SnomedIndexConstants;

/**
 * @since 5.0
 */
public enum DescriptionMembershipFeature implements NestedFeature {
	ID(SnomedIndexConstants.ID),
	RELEASED(SnomedIndexConstants.RELEASED),
	ACTIVE(SnomedIndexConstants.ACTIVE), 
	EFFECTIVE_TIME(SnomedIndexConstants.EFFECTIVE_TIME),
	MODULE_ID(SnomedIndexConstants.MODULE_ID),
	TYPE(SnomedIndexConstants.TYPE),
	REFERENCE_SET_ID(SnomedIndexConstants.REFERENCE_SET_ID),
	REFERENCED_COMPONENT_ID(SnomedIndexConstants.REFERENCED_COMPONENT_ID),
	TARGET_COMPONENT_ID(SnomedIndexConstants.TARGET_COMPONENT_ID),
	VALUE_ID(SnomedIndexConstants.VALUE_ID),
	DESCRIPTION_FORMAT(SnomedIndexConstants.DESCRIPTION_FORMAT),
	DESCRIPTION_LENGTH(SnomedIndexConstants.DESCRIPTION_LENGTH),
	ACCEPTABILITY_ID(SnomedIndexConstants.ACCEPTABILITY_ID),
	MAP_TARGET(SnomedIndexConstants.MAP_TARGET),
	QUERY(SnomedIndexConstants.QUERY),
	SOURCE_EFFECTIVE_TIME(SnomedIndexConstants.SOURCE_EFFECTIVE_TIME),
	TARGET_EFFECTIVE_TIME(SnomedIndexConstants.TARGET_EFFECTIVE_TIME),
	MAP_GROUP(SnomedIndexConstants.MAP_GROUP),
	MAP_PRIORITY(SnomedIndexConstants.MAP_PRIORITY),
	MAP_RULE(SnomedIndexConstants.MAP_RULE),
	MAP_ADVICE(SnomedIndexConstants.MAP_ADVICE),
	CORRELATION_ID(SnomedIndexConstants.CORRELATION_ID),
	MAP_CATEGORY_ID(SnomedIndexConstants.MAP_CATEGORY_ID);

	private final String field;

	private DescriptionMembershipFeature(String field) {
		this.field = checkNotNull(field, "field");
	}
	
	@Override
	public String getField() {
		return getPath().getPath() + "." + field;
	}
	
	@Override
	public NestedPath getPath() {
		return DescriptionNestedPath.MEMBERSHIPS;
	}
}