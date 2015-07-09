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

import java.util.Date;

import com.b2international.snowowl.core.store.query.And;
import com.b2international.snowowl.core.store.query.BooleanPredicate;
import com.b2international.snowowl.core.store.query.Buildable;
import com.b2international.snowowl.core.store.query.DateRangePredicate;
import com.b2international.snowowl.core.store.query.Expression;
import com.b2international.snowowl.core.store.query.Not;
import com.b2international.snowowl.core.store.query.Or;
import com.b2international.snowowl.core.store.query.StringPredicate;
import com.b2international.snowowl.snomed.core.store.query.ConcreteDomainFeature;
import com.b2international.snowowl.snomed.core.store.query.RelationshipConcreteDomainFeature;

/**
 * @since 5.0
 */
abstract public class ConcreteDomainExpressions {
	public interface ConcreteDomainPredicateBuilder extends ComponentPredicateBuilder<ConcreteDomainBinaryOperatorBuilder> {
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
		
		private Expression previous;
		
		public ConcreteDomainBinaryOperatorBuilder and(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			previous = new And(previous, expressionBuilder.build());
			return this;
		}
		
		@Override
		public Expression build() {
			return previous;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder id(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.ID, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder moduleId(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.MODULE_ID, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder active(boolean argument) {
			previous = new BooleanPredicate(ConcreteDomainFeature.ACTIVE, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder released(boolean argument) {
			previous = new BooleanPredicate(ConcreteDomainFeature.RELEASED, argument);
			return this;
		}
		
		@Override
		public ConcreteDomainBinaryOperatorBuilder effectiveTimeBetween(Date from, Date to) {
			previous = new DateRangePredicate(ConcreteDomainFeature.EFFECTIVE_TIME, from, to);
			return this;
		}
		
		@Override
		public ConcreteDomainBinaryOperatorBuilder effectiveTimeBefore(Date date) {
			previous = new DateRangePredicate(ConcreteDomainFeature.EFFECTIVE_TIME, null, date);
			return this;
		}
		
		@Override
		public ConcreteDomainBinaryOperatorBuilder effectiveTimeAfter(Date date) {
			previous = new DateRangePredicate(ConcreteDomainFeature.EFFECTIVE_TIME, date, null);
			return this;
		}
		@Override
		public ConcreteDomainBinaryOperatorBuilder type(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.TYPE, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder label(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.LABEL, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder valueString(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.VALUE_STRING, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder valueBoolean(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.VALUE_BOOLEAN, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder valueDecimal(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.VALUE_DECIMAL, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder characteristicTypeId(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.CHARACTERISTIC_TYPE_ID, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder operatorId(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.OPERATOR_ID, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder uomId(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.UOM_ID, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder referenceSetId(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.REFERENCE_SET_ID, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder memberId(String argument) {
			previous = new StringPredicate(ConcreteDomainFeature.MEMBER_ID, argument);
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder or(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			previous = new Or(previous, expressionBuilder.build());
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder not(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			previous = new Not(expressionBuilder.build());
			return this;
		}
	}
	
	public interface RelationshipConcreteDomainPredicateBuilder extends ComponentPredicateBuilder<RelationshipConcreteDomainBinaryOperatorBuilder>, Buildable<Expression> {
		RelationshipConcreteDomainBinaryOperatorBuilder type(String argument);
		RelationshipConcreteDomainBinaryOperatorBuilder label(String argument);
		RelationshipConcreteDomainBinaryOperatorBuilder valueString(String argument);
		RelationshipConcreteDomainBinaryOperatorBuilder valueBoolean(String argument);
		RelationshipConcreteDomainBinaryOperatorBuilder valueDecimal(String argument);
		RelationshipConcreteDomainBinaryOperatorBuilder characteristicTypeId(String argument);
		RelationshipConcreteDomainBinaryOperatorBuilder operatorId(String argument);
		RelationshipConcreteDomainBinaryOperatorBuilder uomId(String argument);
		RelationshipConcreteDomainBinaryOperatorBuilder referenceSetId(String argument);
		RelationshipConcreteDomainBinaryOperatorBuilder memberId(String argument);
	}
	
	public interface RelationshipConcreteDomainBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<RelationshipConcreteDomainBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface RelationshipConcreteDomainExpressionBuilder extends RelationshipConcreteDomainPredicateBuilder, RelationshipConcreteDomainBinaryOperatorBuilder {}
	
	private static class RelationshipConcreteDomainExpressionBuilderImpl implements RelationshipConcreteDomainExpressionBuilder {
		
		private Expression previous;
		
		public RelationshipConcreteDomainBinaryOperatorBuilder and(RelationshipConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			previous = new And(previous, expressionBuilder.build());
			return this;
		}
		
		@Override
		public Expression build() {
			return previous;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder id(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.ID, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder moduleId(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.MODULE_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder active(boolean argument) {
			previous = new BooleanPredicate(RelationshipConcreteDomainFeature.ACTIVE, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder released(boolean argument) {
			previous = new BooleanPredicate(RelationshipConcreteDomainFeature.RELEASED, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder effectiveTimeBetween(Date from, Date to) {
			previous = new DateRangePredicate(RelationshipConcreteDomainFeature.EFFECTIVE_TIME, from, to);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder effectiveTimeBefore(Date date) {
			previous = new DateRangePredicate(RelationshipConcreteDomainFeature.EFFECTIVE_TIME, null, date);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder effectiveTimeAfter(Date date) {
			previous = new DateRangePredicate(RelationshipConcreteDomainFeature.EFFECTIVE_TIME, date, null);
			return this;
		}
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder type(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.TYPE, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder label(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.LABEL, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder valueString(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.VALUE_STRING, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder valueBoolean(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.VALUE_BOOLEAN, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder valueDecimal(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.VALUE_DECIMAL, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder characteristicTypeId(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.CHARACTERISTIC_TYPE_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder operatorId(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.OPERATOR_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder uomId(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.UOM_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder referenceSetId(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.REFERENCE_SET_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder memberId(String argument) {
			previous = new StringPredicate(RelationshipConcreteDomainFeature.MEMBER_ID, argument);
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder or(RelationshipConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			previous = new Or(previous, expressionBuilder.build());
			return this;
		}
		
		@Override
		public RelationshipConcreteDomainBinaryOperatorBuilder not(RelationshipConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			previous = new Not(expressionBuilder.build());
			return this;
		}
	}
	
	public static ConcreteDomainPredicateBuilder concreteDomain() {
		return new ConcreteDomainExpressionBuilderImpl();
	}
	
	public static RelationshipConcreteDomainPredicateBuilder relationshipConcreteDomain() {
		return new RelationshipConcreteDomainExpressionBuilderImpl();
	}
}
