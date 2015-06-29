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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a concept.
 * 
 * @since 5.0
 */
public class Concept extends SnomedComponent {
	public static enum SubclassDisjointedness {
		DISJOINT, NON_DISJOINT
	}

	private String definitionStatusId;
	private SubclassDisjointedness subclassDisjointedness;
	private List<String> parentIds = new ArrayList<>();
	private List<String> ancestorIds = new ArrayList<>();
	private ReferenceSetType referenceSetType;
	private int referencedComponentType;
	private int mapTargetType;
	private List<ConcreteDomain> concreteDomains = new ArrayList<>();
	private List<Membership> memberships = new ArrayList<>();
	private List<Description> descriptions = new ArrayList<>();
	private List<RelationshipGroup> relationshipGroups = new ArrayList<>();

	public String getDefinitionStatusId() {
		return definitionStatusId;
	}

	void setDefinitionStatusId(String definitionStatusId) {
		this.definitionStatusId = definitionStatusId;
	}

	public SubclassDisjointedness getSubclassDisjointedness() {
		return subclassDisjointedness;
	}

	void setSubclassDisjointedness(SubclassDisjointedness subclassDisjointedness) {
		this.subclassDisjointedness = subclassDisjointedness;
	}

	public List<String> getParentIds() {
		return parentIds;
	}

	void setParentIds(List<String> parentIds) {
		this.parentIds = parentIds;
	}

	public List<String> getAncestorIds() {
		return ancestorIds;
	}

	void setAncestorIds(List<String> ancestorIds) {
		this.ancestorIds = ancestorIds;
	}

	public ReferenceSetType getReferenceSetType() {
		return referenceSetType;
	}

	void setReferenceSetType(ReferenceSetType referenceSetType) {
		this.referenceSetType = referenceSetType;
	}

	public int getReferencedComponentType() {
		return referencedComponentType;
	}

	void setReferencedComponentType(int referencedComponentType) {
		this.referencedComponentType = referencedComponentType;
	}

	public int getMapTargetType() {
		return mapTargetType;
	}

	void setMapTargetType(int mapTargetType) {
		this.mapTargetType = mapTargetType;
	}

	public List<ConcreteDomain> getConcreteDomains() {
		return concreteDomains;
	}

	void setConcreteDomains(List<ConcreteDomain> concreteDomains) {
		this.concreteDomains = concreteDomains;
	}

	public List<Membership> getMemberships() {
		return memberships;
	}

	void setMemberships(List<Membership> memberships) {
		this.memberships = memberships;
	}

	public List<Description> getDescriptions() {
		return descriptions;
	}

	void setDescriptions(List<Description> descriptions) {
		this.descriptions = descriptions;
	}

	public List<RelationshipGroup> getRelationshipGroups() {
		return relationshipGroups;
	}

	void setRelationshipGroups(List<RelationshipGroup> relationshipGroups) {
		this.relationshipGroups = relationshipGroups;
	}

}
