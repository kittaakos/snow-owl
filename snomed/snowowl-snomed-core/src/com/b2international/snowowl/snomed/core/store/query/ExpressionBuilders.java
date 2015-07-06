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

import com.b2international.snowowl.snomed.core.store.query.Feature.ComponentFeature;
import com.b2international.snowowl.snomed.core.store.query.Feature.ConceptFeature;
import com.b2international.snowowl.snomed.core.store.query.Feature.ConcreteDomainFeature;
import com.b2international.snowowl.snomed.core.store.query.Feature.DescriptionFeature;
import com.b2international.snowowl.snomed.core.store.query.Feature.MembershipFeature;
import com.b2international.snowowl.snomed.core.store.query.Feature.RelationshipFeature;
import com.b2international.snowowl.snomed.core.store.query.Type.ComponentType;
import com.b2international.snowowl.snomed.core.store.query.Type.NestedComponentType;
import com.google.common.base.Optional;

/**
 * @since 5.0
 */
abstract public class ExpressionBuilders {
	public interface ComponentPredicateBuilder<B> {
		B id(String argument);
		B moduleId(String argument);
		B active(boolean argument);
		B released(boolean argument);
		// TODO: effective time
		B not(B expressionBuilder);
	}
	
	public interface ComponentBinaryOperatorBuilder<B> {
		B and(B expressionBuilder);
		B or(B expressionBuilder);
	}
	
	public interface ConceptPredicateBuilder extends ComponentPredicateBuilder<ConceptBinaryOperatorBuilder>, Buildable<Expression> {
		ConceptBinaryOperatorBuilder definitionStatusId(String argument);
		ConceptBinaryOperatorBuilder hasDescription(DescriptionBinaryOperatorBuilder expressionBuilder);
		ConceptBinaryOperatorBuilder hasRelationship(RelationshipBinaryOperatorBuilder expressionBuilder);
		ConceptBinaryOperatorBuilder parent(String argument);
		ConceptBinaryOperatorBuilder ancestor(String argument);
		ConceptBinaryOperatorBuilder child(String argument);
		ConceptBinaryOperatorBuilder descendant(String argument);
		ConceptBinaryOperatorBuilder not(ConceptBinaryOperatorBuilder expressionBuilder);
	}
	
	public interface ConceptBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<ConceptBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface ConceptExpressionBuilder extends ConceptPredicateBuilder, ConceptBinaryOperatorBuilder {}
	
	private static class ConceptExpressionBuilderImpl implements ConceptExpressionBuilder {
		private Optional<Expression> previous = Optional.absent();
		
		@Override
		public ConceptBinaryOperatorBuilder id(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ComponentType.CONCEPT, ComponentFeature.ID, argument));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder moduleId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ComponentType.CONCEPT, ComponentFeature.MODULE_ID, argument));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder active(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(ComponentType.CONCEPT, ComponentFeature.ACTIVE, argument));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder released(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(ComponentType.CONCEPT, ComponentFeature.RELEASED, argument));
			return this;
		}

		public ConceptBinaryOperatorBuilder and(ConceptBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			And and = new And(ComponentType.CONCEPT, previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(and);
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder or(ConceptBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			Or or = new Or(ComponentType.CONCEPT, previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(or);
			return this;
		}


		@Override
		public Expression build() {
			return previous.get();
		}

		@Override
		public ConceptBinaryOperatorBuilder not(ConceptBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Not(ComponentType.CONCEPT, expressionBuilder.build()));
			return this;
		}
		
		@Override
		public ConceptBinaryOperatorBuilder definitionStatusId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ComponentType.CONCEPT, ConceptFeature.DEFINITION_STATUS_ID, argument));
			return this;
		}
		
		@Override
		public ConceptBinaryOperatorBuilder parent(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ComponentType.CONCEPT, ConceptFeature.PARENTS, argument));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder ancestor(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ComponentType.CONCEPT, ConceptFeature.ANCESTORS, argument));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder child(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ComponentType.CONCEPT, ConceptFeature.CHILDREN, argument));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder descendant(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ComponentType.CONCEPT, ConceptFeature.DESCENDANTS, argument));
			return this;
		}
		
		@Override
		public ConceptBinaryOperatorBuilder hasDescription(DescriptionBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Same(ComponentType.CONCEPT, NestedComponentType.DESCRIPTION, expressionBuilder.build()));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder hasRelationship(RelationshipBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Same(ComponentType.CONCEPT, NestedComponentType.RELATIONSHIP, expressionBuilder.build()));
			return this;
		}
		
	}
	
	public interface DescriptionPredicateBuilder extends ComponentPredicateBuilder<DescriptionBinaryOperatorBuilder>, Buildable<Expression> {
		DescriptionBinaryOperatorBuilder id(String argument);
		DescriptionBinaryOperatorBuilder moduleId(String argument);
		DescriptionBinaryOperatorBuilder typeId(String argument);
		DescriptionBinaryOperatorBuilder term(String argument);
		DescriptionBinaryOperatorBuilder preferredIn(String argument);
		DescriptionBinaryOperatorBuilder acceptableIn(String argument);
		DescriptionBinaryOperatorBuilder hasMembership(MembershipBinaryOperatorBuilder expressionBuilder);
	}
	
	public interface DescriptionBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<DescriptionBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface DescriptionExpressionBuilder extends DescriptionPredicateBuilder, DescriptionBinaryOperatorBuilder {}
	
	private static class DescriptionExpressionBuilderImpl implements DescriptionExpressionBuilder {
		private static final String ACCEPTABLE_ID = "900000000000549004";
		private static final String PREFERRED_ID = "900000000000548007";
		
		private Optional<Expression> previous = Optional.absent();
		
		public DescriptionBinaryOperatorBuilder and(DescriptionBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			And and = new And(previousExpression.getType(), previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(and);
			return this;
		}
		
		@Override
		public Expression build() {
			return previous.get();
		}
		
		@Override
		public DescriptionBinaryOperatorBuilder id(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.DESCRIPTION, ComponentFeature.ID, argument));
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder moduleId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.DESCRIPTION, ComponentFeature.MODULE_ID, argument));
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder active(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(NestedComponentType.DESCRIPTION, ComponentFeature.ACTIVE, argument));
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder released(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(NestedComponentType.DESCRIPTION, ComponentFeature.RELEASED, argument));
			return this;
		}
		
		@Override
		public DescriptionBinaryOperatorBuilder typeId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.DESCRIPTION, DescriptionFeature.TYPE_ID, argument));
			return this;
		}
		
		@Override
		public DescriptionBinaryOperatorBuilder term(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.DESCRIPTION, DescriptionFeature.TERM, argument));
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder preferredIn(String argument) {
			return hasMembership(membership().referenceSetId(argument).and(membership().acceptabilityId(PREFERRED_ID)));
		}

		@Override
		public DescriptionBinaryOperatorBuilder acceptableIn(String argument) {
			return hasMembership(membership().referenceSetId(argument).and(membership().acceptabilityId(ACCEPTABLE_ID)));
		}

		@Override
		public DescriptionBinaryOperatorBuilder hasMembership(MembershipBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Same(NestedComponentType.DESCRIPTION, NestedComponentType.MEMBERSHIP, expressionBuilder.build()));
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder or(DescriptionBinaryOperatorBuilder expressionBuilder) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DescriptionBinaryOperatorBuilder not(DescriptionBinaryOperatorBuilder expressionBuilder) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public interface MembershipPredicateBuilder extends ComponentPredicateBuilder<MembershipBinaryOperatorBuilder>, Buildable<Expression> {
		MembershipBinaryOperatorBuilder type(String argument);
		MembershipBinaryOperatorBuilder acceptabilityId(String argument);
		MembershipBinaryOperatorBuilder referenceSetId(String argument);
		MembershipBinaryOperatorBuilder referencedComponentId(String argument);
	}
	
	public interface MembershipBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<MembershipBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface MembershipExpressionBuilder extends MembershipPredicateBuilder, MembershipBinaryOperatorBuilder {}
	
	private static final class MembershipExpressionBuilderImpl implements MembershipExpressionBuilder {
		private Optional<Expression> previous = Optional.absent();
		
		@Override
		public MembershipBinaryOperatorBuilder id(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.MEMBERSHIP, ComponentFeature.ID, argument));
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder moduleId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.MEMBERSHIP, ComponentFeature.MODULE_ID, argument));
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder active(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(NestedComponentType.MEMBERSHIP, ComponentFeature.ACTIVE, argument));
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder released(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(NestedComponentType.MEMBERSHIP, ComponentFeature.RELEASED, argument));
			return this;
		}
		
		@Override
		public MembershipBinaryOperatorBuilder type(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.MEMBERSHIP, MembershipFeature.TYPE, argument));
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder referenceSetId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.MEMBERSHIP, MembershipFeature.REFERENCE_SET_ID, argument));
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder referencedComponentId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.MEMBERSHIP, MembershipFeature.REFERENCED_COMPONENT_ID, argument));
			return this;
		}
		
		@Override
		public MembershipBinaryOperatorBuilder acceptabilityId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.MEMBERSHIP, MembershipFeature.ACCEPTABILITY_ID, argument));
			return this;
		}

		@Override
		public Expression build() {
			return previous.get();
		}

		@Override
		public MembershipBinaryOperatorBuilder and(MembershipBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			And and = new And(NestedComponentType.MEMBERSHIP, previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(and);
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder or(MembershipBinaryOperatorBuilder expressionBuilder) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MembershipBinaryOperatorBuilder not(MembershipBinaryOperatorBuilder expressionBuilder) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
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
	}
	
	public interface RelationshipBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<RelationshipBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface RelationshipExpressionBuilder extends RelationshipPredicateBuilder, RelationshipBinaryOperatorBuilder {}
	
	private static class RelationshipExpressionBuilderImpl implements RelationshipExpressionBuilder {
		
		private Optional<Expression> previous = Optional.absent();
		
		public RelationshipBinaryOperatorBuilder and(RelationshipBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			And and = new And(NestedComponentType.RELATIONSHIP, previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(and);
			return this;
		}
		
		@Override
		public Expression build() {
			return previous.get();
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder id(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.RELATIONSHIP, ComponentFeature.ID, argument));
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder moduleId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.RELATIONSHIP, ComponentFeature.MODULE_ID, argument));
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder active(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(NestedComponentType.RELATIONSHIP, ComponentFeature.ACTIVE, argument));
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder released(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(NestedComponentType.RELATIONSHIP, ComponentFeature.RELEASED, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder typeId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.RELATIONSHIP, RelationshipFeature.TYPE_ID, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder typeAncestorId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.RELATIONSHIP, RelationshipFeature.TYPE_ALL_ANCESTOR_IDS, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder destinationId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.RELATIONSHIP, RelationshipFeature.DESTINATION_ID, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder destinationAncestorId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.RELATIONSHIP, RelationshipFeature.DESTINATION_ALL_ANCESTOR_IDS, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder modifierId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.RELATIONSHIP, RelationshipFeature.MODIFIER_ID, argument));
			return this;
		}
		
		@Override
		public RelationshipBinaryOperatorBuilder characteristicTypeId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.RELATIONSHIP, RelationshipFeature.CHARACTERISTIC_TYPE_ID, argument));
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder hasMembership(MembershipBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Same(NestedComponentType.RELATIONSHIP, NestedComponentType.MEMBERSHIP, expressionBuilder.build()));
			return this;
		}

		@Override
		public RelationshipBinaryOperatorBuilder or(RelationshipBinaryOperatorBuilder expressionBuilder) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public RelationshipBinaryOperatorBuilder not(RelationshipBinaryOperatorBuilder expressionBuilder) {
			// TODO Auto-generated method stub
			return null;
		}

	}
	
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
		
		public ConcreteDomainBinaryOperatorBuilder and(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			And and = new And(NestedComponentType.RELATIONSHIP, previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(and);
			return this;
		}
		
		@Override
		public Expression build() {
			return previous.get();
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder id(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ComponentFeature.ID, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder moduleId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ComponentFeature.MODULE_ID, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder active(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(NestedComponentType.CONCRETE_DOMAIN, ComponentFeature.ACTIVE, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder released(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(NestedComponentType.CONCRETE_DOMAIN, ComponentFeature.RELEASED, argument));
			return this;
		}
		
		@Override
		public ConcreteDomainBinaryOperatorBuilder type(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ConcreteDomainFeature.TYPE, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder label(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ConcreteDomainFeature.LABEL, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder valueString(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ConcreteDomainFeature.VALUE_STRING, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder valueBoolean(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ConcreteDomainFeature.VALUE_BOOLEAN, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder valueDecimal(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ConcreteDomainFeature.VALUE_DECIMAL, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder characteristicTypeId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ConcreteDomainFeature.CHARACTERISTIC_TYPE_ID, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder operatorId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ConcreteDomainFeature.OPERATOR_ID, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder uomId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ConcreteDomainFeature.UOM_ID, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder referenceSetId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ConcreteDomainFeature.REFERENCE_SET_ID, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder memberId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(NestedComponentType.CONCRETE_DOMAIN, ConcreteDomainFeature.MEMBER_ID, argument));
			return this;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder or(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ConcreteDomainBinaryOperatorBuilder not(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public static ConceptPredicateBuilder concept() {
		return new ConceptExpressionBuilderImpl();
	}
	
	public static DescriptionPredicateBuilder description() {
		return new DescriptionExpressionBuilderImpl();
	}
	
	public static RelationshipPredicateBuilder relationship() {
		return new RelationshipExpressionBuilderImpl();
	}
	
	public static MembershipPredicateBuilder membership() {
		return new MembershipExpressionBuilderImpl();
	}
	
	public static ConcreteDomainPredicateBuilder concreteDomain() {
		return new ConcreteDomainExpressionBuilderImpl();
	}
}
