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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;

/**
 * Represents a binary operator.
 * 
 * @since 5.0
 */
abstract public class BinaryOperator implements Expression {
	private Expression left;
	private Expression right;
	private Type type;

	public BinaryOperator(Type type, Expression left, Expression right) {
		this.type = checkNotNull(type, "type");
		this.left = checkNotNull(left, "left");
		this.right = checkNotNull(right, "right");
		checkArgument(type.equals(left.getType()) && type.equals(right.getType()), 
				"Mismatched expression types: %s, %s, %s.", type, left.getType(), right.getType());
	}

	public Expression getLeft() {
		return left;
	}

	@JsonIgnore
	public Optional<Expression> getRight() {
		return Optional.fromNullable(right);
	}

	@Override
	public Type getType() {
		return type;
	}
}
