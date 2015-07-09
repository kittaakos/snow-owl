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

import static com.google.common.base.Preconditions.checkNotNull;



/**
 * Unary operator to indicate that some predicates should apply to the same nested object.
 *
 * @since 5.0
 */
public class Same extends UnaryOperator {

	private final NestedPath path;

	public Same(NestedPath path, Expression right) {
		super(right);
		this.path = checkNotNull(path, "path");
	}

	public NestedPath getPath() {
		return path;
	}
	
	@Override
	public String toString() {
		return "SAME " + path + " [" + getRight().toString() + "]";
	}

}
