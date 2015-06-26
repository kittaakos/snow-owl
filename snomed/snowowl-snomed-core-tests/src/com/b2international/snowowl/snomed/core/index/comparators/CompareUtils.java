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

import com.google.common.collect.Ordering;

/**
 * @since 5.0
 */
abstract public class CompareUtils {

	private CompareUtils() {
	}

	/**
	 * Compares two comparables, both of which are allowed to be null. Null
	 * values are considered less than non-null values.
	 * 
	 * @param o1
	 *            the first object to be compared
	 * @param o2
	 *            the second object to be compared
	 * @return the comparison result
	 */
	public static <T> int nullSafeCompare(Comparable<T> o1, Comparable<T> o2) {
		return Ordering.natural().nullsFirst().compare(o1, o2);
	}
}
