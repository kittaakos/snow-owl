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

import com.b2international.snowowl.core.store.query.Feature;
import com.b2international.snowowl.core.store.query.Predicate;
import com.b2international.snowowl.core.store.query.Type;

/**
 * @since 5.0
 */
public class DescriptionTermPredicate extends Predicate {

	public static enum Operator {
		ALL, EXACT, ANY, NONE
	}

	private String text;
	private Operator operator;

	public DescriptionTermPredicate(Type type, Feature feature) {
		super(type, feature);
	}

	void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	public Operator getOperator() {
		return operator;
	}
}
