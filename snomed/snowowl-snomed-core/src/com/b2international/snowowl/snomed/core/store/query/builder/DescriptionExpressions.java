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
package com.b2international.snowowl.snomed.core.store.query.builder;

import static com.b2international.snowowl.snomed.core.store.query.builder.MembershipExpressions.descriptionMembership;

import com.b2international.snowowl.core.store.query.And;
import com.b2international.snowowl.core.store.query.BooleanPredicate;
import com.b2international.snowowl.core.store.query.Buildable;
import com.b2international.snowowl.core.store.query.Expression;
import com.b2international.snowowl.core.store.query.Not;
import com.b2international.snowowl.core.store.query.Or;
import com.b2international.snowowl.core.store.query.Same;
import com.b2international.snowowl.core.store.query.StringPredicate;
import com.b2international.snowowl.core.store.query.TextPredicate;
import com.b2international.snowowl.snomed.core.store.query.DescriptionFeature;
import com.b2international.snowowl.snomed.core.store.query.DescriptionNestedPath;
import com.b2international.snowowl.snomed.core.store.query.SnomedComponentFeature;
import com.b2international.snowowl.snomed.core.store.query.builder.MembershipExpressions.DescriptionMembershipBinaryOperatorBuilder;

/**
 * @since 5.0
 */
abstract public class DescriptionExpressions {
	public interface DescriptionPredicateBuilder extends ComponentPredicateBuilder<DescriptionBinaryOperatorBuilder> {
		DescriptionBinaryOperatorBuilder id(String argument);
		DescriptionBinaryOperatorBuilder moduleId(String argument);
		DescriptionBinaryOperatorBuilder typeId(String argument);
		DescriptionBinaryOperatorBuilder caseSensitivityId(String argument);
		DescriptionBinaryOperatorBuilder term(String argument, TextPredicate.Operator operator);
		DescriptionBinaryOperatorBuilder preferredIn(String argument);
		DescriptionBinaryOperatorBuilder acceptableIn(String argument);
		DescriptionBinaryOperatorBuilder hasMembership(DescriptionMembershipBinaryOperatorBuilder expressionBuilder);
	}
	
	public interface DescriptionBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<DescriptionBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface DescriptionExpressionBuilder extends DescriptionPredicateBuilder, DescriptionBinaryOperatorBuilder {}
	
	private static class DescriptionExpressionBuilderImpl implements DescriptionExpressionBuilder {
		private static final String ACCEPTABLE_ID = "900000000000549004";
		private static final String PREFERRED_ID = "900000000000548007";
		
		private Expression previous;
		
		public DescriptionBinaryOperatorBuilder and(DescriptionBinaryOperatorBuilder expressionBuilder) {
			previous = new And(previous, expressionBuilder.build());
			return this;
		}
		
		@Override
		public Expression build() {
			return previous;
		}
		
		@Override
		public DescriptionBinaryOperatorBuilder id(String argument) {
			previous = new StringPredicate(DescriptionFeature.ID, argument);
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder moduleId(String argument) {
			previous = new StringPredicate(DescriptionFeature.MODULE_ID, argument);
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder active(boolean argument) {
			previous = new BooleanPredicate(DescriptionFeature.ACTIVE, argument);
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder released(boolean argument) {
			previous = new BooleanPredicate(DescriptionFeature.RELEASED, argument);
			return this;
		}
		
		@Override
		public DescriptionBinaryOperatorBuilder typeId(String argument) {
			previous = new StringPredicate(DescriptionFeature.TYPE_ID, argument);
			return this;
		}
		
		@Override
		public DescriptionBinaryOperatorBuilder caseSensitivityId(String argument) {
			previous = new StringPredicate(DescriptionFeature.CASE_SENSITIVITY_ID, argument);
			return this;
		}
		
		@Override
		public DescriptionBinaryOperatorBuilder term(String argument, TextPredicate.Operator operator) {
			previous = new TextPredicate(DescriptionFeature.TERM, argument, operator);
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder preferredIn(String argument) {
			return hasMembership(descriptionMembership().referenceSetId(argument).and(descriptionMembership().acceptabilityId(PREFERRED_ID)));
		}

		@Override
		public DescriptionBinaryOperatorBuilder acceptableIn(String argument) {
			return hasMembership(descriptionMembership().referenceSetId(argument).and(descriptionMembership().acceptabilityId(ACCEPTABLE_ID)));
		}

		@Override
		public DescriptionBinaryOperatorBuilder hasMembership(DescriptionMembershipBinaryOperatorBuilder expressionBuilder) {
			previous = new Same(DescriptionNestedPath.MEMBERSHIPS, expressionBuilder.build());
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder or(DescriptionBinaryOperatorBuilder expressionBuilder) {
			previous = new Or(previous, expressionBuilder.build());
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder not(DescriptionBinaryOperatorBuilder expressionBuilder) {
			previous = new Not(expressionBuilder.build());
			return this;
		}
		
	}
	
	public static DescriptionPredicateBuilder description() {
		return new DescriptionExpressionBuilderImpl();
	}
}
