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
package com.b2international.snowowl.snomed.core.index.comparators;

import com.b2international.snowowl.snomed.core.index.ConcreteDomain;

/**
 * @since 5.0
 */
public class ConcreteDomainComparator extends SnomedComponentComparator<ConcreteDomain> {

	@Override
	public int compare(ConcreteDomain o1, ConcreteDomain o2) {
		int snomedComponentComparison = super.compare(o1, o2);
		if (snomedComponentComparison != 0)
			return snomedComponentComparison;
		int characteristicTypeIdComparison = CompareUtils.nullSafeCompare(o1.getCharacteristicTypeId(), o2.getCharacteristicTypeId());
		if (characteristicTypeIdComparison != 0)
			return characteristicTypeIdComparison;
		int labelComparison = CompareUtils.nullSafeCompare(o1.getLabel(), o2.getLabel());
		if (labelComparison != 0)
			return labelComparison;
		int memberIdComparison = CompareUtils.nullSafeCompare(o1.getMemberId(), o2.getMemberId());
		if (memberIdComparison != 0)
			return memberIdComparison;
		int operatorIdComparison = CompareUtils.nullSafeCompare(o1.getOperatorId(), o2.getOperatorId());
		if (operatorIdComparison != 0)
			return operatorIdComparison;
		int referenceSetIdComparison = CompareUtils.nullSafeCompare(o1.getReferenceSetId(), o2.getReferenceSetId());
		if (referenceSetIdComparison != 0)
			return referenceSetIdComparison;
		int typeComparison = CompareUtils.nullSafeCompare(o1.getType(), o2.getType());
		if (typeComparison != 0)
			return typeComparison;
		int uomIdComparison = CompareUtils.nullSafeCompare(o1.getUomId(), o2.getUomId());
		if (uomIdComparison != 0)
			return uomIdComparison;
		int valueDecimalComparison = CompareUtils.nullSafeCompare(o1.getValueDecimal(), o2.getValueDecimal());
		if (valueDecimalComparison != 0)
			return valueDecimalComparison;
		int valueStringComparison = CompareUtils.nullSafeCompare(o1.getValueString(), o2.getValueString());
		if (valueStringComparison != 0)
			return valueStringComparison;
		int valueBooleanComparison = CompareUtils.nullSafeCompare(o1.getValueBoolean(), o2.getValueBoolean());
		if (valueBooleanComparison != 0)
			return valueBooleanComparison;
		return 0;
	}

}
