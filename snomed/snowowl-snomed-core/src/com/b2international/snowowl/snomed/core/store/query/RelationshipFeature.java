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
public enum RelationshipFeature implements NestedFeature {
	ID(SnomedIndexConstants.ID),
	RELEASED(SnomedIndexConstants.RELEASED),
	ACTIVE(SnomedIndexConstants.ACTIVE), 
	EFFECTIVE_TIME(SnomedIndexConstants.EFFECTIVE_TIME),
	MODULE_ID(SnomedIndexConstants.MODULE_ID),
	GROUP(SnomedIndexConstants.GROUP),
	UNION_GROUP(SnomedIndexConstants.UNION_GROUP),
	TYPE_ID(SnomedIndexConstants.TYPE_ID),
	TYPE_ALL_ANCESTOR_IDS(SnomedIndexConstants.TYPE_ALL_ANCESTOR_IDS),
	DESTINATION_ID(SnomedIndexConstants.DESTINATION_ID),
	DESTINATION_ALL_ANCESTOR_IDS(SnomedIndexConstants.DESTINATION_ALL_ANCESTOR_IDS),
	CHARACTERISTIC_TYPE_ID(SnomedIndexConstants.CHARACTERISTIC_TYPE_ID),
	MODIFIER_ID(SnomedIndexConstants.MODIFIER_ID);
	
	private final String field;

	private RelationshipFeature(String field) {
		this.field = checkNotNull(field, "field");
	}
	
	@Override
	public String getField() {
		return getPath().getPath() + "." + field;
	}

	@Override
	public NestedPath getPath() {
		return ConceptNestedPath.RELATIONSHIPS;
	}
}