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

import com.b2international.snowowl.core.store.query.RootType;
import com.b2international.snowowl.snomed.core.store.index.SnomedIndexConstants;

/**
 * @since 5.0
 */
public enum SnomedComponentType implements RootType {
	CONCEPT(SnomedIndexConstants.CONCEPT_OBJECT_TYPE);
	
	private final String name;

	private SnomedComponentType(String name) {
		this.name = checkNotNull(name, "name");
	}
	
	public String getName() {
		return this.name;
	}
}