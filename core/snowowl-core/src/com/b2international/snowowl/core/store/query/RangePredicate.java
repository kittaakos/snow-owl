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
package com.b2international.snowowl.core.store.query;

import com.google.common.base.Optional;

/**
 * Represents a predicate with a value range.
 * 
 * @since 5.0
 * @param <T>
 *            the value type
 */
abstract public class RangePredicate<T> extends Predicate {

	private final Optional<T> start;
	private final Optional<T> end;
	private final boolean startInclusive;
	private final boolean endInclusive;

	/**
	 * Creates a range predicate with both start and end values included.
	 * 
	 * @param feature
	 *            - the feature to query
	 * @param start
	 *            - the range start value
	 * @param end
	 *            - the range end value
	 */
	public RangePredicate(Feature feature, T start, T end) {
		this(feature, start, end, true, true);
	}

	/**
	 * Creates a range predicate.
	 * 
	 * @param feature
	 *            - the feature to query
	 * @param start
	 *            - the range start value
	 * @param end
	 *            - the range end value
	 * @param startInclusive
	 *            - range start inclusiveness
	 * @param endInclusive
	 *            - range end inclusiveness
	 */
	public RangePredicate(Feature feature, T start, T end, boolean startInclusive, boolean endInclusive) {
		super(feature);
		this.start = Optional.fromNullable(start);
		this.end = Optional.fromNullable(end);
		this.startInclusive = startInclusive;
		this.endInclusive = endInclusive;
	}

	public Optional<T> getStart() {
		return start;
	}

	public Optional<T> getEnd() {
		return end;
	}

	public boolean isStartInclusive() {
		return startInclusive;
	}

	public boolean isEndInclusive() {
		return endInclusive;
	}
}
