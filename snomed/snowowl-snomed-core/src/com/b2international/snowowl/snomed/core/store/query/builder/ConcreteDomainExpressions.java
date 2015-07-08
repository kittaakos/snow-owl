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
import com.b2international.snowowl.core.store.query.Feature;
import com.b2international.snowowl.core.store.query.NestedPath;
import com.b2international.snowowl.core.store.query.Not;
import com.b2international.snowowl.core.store.query.Or;
import com.b2international.snowowl.core.store.query.StringPredicate;
import com.b2international.snowowl.snomed.core.store.query.ConcreteDomainFeature;
import com.b2international.snowowl.snomed.core.store.query.RelationshipConcreteDomainFeature;
import com.b2international.snowowl.snomed.core.store.query.RelationshipNestedPath;
import com.google.common.base.Optional;

/**
 * @since 5.0
 */
abstract public class ConcreteDomainExpressions {
	public interface ConcreteDomainPredicateBuilder extends ComponentPredicateBuilder<ConcreteDomainBinaryOperatorBuilder>, Buildable<Expression> {
		ConcreteDomainBinaryOperatorBuilder type(String argument);
		ConcreteDomainBinaryOperatorBuilder label(String argument);
		ConcreteDomainBinaryOperatorBuilder valueString(String argument);
		ConcreteDomainBinaryOperatorBuilder valueBoolean(String argument);
		ConcreteDomainBinaryOperatorBuilder valueDecimal(String argument);
		ConcreteDomainBinaryOperatorBuilder characteristicTypeId(String argument);
		ConcreteDomainBinaryOperatorBuilder operatorId(String argument);
		ConcreteDomainBinaryOperatorBuilder uomId(String argument);
		ConcreteDomainBinaryOperatorBuilder referenceSetId(String argument);
		ConcreteDomainBinaryOperatorBuilder memberId(String argument);
	}
	
	public interface ConcreteDomainBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<ConcreteDomainBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface ConcreteDomainExpressionBuilder extends ConcreteDomainPredicateBuilder, ConcreteDomainBinaryOperatorBuilder {}
	
	private static class ConcreteDomainExpressionBuilderImpl implements ConcreteDomainExpressionBuilder {
		
		private Optional<Expression> previous = Optional.absent();
		private final Optional<NestedPath> path;
		
		public ConcreteDomainExpressionBuilderImpl() {
			this.path = Optional.absent();
		}
		
		public ConcreteDomainExpressionBuilderImpl(NestedPath path) {
			this.path = Optional.fromNullable(path);
		}
		
		public ConcreteDomainBinaryOperatorBuilder and(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
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
		public ConcreteDomainBinaryOperatorBuilder id(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.ID), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder moduleId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.MODULE_ID), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder active(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(ensureNestedness(ConcreteDomainFeature.ACTIVE), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder released(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(ensureNestedness(ConcreteDomainFeature.RELEASED), argument));
			return this;
		}
		
		@Override
		public ConcreteDomainBinaryOperatorBuilder type(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.TYPE), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder label(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.LABEL), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder valueString(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.VALUE_STRING), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder valueBoolean(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.VALUE_BOOLEAN), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder valueDecimal(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.VALUE_DECIMAL), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder characteristicTypeId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.CHARACTERISTIC_TYPE_ID), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder operatorId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.OPERATOR_ID), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder uomId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.UOM_ID), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder referenceSetId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.REFERENCE_SET_ID), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder memberId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(ConcreteDomainFeature.MEMBER_ID), argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder or(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			Or or = new Or(previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(or);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder not(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Not(expressionBuilder.build()));
			return this;
		}
		
		private Feature ensureNestedness(ConcreteDomainFeature feature) {
			if (isNested()) {
				switch (feature) {
				case ID:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.ID;
					}
				case MODULE_ID:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.MODULE_ID;
					}
				case RELEASED:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.RELEASED;
					}
				case EFFECTIVE_TIME:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.EFFECTIVE_TIME;
					}
				case ACTIVE:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.ACTIVE;
					}
				case REFERENCE_SET_ID:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.REFERENCE_SET_ID;
					}
				case TYPE:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.TYPE;
					}
				case CHARACTERISTIC_TYPE_ID:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.CHARACTERISTIC_TYPE_ID;
					}
				case LABEL:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.LABEL;
					}
				case MEMBER_ID:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.MEMBER_ID;
					}
				case OPERATOR_ID:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.OPERATOR_ID;
					}
				case UOM_ID:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.UOM_ID;
					}
				case VALUE_BOOLEAN:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.VALUE_BOOLEAN;
					}
				case VALUE_DECIMAL:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.VALUE_DECIMAL;
					}
				case VALUE_STRING:
					if (isNestedInRelationship()) {
						return RelationshipConcreteDomainFeature.VALUE_STRING;
					}
				default:
					throw new IllegalArgumentException("Unexpected feature: " + feature);
				}
			} else {
				return feature;
			}
		}

		private boolean isNested() {
			return path.isPresent();
		}
		
		private boolean isNestedInRelationship() {
			return path.isPresent() ? RelationshipNestedPath.CONCRETE_DOMAINS.equals(path.get()) : false;
		}
	}
	
	public static ConcreteDomainPredicateBuilder concreteDomain() {
		return new ConcreteDomainExpressionBuilderImpl();
	}
	
	public static ConcreteDomainPredicateBuilder relationshipConcreteDomain() {
		return new ConcreteDomainExpressionBuilderImpl(RelationshipNestedPath.CONCRETE_DOMAINS);
	}
}
