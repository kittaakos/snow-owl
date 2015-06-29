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
import java.util.List;

/**
 * @since 5.0
 */
public class ListComparator<T> implements Comparator<List<T>> {

	final private Comparator<T> elementComparator;

	public ListComparator(Comparator<T> elementComparator) {
		this.elementComparator = elementComparator;
	}

	@Override
	public int compare(List<T> o1, List<T> o2) {
		int sizeComparison = Integer.compare(o1.size(), o2.size());
		if (sizeComparison != 0)
			return sizeComparison;
		for (int i = 0; i < o1.size(); i++) {
			T e1 = o1.get(i);
			T e2 = o2.get(i);
			int elementComparison = elementComparator.compare(e1, e2);
			if (elementComparison != 0)
				return elementComparison;
		}
		return 0;
	}

}
