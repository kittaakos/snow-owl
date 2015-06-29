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

import com.b2international.snowowl.core.terminology.Component;

/**
 * Represents an MRCM predicate.
 * 
 * @since 5.0
 */
public class Predicate extends Component {

	public static enum Type {
		/** Relationship type predicate. */
		RELATIONSHIP,
		/** Description type predicate. */
		DESCRIPTION,
		/** Data type predicate. */
		DATATYPE;
	}

	public static enum RelationshipGroupRule {
		UNGROUPED, SINGLE_GROUP, ALL_GROUPS, MULTIPLE_GROUPS
	}

	private String domainExpression;
	private int min;
	private int max;
	private Type type;
	private String descriptionTypeId;
	private String relationshipTypeExpression;
	private String relationshipValueExpression;
	private String relationshipCharacteristicTypeExpression;
	private RelationshipGroupRule relationshipGroupRule;
	private String concreteDomainLabel;
	private String concreteDomainDisplayName;
	private ConcreteDomain.Type concreteDomainType;

	public String getDomainExpression() {
		return domainExpression;
	}

	void setDomainExpression(String domainExpression) {
		this.domainExpression = domainExpression;
	}

	public int getMin() {
		return min;
	}

	void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	void setMax(int max) {
		this.max = max;
	}

	public Type getType() {
		return type;
	}

	void setType(Type type) {
		this.type = type;
	}

	public String getDescriptionTypeId() {
		return descriptionTypeId;
	}

	void setDescriptionTypeId(String descriptionTypeId) {
		this.descriptionTypeId = descriptionTypeId;
	}

	public String getRelationshipTypeExpression() {
		return relationshipTypeExpression;
	}

	void setRelationshipTypeExpression(String relationshipTypeExpression) {
		this.relationshipTypeExpression = relationshipTypeExpression;
	}

	public String getRelationshipValueExpression() {
		return relationshipValueExpression;
	}

	void setRelationshipValueExpression(String relationshipValueExpression) {
		this.relationshipValueExpression = relationshipValueExpression;
	}

	public String getRelationshipCharacteristicTypeExpression() {
		return relationshipCharacteristicTypeExpression;
	}

	void setRelationshipCharacteristicTypeExpression(String relationshipCharacteristicTypeExpression) {
		this.relationshipCharacteristicTypeExpression = relationshipCharacteristicTypeExpression;
	}

	public RelationshipGroupRule getRelationshipGroupRule() {
		return relationshipGroupRule;
	}

	void setRelationshipGroupRule(RelationshipGroupRule relationshipGroupRule) {
		this.relationshipGroupRule = relationshipGroupRule;
	}

	public String getConcreteDomainLabel() {
		return concreteDomainLabel;
	}

	void setConcreteDomainLabel(String concreteDomainLabel) {
		this.concreteDomainLabel = concreteDomainLabel;
	}

	public String getConcreteDomainDisplayName() {
		return concreteDomainDisplayName;
	}

	void setConcreteDomainDisplayName(String concreteDomainDisplayName) {
		this.concreteDomainDisplayName = concreteDomainDisplayName;
	}

	public ConcreteDomain.Type getConcreteDomainType() {
		return concreteDomainType;
	}

	void setConcreteDomainType(ConcreteDomain.Type concreteDomainType) {
		this.concreteDomainType = concreteDomainType;
	}

}
