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
package com.b2international.snowowl.snomed.core.store.index.comparators;

import java.util.Comparator;

import com.b2international.snowowl.snomed.core.store.index.Predicate;

/**
 * @since 5.0
 */
public class PredicateComparator implements Comparator<Predicate> {

	@Override
	public int compare(Predicate o1, Predicate o2) {
		int idComparison = CompareUtils.nullSafeCompare(o1.getId(), o2.getId());
		if (idComparison != 0)
			return idComparison;
		int concreteDomainDisplayNameComparison = CompareUtils.nullSafeCompare(o1.getConcreteDomainDisplayName(), o2.getConcreteDomainDisplayName());
		if (concreteDomainDisplayNameComparison != 0)
			return concreteDomainDisplayNameComparison;
		int concreteDomainLabelComparison = CompareUtils.nullSafeCompare(o1.getConcreteDomainLabel(), o2.getConcreteDomainLabel());
		if (concreteDomainLabelComparison != 0)
			return concreteDomainLabelComparison;
		int concreteDomainTypeComparison = CompareUtils.nullSafeCompare(o1.getConcreteDomainType(), o2.getConcreteDomainType());
		if (concreteDomainTypeComparison != 0)
			return concreteDomainTypeComparison;
		int descriptionTypeIdComparison = CompareUtils.nullSafeCompare(o1.getDescriptionTypeId(), o2.getDescriptionTypeId());
		if (descriptionTypeIdComparison != 0)
			return descriptionTypeIdComparison;
		int domainExpressionComparison = CompareUtils.nullSafeCompare(o1.getDomainExpression(), o2.getDomainExpression());
		if (domainExpressionComparison != 0)
			return domainExpressionComparison;
		int maxComparison = CompareUtils.nullSafeCompare(o1.getMax(), o2.getMax());
		if (maxComparison != 0)
			return maxComparison;
		int minComparison = CompareUtils.nullSafeCompare(o1.getMin(), o2.getMin());
		if (minComparison != 0)
			return minComparison;
		int relCharTypeExpressionComparison = CompareUtils.nullSafeCompare(o1.getRelationshipCharacteristicTypeExpression(),
				o2.getRelationshipCharacteristicTypeExpression());
		if (relCharTypeExpressionComparison != 0)
			return relCharTypeExpressionComparison;
		int relationshipGroupRuleComparison = CompareUtils.nullSafeCompare(o1.getRelationshipGroupRule(), o2.getRelationshipGroupRule());
		if (relationshipGroupRuleComparison != 0)
			return relationshipGroupRuleComparison;
		int relationshipTypeExpressionComparison = CompareUtils.nullSafeCompare(o1.getRelationshipTypeExpression(),
				o2.getRelationshipTypeExpression());
		if (relationshipTypeExpressionComparison != 0)
			return relationshipTypeExpressionComparison;
		int relationshipValueExpressionComparison = CompareUtils.nullSafeCompare(o1.getRelationshipValueExpression(),
				o2.getRelationshipValueExpression());
		if (relationshipValueExpressionComparison != 0)
			return relationshipValueExpressionComparison;
		int typeComparison = CompareUtils.nullSafeCompare(o1.getType(), o2.getType());
		if (typeComparison != 0)
			return typeComparison;
		return 0;
	}

}
