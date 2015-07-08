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
 * Represents a relationship group.
 * 
 * @since 5.0
 */
public class RelationshipGroup {
	private int group;
	private List<Relationship> relationships = new ArrayList<>();

	public int getGroup() {
		return group;
	}

	void setGroup(int group) {
		this.group = group;
	}

	public List<Relationship> getRelationships() {
		return relationships;
	}

	void setRelationships(List<Relationship> relationships) {
		this.relationships = relationships;
	}

	public static RelationshipGroup of(Integer groupId) {
		final RelationshipGroup group = new RelationshipGroup();
		group.setGroup(groupId);
		return group;
	}

}
