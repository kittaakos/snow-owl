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

import com.b2international.snowowl.core.store.query.Feature;
import com.b2international.snowowl.snomed.core.store.index.SnomedIndexConstants;

public enum ConcreteDomainFeature implements Feature {
	TYPE(SnomedIndexConstants.TYPE),
	LABEL(SnomedIndexConstants.LABEL),
	VALUE_STRING(SnomedIndexConstants.VALUE_STRING),
	VALUE_BOOLEAN(SnomedIndexConstants.VALUE_BOOLEAN),
	VALUE_DECIMAL(SnomedIndexConstants.VALUE_DECIMAL),
	CHARACTERISTIC_TYPE_ID(SnomedIndexConstants.CHARACTERISTIC_TYPE_ID),
	OPERATOR_ID(SnomedIndexConstants.OPERATOR_ID),
	UOM_ID(SnomedIndexConstants.UOM_ID),
	REFERENCE_SET_ID(SnomedIndexConstants.REFERENCE_SET_ID),
	MEMBER_ID(SnomedIndexConstants.MEMBER_ID);
	
	private final String field;

	private ConcreteDomainFeature(String field) {
		this.field = checkNotNull(field, "field");
	}
	
	@Override
	public String getField() {
		return field;
	}
}