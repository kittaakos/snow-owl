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

import com.b2international.snowowl.core.store.query.IndexConstants;

/**
 * @since 5.0
 */
public abstract class SnomedIndexConstants {

	// index name
	public static final String INDEX_NAME = "snomed";
	
	// types
	public static final String CONCEPT_OBJECT_TYPE = "concept";
	
	// component field
	public static final String ID = IndexConstants.ID;
	public static final String RELEASED = "released";
	public static final String ACTIVE = "active";
	public static final String EFFECTIVE_TIME = "effectiveTime";
	public static final String MODULE_ID = "moduleId";
	
	// concept fields
	public static final String DEFINITION_STATUS_ID = "definitionStatusId";
	public static final String SUBCLASS_DISJOINTEDNESS = "subclassDisjointedness";
	public static final String PARENT_IDS = "parentIds";
	public static final String ANCESTOR_IDS = "ancestorIds";
	public static final String REFERENCE_SET_TYPE = "referenceSetType";
	public static final String REFERENCED_COMPONENT_TYPE = "referencedComponentType";
	public static final String MAP_TARGET_TYPE = "mapTargetType";
	public static final String CONCRETE_DOMAINS = "concreteDomains";
	public static final String MEMBERSHIPS = "memberships";
	public static final String DESCRIPTIONS = "descriptions";
	public static final String RELATIONSHIP_GROUPS = "relationshipGroups";
	public static final String RELATIONSHIPS = "relationships";
	
	// description fields
	public static final String TYPE_ID = "typeId";
	public static final String LANGUAGE_CODE = "languageCode";
	public static final String CASE_SENSITIVITY_ID = "caseSensitivityId";
	public static final String TERM = "term";
	public static final String TERM_TOKENIZED = "term.tokenized";
	
	// relationship fields
	public static final String GROUP = "group";
	public static final String UNION_GROUP = "unionGroup";
	public static final String TYPE_ALL_ANCESTOR_IDS = "typeAllAncestorIds";
	public static final String DESTINATION_ALL_ANCESTOR_IDS = "destinationAllAncestorIds";
	public static final String DESTINATION_ID = "destinationId";
	public static final String CHARACTERISTIC_TYPE_ID = "characteristicTypeId";
	public static final String MODIFIER_ID = "modifierId";
	
	// membership fields
	public static final String REFERENCE_SET_ID = "referenceSetId";
	public static final String REFERENCED_COMPONENT_ID = "referencedComponentId";
	public static final String TARGET_COMPONENT_ID = "targetComponentId";
	public static final String ACCEPTABILITY_ID = "acceptabilityId";
	public static final String MAP_TARGET = "mapTarget";
	public static final String QUERY = "query";
	public static final String SOURCE_EFFECTIVE_TIME = "sourceEffectiveTime";
	public static final String TARGET_EFFECTIVE_TIME = "targetEffectiveTime";
	public static final String MAP_GROUP = "mapGroup";
	public static final String MAP_PRIORITY = "mapPriority";
	public static final String MAP_RULE = "mapRule";
	public static final String MAP_ADVICE = "mapAdvice";
	public static final String CORRELATION_ID = "correlationId";
	public static final String MAP_CATEGORY_ID = "mapCategoryId";
	public static final String VALUE_ID = "valueId";
	public static final String DESCRIPTION_FORMAT = "descriptionFormat";
	public static final String DESCRIPTION_LENGTH = "descriptionLength";
	
	// concrete domain
	public static final String LABEL = "label";
	public static final String VALUE_STRING = "valueString";
	public static final String VALUE_DECIMAL = "valueDecimal";
	public static final String VALUE_BOOLEAN = "valueBoolean";
	public static final String OPERATOR_ID = "operatorId";
	public static final String UOM_ID = "uomId";
	public static final String MEMBER_ID = "memberId";

	public static final String TYPE = "type";

	private SnomedIndexConstants() {}
}
