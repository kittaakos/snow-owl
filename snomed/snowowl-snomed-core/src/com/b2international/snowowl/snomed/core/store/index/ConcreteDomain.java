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

/**
 * Represents a concrete domain.
 * 
 * @since 5.0
 */
public class ConcreteDomain extends SnomedComponent {

	public static enum Type {
		BOOLEAN, INTEGER, DECIMAL, DATE, STRING
	}

	private String label;
	private String valueString;
	private boolean valueBoolean;
	private String valueDecimal; // TODO: BigDecimal conversion in Java API?
	private String characteristicTypeId;
	private String operatorId;
	private String uomId;
	private String referenceSetId;
	private String memberId;
	private Type type;

	public String getLabel() {
		return label;
	}

	void setLabel(String label) {
		this.label = label;
	}

	public String getValueString() {
		return valueString;
	}

	void setValueString(String valueString) {
		this.valueString = valueString;
	}

	public boolean getValueBoolean() {
		return valueBoolean;
	}

	void setValueBoolean(boolean valueBoolean) {
		this.valueBoolean = valueBoolean;
	}

	public String getValueDecimal() {
		return valueDecimal;
	}

	void setValueDecimal(String valueDecimal) {
		this.valueDecimal = valueDecimal;
	}

	public String getCharacteristicTypeId() {
		return characteristicTypeId;
	}

	void setCharacteristicTypeId(String characteristicTypeId) {
		this.characteristicTypeId = characteristicTypeId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getUomId() {
		return uomId;
	}

	void setUomId(String uomId) {
		this.uomId = uomId;
	}

	public String getReferenceSetId() {
		return referenceSetId;
	}

	void setReferenceSetId(String referenceSetId) {
		this.referenceSetId = referenceSetId;
	}

	public String getMemberId() {
		return memberId;
	}

	void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public Type getType() {
		return type;
	}

	void setType(Type type) {
		this.type = type;
	}

}
