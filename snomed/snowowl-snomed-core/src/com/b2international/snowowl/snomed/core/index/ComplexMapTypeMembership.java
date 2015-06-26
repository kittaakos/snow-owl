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
 * Represents a complex map type reference set membership.
 * 
 * @since 5.0
 */
public class ComplexMapTypeMembership extends Membership {
	private String mapTarget;
	private byte mapGroup;
	private byte mapPriority;
	private String mapRule;
	private String mapAdvice;
	private String correlationId;

	public ComplexMapTypeMembership() {
		super(ReferenceSetType.COMPLEX_MAP);
	}

	public String getMapTarget() {
		return mapTarget;
	}

	void setMapTarget(String mapTarget) {
		this.mapTarget = mapTarget;
	}

	public byte getMapGroup() {
		return mapGroup;
	}

	void setMapGroup(byte mapGroup) {
		this.mapGroup = mapGroup;
	}

	public byte getMapPriority() {
		return mapPriority;
	}

	void setMapPriority(byte mapPriority) {
		this.mapPriority = mapPriority;
	}

	public String getMapRule() {
		return mapRule;
	}

	void setMapRule(String mapRule) {
		this.mapRule = mapRule;
	}

	public String getMapAdvice() {
		return mapAdvice;
	}

	void setMapAdvice(String mapAdvice) {
		this.mapAdvice = mapAdvice;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

}
