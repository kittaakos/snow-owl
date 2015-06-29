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
package com.b2international.snowowl.snomed.core.store.index;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * Represents a reference set membership.
 * 
 * @since 5.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeIdResolver(MembershipTypeIdResolver.class)
abstract public class Membership extends SnomedComponent {
	@JsonIgnore
	private ReferenceSetType type;
	private String referenceSetId;
	private String referencedComponentId;

	protected Membership(ReferenceSetType type) {
		this.type = type;
	}

	public String getReferencedComponentId() {
		return referencedComponentId;
	}

	void setReferencedComponentId(String referencedComponentId) {
		this.referencedComponentId = referencedComponentId;
	}

	public String getReferenceSetId() {
		return referenceSetId;
	}

	void setReferenceSetId(String referenceSetId) {
		this.referenceSetId = referenceSetId;
	}

	public ReferenceSetType getType() {
		return type;
	}

	void setType(ReferenceSetType type) {
		this.type = type;
	}

}
