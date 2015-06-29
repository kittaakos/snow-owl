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

import java.util.Date;

import com.b2international.snowowl.core.terminology.Component;

/**
 * Represents a SNOMED CT component.
 * 
 * @since 5.0
 */
abstract public class SnomedComponent extends Component {
	private boolean released;
	private boolean active;
	private Date effectiveTime;
	private String moduleId;

	public boolean isReleased() {
		return released;
	}

	void setReleased(boolean released) {
		this.released = released;
	}

	public boolean isActive() {
		return active;
	}

	void setActive(boolean active) {
		this.active = active;
	}

	public Date getEffectiveTime() {
		return effectiveTime;
	}

	void setEffectiveTime(Date effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public String getModuleId() {
		return moduleId;
	}

	void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

}
