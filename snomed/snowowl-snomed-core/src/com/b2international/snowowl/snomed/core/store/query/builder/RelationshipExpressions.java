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
import com.b2international.snowowl.core.store.query.Same;
import com.b2international.snowowl.core.store.query.StringPredicate;
import com.b2international.snowowl.snomed.core.store.query.RelationshipFeature;
import com.b2international.snowowl.snomed.core.store.query.RelationshipNestedPath;
import com.b2international.snowowl.snomed.core.store.query.SnomedComponentFeature;
import com.b2international.snowowl.snomed.core.store.query.builder.ConcreteDomainExpressions.ConcreteDomainBinaryOperatorBuilder;
import com.b2international.snowowl.snomed.core.store.query.builder.MembershipExpressions.MembershipBinaryOperatorBuilder;
import com.google.common.base.Optional;

/**
 * @since 5.0
 */
abstract public class RelationshipExpressions {
	public interface RelationshipPredicateBuilder extends ComponentPredicateBuilder<RelationshipBinaryOperatorBuilder>, Buildable<Expression> {
		RelationshipBinaryOperatorBuilder id(String argument);
		RelationshipBinaryOperatorBuilder moduleId(String argument);
		RelationshipBinaryOperatorBuilder typeId(String argument);
		RelationshipBinaryOperatorBuilder typeAncestorId(String argument); 
		RelationshipBinaryOperatorBuilder destinationId(String argument);
		RelationshipBinaryOperatorBuilder destinationAncestorId(String argument); 
		RelationshipBinaryOperatorBuilder modifierId(String argument);
		RelationshipBinaryOperatorBuilder characteristicTypeId(String argument);
		RelationshipBinaryOperatorBuilder hasMembership(MembershipBinaryOperatorBuilder expressionBuilder);
		RelationshipBinaryOperatorBuilder hasConcreteDomain(ConcreteDomainBinaryOperatorBuilder expressionBuilder);
	}
	
	public interface RelationshipBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<RelationshipBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface RelationshipExpressionBuilder extends RelationshipPredicateBuilder, RelationshipBinaryOperatorBuilder {}
	
	private static class RelationshipExpressionBuilderImpl implements RelationshipExpressionBuilder {
		
		private Optional<Expression> previous = Optional.absent();
		
		public RelationshipBinaryOperatorBuilder and(RelationshipBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			And and = new And(previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(and);
			return this;
		}
		
		@Override
		public Expression build() {
			return previous.get();
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder id(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(SnomedComponentFeature.ID, argument));
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder moduleId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(SnomedComponentFeature.MODULE_ID, argument));
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder active(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(SnomedComponentFeature.ACTIVE, argument));
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder released(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(SnomedComponentFeature.RELEASED, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder typeId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(RelationshipFeature.TYPE_ID, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder typeAncestorId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(RelationshipFeature.TYPE_ALL_ANCESTOR_IDS, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder destinationId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(RelationshipFeature.DESTINATION_ID, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder destinationAncestorId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(RelationshipFeature.DESTINATION_ALL_ANCESTOR_IDS, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder modifierId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(RelationshipFeature.MODIFIER_ID, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder characteristicTypeId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(RelationshipFeature.CHARACTERISTIC_TYPE_ID, argument));
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder hasMembership(MembershipBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Same(RelationshipNestedPath.MEMBERSHIPS, expressionBuilder.build()));
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder hasConcreteDomain(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Same(RelationshipNestedPath.CONCRETE_DOMAINS, expressionBuilder.build()));
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder or(RelationshipBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			Or or = new Or(previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(or);
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder not(RelationshipBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Not(expressionBuilder.build()));
			return this;
		}

	}
	
	public static RelationshipPredicateBuilder relationship() {
		return new RelationshipExpressionBuilderImpl();
	}
}
