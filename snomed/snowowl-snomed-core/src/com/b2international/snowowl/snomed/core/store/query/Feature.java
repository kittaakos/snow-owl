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

/**
 * @since 5.0
 */
public interface Feature {
	String getName();
	
	public enum MatchAllFeature implements Feature {
		INSTANCE;
		
		@Override
		public String getName() {
			return name();
		}
	}

	public enum ComponentFeature implements Feature {
		ID,
		RELEASED,
		ACTIVE, 
		EFFECTIVE_TIME,
		MODULE_ID;

		@Override
		public String getName() {
			return name();
		}
	}
	
	public enum ConceptFeature implements Feature {
		PARENTS,
		ANCESTORS,
		CHILDREN,
		DESCENDANTS,
		DEFINITION_STATUS_ID,
		SUBCLASS_DISJOINTEDNESS,
		REFERENCE_SET_TYPE,
		REFERENCED_COMPONENT_TYPE,
		MAP_TARGET_TYPE;

		@Override
		public String getName() {
			return name();
		}

	}
	
	public enum DescriptionFeature implements Feature {
		TYPE_ID,
		LANGUAGE_CODE,
		CASE_SENSITIVITY_ID,
		TERM,
		NORMALIZED_TERM;
		
		@Override
		public String getName() {
			return name();
		}
		
	}
	
	public enum RelationshipFeature implements Feature {
		GROUP,
		UNION_GROUP,
		TYPE_ID,
		TYPE_ALL_ANCESTOR_IDS,
		DESTINATION_ID,
		DESTINATION_ALL_ANCESTOR_IDS,
		CHARACTERISTIC_TYPE_ID,
		MODIFIER_ID;
		
		@Override
		public String getName() {
			return name();
		}
		
	}
	
	public enum MembershipFeature implements Feature {
		TYPE,
		REFERENCE_SET_ID,
		REFERENCED_COMPONENT_ID,
		ACCEPTABILITY_ID;
		
		@Override
		public String getName() {
			return name();
		}
	}
	
	public enum ConcreteDomainFeature implements Feature {
		TYPE,
		LABEL,
		VALUE_STRING,
		VALUE_BOOLEAN,
		VALUE_DECIMAL,
		CHARACTERISTIC_TYPE_ID,
		OPERATOR_ID,
		UOM_ID,
		REFERENCE_SET_ID,
		MEMBER_ID;
		
		@Override
		public String getName() {
			return name();
		}
	}
}
