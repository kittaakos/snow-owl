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

import java.util.Comparator;

import com.b2international.snowowl.snomed.core.index.SnomedComponent;

/**
 * @since 5.0
 */
public class SnomedComponentComparator<T extends SnomedComponent> implements Comparator<T> {

	@Override
	public int compare(T o1, T o2) {
		int idComparison = CompareUtils.nullSafeCompare(o1.getId(), o2.getId());
		if (idComparison != 0)
			return idComparison;
		int activeComparison = Boolean.compare(o1.isActive(), o2.isActive());
		if (activeComparison != 0)
			return activeComparison;
		int releasedComparison = Boolean.compare(o1.isReleased(), o2.isReleased());
		if (releasedComparison != 0)
			return releasedComparison;
		int effectiveTimeComparison = CompareUtils.nullSafeCompare(o1.getEffectiveTime(), o2.getEffectiveTime());
		if (effectiveTimeComparison != 0)
			return effectiveTimeComparison;
		int moduleIdComparison = CompareUtils.nullSafeCompare(o1.getModuleId(), o2.getModuleId());
		if (moduleIdComparison != 0)
			return moduleIdComparison;
		return 0;
	}

}
