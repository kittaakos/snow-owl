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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.b2international.snowowl.snomed.core.store.query.Type.NestedType;

/**
 * Unary operator to indicate that some predicates should apply to the same nested object.
 *
 * @since 5.0
 */
public class Same extends UnaryOperator {

	private NestedType nestedType;

	public Same(Type type, NestedType nestedType, Expression right) {
		super(type, right);
		this.nestedType = checkNotNull(nestedType, "nestedType");
		checkArgument(!nestedType.equals(type), "Type and nested type must not be the same: %s.", type);
		checkArgument(nestedType.equals(right.getType()), "Mismatched expression types: %s, %s.", nestedType, right.getType());
	}

	public NestedType getNestedType() {
		return nestedType;
	}
	
	@Override
	public String toString() {
		return "SAME " + getNestedType() + " " + getRight().toString();
	}

}
