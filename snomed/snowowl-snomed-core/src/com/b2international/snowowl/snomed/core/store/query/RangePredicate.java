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

import com.google.common.base.Optional;

/**
 * Represents a predicate with a value range.
 * 
 * @since 5.0
 * @param <T> the value type
 */
abstract public class RangePredicate<T> extends Predicate {

	private T start;
	private T end;
	
	public RangePredicate(Type type, Feature feature) {
		super(type, feature);
	}

	public Optional<T> getStart() {
		return Optional.fromNullable(start);
	}
	
	void setStart(T start) {
		this.start = start;
	}
	
	public Optional<T> getEnd() {
		return Optional.fromNullable(end);
	}
	
	void setEnd(T end) {
		this.end = end;
	}
}
