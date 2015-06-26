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
package com.b2international.snowowl.snomed.core.index;

/**
 * Represents a description type reference set membership.
 * 
 * @since 5.0
 */
public class DescriptionTypeMembership extends Membership {
	private String descriptionFormat;
	private int descriptionLength;

	public DescriptionTypeMembership() {
		super(ReferenceSetType.DESCRIPTION_TYPE);
	}

	public String getDescriptionFormat() {
		return descriptionFormat;
	}

	void setDescriptionFormat(String descriptionFormat) {
		this.descriptionFormat = descriptionFormat;
	}

	public int getDescriptionLength() {
		return descriptionLength;
	}

	void setDescriptionLength(int descriptionLength) {
		this.descriptionLength = descriptionLength;
	}

}
