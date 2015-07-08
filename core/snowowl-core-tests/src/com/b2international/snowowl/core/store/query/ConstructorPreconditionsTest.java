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

import org.junit.Test;

/**
 * @since 5.0
 */
public class ConstructorPreconditionsTest {

	private static final String TEST_STRING_ARGUMENT = "test";
	private static final Boolean TEST_BOOLEAN_ARGUMENT = true;
	private static final String TEST_TEXT_ARGUMENT = "test text argument";

	@Test(expected=NullPointerException.class)
	public void whenStringPredicateWithNullFeature_ThenThrowNPE() {
		new StringPredicate(null, TEST_STRING_ARGUMENT);
	}

	@Test(expected=NullPointerException.class)
	public void whenStringPredicateWithNullArgument_ThenThrowNPE() {
		new StringPredicate(MockFeature.FOO, null);
	}

	@Test(expected=NullPointerException.class)
	public void whenTextPredicateWithNullFeature_ThenThrowNPE() {
		new TextPredicate(null, TEST_TEXT_ARGUMENT, TextPredicate.Operator.ANY);
	}

	@Test(expected=NullPointerException.class)
	public void whenTextPredicateWithNullArgument_ThenThrowNPE() {
		new TextPredicate(MockFeature.FOO, null, TextPredicate.Operator.ANY);
	}
	
	@Test(expected=NullPointerException.class)
	public void whenTextPredicateWithNullOperator_ThenThrowNPE() {
		new TextPredicate(MockFeature.FOO, TEST_TEXT_ARGUMENT, null);
	}

	@Test(expected=NullPointerException.class)
	public void whenBooleanPredicateWithNullFeature_ThenThrowNPE() {
		new BooleanPredicate(null, TEST_BOOLEAN_ARGUMENT);
	}

	@Test(expected=NullPointerException.class)
	public void whenBooleanPredicateWithNullArgument_ThenThrowNPE() {
		new BooleanPredicate(MockFeature.FOO, null);
	}
	
	@Test(expected=NullPointerException.class)
	public void whenGroupWithNullContent_ThenThrowNPE() {
		new Group(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void whenAndWithNullLeftExpression_ThenThrowNPE() {
		new And(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void whenAndWithNullLeftExpressionNonNullRightExpression_ThenThrowNPE() {
		new And(null, MockExpression.INSTANCE);
	}
	
	@Test(expected=NullPointerException.class)
	public void whenOrWithNullLeftExpression_ThenThrowNPE() {
		new Or(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void whenOrWithNullLeftExpressionNonNullRightExpression_ThenThrowNPE() {
		new Or(null, MockExpression.INSTANCE);
	}
	
	@Test(expected=NullPointerException.class)
	public void whenSameWithNullPath_ThenThrowNPE() {
		new Same(null, MockExpression.INSTANCE);
	}
	
	@Test(expected=NullPointerException.class)
	public void whenSameWithNullExpression_ThenThrowNPE() {
		new Same(MockNestedPath.INSTANCE, null);
	}
}
