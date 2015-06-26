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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a concept label, including its type and acceptability.
 * 
 * @since 5.0
 */
public class Label {
	private String typeId;
	private String term;
	private List<String> preferredIds = new ArrayList<>();
	private List<String> acceptableIds = new ArrayList<>();

	public String getTypeId() {
		return typeId;
	}

	void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getTerm() {
		return term;
	}

	void setTerm(String term) {
		this.term = term;
	}

	public List<String> getPreferredIds() {
		return preferredIds;
	}

	void setPreferredIds(List<String> preferredIds) {
		this.preferredIds = preferredIds;
	}

	public List<String> getAcceptableIds() {
		return acceptableIds;
	}

	void setAcceptableIds(List<String> acceptableIds) {
		this.acceptableIds = acceptableIds;
	}

}
