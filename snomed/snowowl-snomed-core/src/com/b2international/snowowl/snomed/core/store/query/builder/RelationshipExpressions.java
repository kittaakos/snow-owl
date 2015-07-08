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
import com.b2international.snowowl.snomed.core.store.query.builder.ConcreteDomainExpressions.RelationshipConcreteDomainBinaryOperatorBuilder;
import com.b2international.snowowl.snomed.core.store.query.builder.MembershipExpressions.RelationshipMembershipBinaryOperatorBuilder;

/**
 * @since 5.0
 */
abstract public class RelationshipExpressions {
	public interface RelationshipPredicateBuilder extends ComponentPredicateBuilder<RelationshipBinaryOperatorBuilder> {
		RelationshipBinaryOperatorBuilder id(String argument);
		RelationshipBinaryOperatorBuilder moduleId(String argument);
		RelationshipBinaryOperatorBuilder typeId(String argument);
		RelationshipBinaryOperatorBuilder typeAncestorId(String argument); 
		RelationshipBinaryOperatorBuilder destinationId(String argument);
		RelationshipBinaryOperatorBuilder destinationAncestorId(String argument); 
		RelationshipBinaryOperatorBuilder modifierId(String argument);
		RelationshipBinaryOperatorBuilder characteristicTypeId(String argument);
		RelationshipBinaryOperatorBuilder hasMembership(RelationshipMembershipBinaryOperatorBuilder expressionBuilder);
		RelationshipBinaryOperatorBuilder hasConcreteDomain(RelationshipConcreteDomainBinaryOperatorBuilder expressionBuilder);
	}
	
	public interface RelationshipBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<RelationshipBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface RelationshipExpressionBuilder extends RelationshipPredicateBuilder, RelationshipBinaryOperatorBuilder {}
	
	private static class RelationshipExpressionBuilderImpl implements RelationshipExpressionBuilder {
		
		private Expression previous;
		
		public RelationshipBinaryOperatorBuilder and(RelationshipBinaryOperatorBuilder expressionBuilder) {
			previous = new And(previous, expressionBuilder.build());
			return this;
		}
		
		@Override
		public Expression build() {
			return previous;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder id(String argument) {
			previous = new StringPredicate(SnomedComponentFeature.ID, argument);
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder moduleId(String argument) {
			previous = new StringPredicate(SnomedComponentFeature.MODULE_ID, argument);
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder active(boolean argument) {
			previous = new BooleanPredicate(SnomedComponentFeature.ACTIVE, argument);
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder released(boolean argument) {
			previous = new BooleanPredicate(SnomedComponentFeature.RELEASED, argument);
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder typeId(String argument) {
			previous = new StringPredicate(RelationshipFeature.TYPE_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder typeAncestorId(String argument) {
			previous = new StringPredicate(RelationshipFeature.TYPE_ALL_ANCESTOR_IDS, argument);
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder destinationId(String argument) {
			previous = new StringPredicate(RelationshipFeature.DESTINATION_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder destinationAncestorId(String argument) {
			previous = new StringPredicate(RelationshipFeature.DESTINATION_ALL_ANCESTOR_IDS, argument);
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder modifierId(String argument) {
			previous = new StringPredicate(RelationshipFeature.MODIFIER_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder characteristicTypeId(String argument) {
			previous = new StringPredicate(RelationshipFeature.CHARACTERISTIC_TYPE_ID, argument);
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder hasMembership(RelationshipMembershipBinaryOperatorBuilder expressionBuilder) {
			previous = new Same(RelationshipNestedPath.MEMBERSHIPS, expressionBuilder.build());
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder hasConcreteDomain(RelationshipConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			previous = new Same(RelationshipNestedPath.CONCRETE_DOMAINS, expressionBuilder.build());
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder or(RelationshipBinaryOperatorBuilder expressionBuilder) {
			previous = new Or(previous, expressionBuilder.build());
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder not(RelationshipBinaryOperatorBuilder expressionBuilder) {
			previous = new Not(expressionBuilder.build());
			return this;
		}

	}
	
	public static RelationshipPredicateBuilder relationship() {
		return new RelationshipExpressionBuilderImpl();
	}
}
