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
import com.b2international.snowowl.snomed.core.store.query.ConceptFeature;
import com.b2international.snowowl.snomed.core.store.query.ConceptNestedPath;
import com.b2international.snowowl.snomed.core.store.query.SnomedComponentFeature;
import com.b2international.snowowl.snomed.core.store.query.builder.ConcreteDomainExpressions.ConcreteDomainBinaryOperatorBuilder;
import com.b2international.snowowl.snomed.core.store.query.builder.DescriptionExpressions.DescriptionBinaryOperatorBuilder;
import com.b2international.snowowl.snomed.core.store.query.builder.RelationshipExpressions.RelationshipBinaryOperatorBuilder;

/**
 * @since 5.0
 */
abstract public class ConceptExpressions {
	public interface ConceptPredicateBuilder extends ComponentPredicateBuilder<ConceptBinaryOperatorBuilder> {
		ConceptBinaryOperatorBuilder definitionStatusId(String argument);
		ConceptBinaryOperatorBuilder hasDescription(DescriptionBinaryOperatorBuilder expressionBuilder);
		ConceptBinaryOperatorBuilder hasRelationship(RelationshipBinaryOperatorBuilder expressionBuilder);
		ConceptBinaryOperatorBuilder hasConcreteDomain(ConcreteDomainBinaryOperatorBuilder expressionBuilder);
		ConceptBinaryOperatorBuilder parent(String argument);
		ConceptBinaryOperatorBuilder ancestor(String argument);
		ConceptBinaryOperatorBuilder not(ConceptBinaryOperatorBuilder expressionBuilder);
	}
	
	public interface ConceptBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<ConceptBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface ConceptExpressionBuilder extends ConceptPredicateBuilder, ConceptBinaryOperatorBuilder {}
	
	private static class ConceptExpressionBuilderImpl implements ConceptExpressionBuilder {
		private Expression previous;
		
		@Override
		public ConceptBinaryOperatorBuilder id(String argument) {
			previous = new StringPredicate(SnomedComponentFeature.ID, argument);
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder moduleId(String argument) {
			previous = new StringPredicate(SnomedComponentFeature.MODULE_ID, argument);
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder active(boolean argument) {
			previous = new BooleanPredicate(SnomedComponentFeature.ACTIVE, argument);
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder released(boolean argument) {
			previous = new BooleanPredicate(SnomedComponentFeature.RELEASED, argument);
			return this;
		}

		public ConceptBinaryOperatorBuilder and(ConceptBinaryOperatorBuilder expressionBuilder) {
			previous = new And(previous, expressionBuilder.build());
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder or(ConceptBinaryOperatorBuilder expressionBuilder) {
			previous = new Or(previous, expressionBuilder.build());
			return this;
		}


		@Override
		public Expression build() {
			return previous;
		}

		@Override
		public ConceptBinaryOperatorBuilder not(ConceptBinaryOperatorBuilder expressionBuilder) {
			previous = new Not(expressionBuilder.build());
			return this;
		}
		
		@Override
		public ConceptBinaryOperatorBuilder definitionStatusId(String argument) {
			previous = new StringPredicate(ConceptFeature.DEFINITION_STATUS_ID, argument);
			return this;
		}
		
		@Override
		public ConceptBinaryOperatorBuilder parent(String argument) {
			previous = new StringPredicate(ConceptFeature.PARENTS, argument);
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder ancestor(String argument) {
			previous = new StringPredicate(ConceptFeature.ANCESTORS, argument);
			return this;
		}
		
		@Override
		public ConceptBinaryOperatorBuilder hasDescription(DescriptionBinaryOperatorBuilder expressionBuilder) {
			previous = new Same(ConceptNestedPath.DESCRIPTIONS, expressionBuilder.build());
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder hasRelationship(RelationshipBinaryOperatorBuilder expressionBuilder) {
			previous = new Same(ConceptNestedPath.RELATIONSHIPS, expressionBuilder.build());
			return this;
		}
		
		
		@Override
		public ConceptBinaryOperatorBuilder hasConcreteDomain(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			previous = new Same(ConceptNestedPath.CONCRETE_DOMAINS, expressionBuilder.build());
			return this;
		}
		
	}
	
	public static ConceptPredicateBuilder concept() {
		return new ConceptExpressionBuilderImpl();
	}
}
