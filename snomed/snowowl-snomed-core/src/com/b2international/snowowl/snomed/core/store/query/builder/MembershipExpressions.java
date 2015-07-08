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

import com.b2international.snowowl.core.store.query.And;
import com.b2international.snowowl.core.store.query.BooleanPredicate;
import com.b2international.snowowl.core.store.query.Buildable;
import com.b2international.snowowl.core.store.query.Expression;
import com.b2international.snowowl.core.store.query.Not;
import com.b2international.snowowl.core.store.query.Or;
import com.b2international.snowowl.core.store.query.StringPredicate;
import com.b2international.snowowl.snomed.core.store.query.DescriptionMembershipFeature;
import com.b2international.snowowl.snomed.core.store.query.MembershipFeature;
import com.b2international.snowowl.snomed.core.store.query.RelationshipMembershipFeature;

/**
 * @since 5.0
 */
abstract public class MembershipExpressions {
	public interface MembershipPredicateBuilder extends ComponentPredicateBuilder<MembershipBinaryOperatorBuilder> {
		MembershipBinaryOperatorBuilder type(String argument);
		MembershipBinaryOperatorBuilder acceptabilityId(String argument);
		MembershipBinaryOperatorBuilder referenceSetId(String argument);
		MembershipBinaryOperatorBuilder referencedComponentId(String argument);
	}
	
	public interface MembershipBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<MembershipBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface MembershipExpressionBuilder extends MembershipPredicateBuilder, MembershipBinaryOperatorBuilder {}
	
	private static final class MembershipExpressionBuilderImpl implements MembershipExpressionBuilder {
		private Expression previous;
		
		@Override
		public MembershipBinaryOperatorBuilder id(String argument) {
			previous = new StringPredicate(MembershipFeature.ID, argument);
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder moduleId(String argument) {
			previous = new StringPredicate(MembershipFeature.MODULE_ID, argument);
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder active(boolean argument) {
			previous = new BooleanPredicate(MembershipFeature.ACTIVE, argument);
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder released(boolean argument) {
			previous = new BooleanPredicate(MembershipFeature.RELEASED, argument);
			return this;
		}
		
		@Override
		public MembershipBinaryOperatorBuilder type(String argument) {
			previous = new StringPredicate(MembershipFeature.TYPE, argument);
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder referenceSetId(String argument) {
			previous = new StringPredicate(MembershipFeature.REFERENCE_SET_ID, argument);
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder referencedComponentId(String argument) {
			previous = new StringPredicate(MembershipFeature.REFERENCED_COMPONENT_ID, argument);
			return this;
		}
		
		@Override
		public MembershipBinaryOperatorBuilder acceptabilityId(String argument) {
			previous = new StringPredicate(MembershipFeature.ACCEPTABILITY_ID, argument);
			return this;
		}

		@Override
		public Expression build() {
			return previous;
		}

		@Override
		public MembershipBinaryOperatorBuilder and(MembershipBinaryOperatorBuilder expressionBuilder) {
			previous = new And(previous, expressionBuilder.build());
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder or(MembershipBinaryOperatorBuilder expressionBuilder) {
			previous = new Or(previous, expressionBuilder.build());
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder not(MembershipBinaryOperatorBuilder expressionBuilder) {
			previous = new Not(expressionBuilder.build());
			return this;
		}
		
	}
	
	public interface DescriptionMembershipPredicateBuilder extends ComponentPredicateBuilder<DescriptionMembershipBinaryOperatorBuilder>, Buildable<Expression> {
		DescriptionMembershipBinaryOperatorBuilder type(String argument);
		DescriptionMembershipBinaryOperatorBuilder acceptabilityId(String argument);
		DescriptionMembershipBinaryOperatorBuilder referenceSetId(String argument);
		DescriptionMembershipBinaryOperatorBuilder referencedComponentId(String argument);
	}

	public interface DescriptionMembershipBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<DescriptionMembershipBinaryOperatorBuilder>, Buildable<Expression> {}

	public interface DescriptionMembershipExpressionBuilder extends DescriptionMembershipPredicateBuilder, DescriptionMembershipBinaryOperatorBuilder {}

	private static final class DescriptionMembershipExpressionBuilderImpl implements DescriptionMembershipExpressionBuilder {
		private Expression previous;
		
		@Override
		public DescriptionMembershipBinaryOperatorBuilder id(String argument) {
			previous = new StringPredicate(DescriptionMembershipFeature.ID, argument);
			return this;
		}
	
		@Override
		public DescriptionMembershipBinaryOperatorBuilder moduleId(String argument) {
			previous = new StringPredicate(DescriptionMembershipFeature.MODULE_ID, argument);
			return this;
		}
	
		@Override
		public DescriptionMembershipBinaryOperatorBuilder active(boolean argument) {
			previous = new BooleanPredicate(DescriptionMembershipFeature.ACTIVE, argument);
			return this;
		}
	
		@Override
		public DescriptionMembershipBinaryOperatorBuilder released(boolean argument) {
			previous = new BooleanPredicate(DescriptionMembershipFeature.RELEASED, argument);
			return this;
		}
		
		@Override
		public DescriptionMembershipBinaryOperatorBuilder type(String argument) {
			previous = new StringPredicate(DescriptionMembershipFeature.TYPE, argument);
			return this;
		}
	
		@Override
		public DescriptionMembershipBinaryOperatorBuilder referenceSetId(String argument) {
			previous = new StringPredicate(DescriptionMembershipFeature.REFERENCE_SET_ID, argument);
			return this;
		}
	
		@Override
		public DescriptionMembershipBinaryOperatorBuilder referencedComponentId(String argument) {
			previous = new StringPredicate(DescriptionMembershipFeature.REFERENCED_COMPONENT_ID, argument);
			return this;
		}
		
		@Override
		public DescriptionMembershipBinaryOperatorBuilder acceptabilityId(String argument) {
			previous = new StringPredicate(DescriptionMembershipFeature.ACCEPTABILITY_ID, argument);
			return this;
		}
	
		@Override
		public Expression build() {
			return previous;
		}
	
		@Override
		public DescriptionMembershipBinaryOperatorBuilder and(DescriptionMembershipBinaryOperatorBuilder expressionBuilder) {
			previous = new And(previous, expressionBuilder.build());
			return this;
		}
	
		@Override
		public DescriptionMembershipBinaryOperatorBuilder or(DescriptionMembershipBinaryOperatorBuilder expressionBuilder) {
			previous = new Or(previous, expressionBuilder.build());
			return this;
		}
	
		@Override
		public DescriptionMembershipBinaryOperatorBuilder not(DescriptionMembershipBinaryOperatorBuilder expressionBuilder) {
			previous = new Not(expressionBuilder.build());
			return this;
		}
		
	}

	public interface RelationshipMembershipPredicateBuilder extends ComponentPredicateBuilder<RelationshipMembershipBinaryOperatorBuilder>, Buildable<Expression> {
		RelationshipMembershipBinaryOperatorBuilder type(String argument);
		RelationshipMembershipBinaryOperatorBuilder acceptabilityId(String argument);
		RelationshipMembershipBinaryOperatorBuilder referenceSetId(String argument);
		RelationshipMembershipBinaryOperatorBuilder referencedComponentId(String argument);
	}
	
	public interface RelationshipMembershipBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<RelationshipMembershipBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface RelationshipMembershipExpressionBuilder extends RelationshipMembershipPredicateBuilder, RelationshipMembershipBinaryOperatorBuilder {}
	
	private static final class RelationshipMembershipExpressionBuilderImpl implements RelationshipMembershipExpressionBuilder {
		private Expression previous;
		
		@Override
		public RelationshipMembershipBinaryOperatorBuilder id(String argument) {
			previous = new StringPredicate(RelationshipMembershipFeature.ID, argument);
			return this;
		}
		
		@Override
		public RelationshipMembershipBinaryOperatorBuilder moduleId(String argument) {
			previous = new StringPredicate(RelationshipMembershipFeature.MODULE_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipMembershipBinaryOperatorBuilder active(boolean argument) {
			previous = new BooleanPredicate(RelationshipMembershipFeature.ACTIVE, argument);
			return this;
		}
		
		@Override
		public RelationshipMembershipBinaryOperatorBuilder released(boolean argument) {
			previous = new BooleanPredicate(RelationshipMembershipFeature.RELEASED, argument);
			return this;
		}
		
		@Override
		public RelationshipMembershipBinaryOperatorBuilder type(String argument) {
			previous = new StringPredicate(RelationshipMembershipFeature.TYPE, argument);
			return this;
		}
		
		@Override
		public RelationshipMembershipBinaryOperatorBuilder referenceSetId(String argument) {
			previous = new StringPredicate(RelationshipMembershipFeature.REFERENCE_SET_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipMembershipBinaryOperatorBuilder referencedComponentId(String argument) {
			previous = new StringPredicate(RelationshipMembershipFeature.REFERENCED_COMPONENT_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipMembershipBinaryOperatorBuilder acceptabilityId(String argument) {
			previous = new StringPredicate(RelationshipMembershipFeature.ACCEPTABILITY_ID, argument);
			return this;
		}
		
		@Override
		public Expression build() {
			return previous;
		}
		
		@Override
		public RelationshipMembershipBinaryOperatorBuilder and(RelationshipMembershipBinaryOperatorBuilder expressionBuilder) {
			previous = new And(previous, expressionBuilder.build());
			return this;
		}
		
		@Override
		public RelationshipMembershipBinaryOperatorBuilder or(RelationshipMembershipBinaryOperatorBuilder expressionBuilder) {
			previous = new Or(previous, expressionBuilder.build());
			return this;
		}
		
		@Override
		public RelationshipMembershipBinaryOperatorBuilder not(RelationshipMembershipBinaryOperatorBuilder expressionBuilder) {
			previous = new Not(expressionBuilder.build());
			return this;
		}
		
	}
	
	public static MembershipPredicateBuilder membership() {
		return new MembershipExpressionBuilderImpl();
	}
	
	public static DescriptionMembershipPredicateBuilder descriptionMembership() {
		return new DescriptionMembershipExpressionBuilderImpl();
	}
	
	public static RelationshipMembershipPredicateBuilder relationshipMembership() {
		return new RelationshipMembershipExpressionBuilderImpl();
	}
}
