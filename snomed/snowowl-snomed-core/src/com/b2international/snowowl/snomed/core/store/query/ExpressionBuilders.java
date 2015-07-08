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

import com.b2international.snowowl.core.store.query.And;
import com.b2international.snowowl.core.store.query.BooleanPredicate;
import com.b2international.snowowl.core.store.query.Buildable;
import com.b2international.snowowl.core.store.query.Expression;
import com.b2international.snowowl.core.store.query.Feature;
import com.b2international.snowowl.core.store.query.NestedPath;
import com.b2international.snowowl.core.store.query.Not;
import com.b2international.snowowl.core.store.query.Or;
import com.b2international.snowowl.core.store.query.Same;
import com.b2international.snowowl.core.store.query.StringPredicate;
import com.b2international.snowowl.core.store.query.TextPredicate;
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
		ConceptBinaryOperatorBuilder hasConcreteDomain(ConcreteDomainBinaryOperatorBuilder expressionBuilder);
		ConceptBinaryOperatorBuilder parent(String argument);
		ConceptBinaryOperatorBuilder ancestor(String argument);
		ConceptBinaryOperatorBuilder not(ConceptBinaryOperatorBuilder expressionBuilder);
	}
	
	public interface ConceptBinaryOperatorBuilder extends ComponentBinaryOperatorBuilder<ConceptBinaryOperatorBuilder>, Buildable<Expression> {}
	
	public interface ConceptExpressionBuilder extends ConceptPredicateBuilder, ConceptBinaryOperatorBuilder {}
	
	private static class ConceptExpressionBuilderImpl implements ConceptExpressionBuilder {
		private Optional<Expression> previous = Optional.absent();
		
		@Override
		public ConceptBinaryOperatorBuilder id(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(SnomedComponentFeature.ID, argument));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder moduleId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(SnomedComponentFeature.MODULE_ID, argument));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder active(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(SnomedComponentFeature.ACTIVE, argument));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder released(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(SnomedComponentFeature.RELEASED, argument));
			return this;
		}

		public ConceptBinaryOperatorBuilder and(ConceptBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			And and = new And(previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(and);
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder or(ConceptBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			Or or = new Or(previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(or);
			return this;
		}


		@Override
		public Expression build() {
			return previous.get();
		}

		@Override
		public ConceptBinaryOperatorBuilder not(ConceptBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Not(expressionBuilder.build()));
			return this;
		}
		
		@Override
		public ConceptBinaryOperatorBuilder definitionStatusId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ConceptFeature.DEFINITION_STATUS_ID, argument));
			return this;
		}
		
		@Override
		public ConceptBinaryOperatorBuilder parent(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ConceptFeature.PARENTS, argument));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder ancestor(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ConceptFeature.ANCESTORS, argument));
			return this;
		}
		
		@Override
		public ConceptBinaryOperatorBuilder hasDescription(DescriptionBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Same(ConceptNestedPath.DESCRIPTIONS, expressionBuilder.build()));
			return this;
		}

		@Override
		public ConceptBinaryOperatorBuilder hasRelationship(RelationshipBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Same(ConceptNestedPath.RELATIONSHIPS, expressionBuilder.build()));
			return this;
		}
		
		
		@Override
		public ConceptBinaryOperatorBuilder hasConcreteDomain(ConcreteDomainBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Same(ConceptNestedPath.CONCRETE_DOMAINS, expressionBuilder.build()));
			return this;
		}
		
	}
	
	public interface DescriptionPredicateBuilder extends ComponentPredicateBuilder<DescriptionBinaryOperatorBuilder>, Buildable<Expression> {
		DescriptionBinaryOperatorBuilder id(String argument);
		DescriptionBinaryOperatorBuilder moduleId(String argument);
		DescriptionBinaryOperatorBuilder typeId(String argument);
		DescriptionBinaryOperatorBuilder caseSensitivityId(String argument);
		DescriptionBinaryOperatorBuilder term(String argument, TextPredicate.Operator operator);
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
			And and = new And(previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(and);
			return this;
		}
		
		@Override
		public Expression build() {
			return previous.get();
		}
		
		@Override
		public DescriptionBinaryOperatorBuilder id(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(SnomedComponentFeature.ID, argument));
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder moduleId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(SnomedComponentFeature.MODULE_ID, argument));
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder active(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(SnomedComponentFeature.ACTIVE, argument));
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder released(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(SnomedComponentFeature.RELEASED, argument));
			return this;
		}
		
		@Override
		public DescriptionBinaryOperatorBuilder typeId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(DescriptionFeature.TYPE_ID, argument));
			return this;
		}
		
		@Override
		public DescriptionBinaryOperatorBuilder caseSensitivityId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(DescriptionFeature.CASE_SENSITIVITY_ID, argument));
			return this;
		}
		
		@Override
		public DescriptionBinaryOperatorBuilder term(String argument, TextPredicate.Operator operator) {
			previous = Optional.<Expression>of(new TextPredicate(DescriptionFeature.TERM, argument, operator));
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder preferredIn(String argument) {
			return hasMembership(descriptionMembership().referenceSetId(argument).and(membership().acceptabilityId(PREFERRED_ID)));
		}

		@Override
		public DescriptionBinaryOperatorBuilder acceptableIn(String argument) {
			return hasMembership(descriptionMembership().referenceSetId(argument).and(membership().acceptabilityId(ACCEPTABLE_ID)));
		}

		@Override
		public DescriptionBinaryOperatorBuilder hasMembership(MembershipBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Same(DescriptionNestedPath.MEMBERSHIPS, expressionBuilder.build()));
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder or(DescriptionBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			Or or = new Or(previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(or);
			return this;
		}

		@Override
		public DescriptionBinaryOperatorBuilder not(DescriptionBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Not(expressionBuilder.build()));
			return this;
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
		private final Optional<NestedPath> path;
		
		public MembershipExpressionBuilderImpl() {
			this.path = Optional.absent();
		}
		
		public MembershipExpressionBuilderImpl(NestedPath path) {
			this.path = Optional.fromNullable(path);
		}

		@Override
		public MembershipBinaryOperatorBuilder id(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(MembershipFeature.ID), argument));
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder moduleId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(MembershipFeature.MODULE_ID), argument));
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder active(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(ensureNestedness(MembershipFeature.ACTIVE), argument));
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder released(boolean argument) {
			previous = Optional.<Expression>of(new BooleanPredicate(ensureNestedness(MembershipFeature.RELEASED), argument));
			return this;
		}
		
		@Override
		public MembershipBinaryOperatorBuilder type(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(MembershipFeature.TYPE), argument));
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder referenceSetId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(MembershipFeature.REFERENCE_SET_ID), argument));
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder referencedComponentId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(MembershipFeature.REFERENCED_COMPONENT_ID), argument));
			return this;
		}
		
		@Override
		public MembershipBinaryOperatorBuilder acceptabilityId(String argument) {
			previous = Optional.<Expression>of(new StringPredicate(ensureNestedness(MembershipFeature.ACCEPTABILITY_ID), argument));
			return this;
		}

		@Override
		public Expression build() {
			return previous.get();
		}

		@Override
		public MembershipBinaryOperatorBuilder and(MembershipBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			And and = new And(previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(and);
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder or(MembershipBinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			Or or = new Or(previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(or);
			return this;
		}

		@Override
		public MembershipBinaryOperatorBuilder not(MembershipBinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(new Not(expressionBuilder.build()));
			return this;
		}
		
		private Feature ensureNestedness(MembershipFeature feature) {
			if (isNested()) {
				switch (feature) {
				case ID:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.ID;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.ID;
					}
				case MODULE_ID:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.MODULE_ID;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.MODULE_ID;
					}
				case ACTIVE:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.ACTIVE;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.ACTIVE;
					}
				case RELEASED:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.RELEASED;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.RELEASED;
					}
				case EFFECTIVE_TIME:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.EFFECTIVE_TIME;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.EFFECTIVE_TIME;
					}
				case ACCEPTABILITY_ID:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.ACCEPTABILITY_ID;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.ACCEPTABILITY_ID;
					}
				case CORRELATION_ID:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.CORRELATION_ID;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.CORRELATION_ID;
					}
				case DESCRIPTION_FORMAT:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.DESCRIPTION_FORMAT;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.DESCRIPTION_FORMAT;
					}
				case DESCRIPTION_LENGTH:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.DESCRIPTION_LENGTH;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.DESCRIPTION_LENGTH;
					}
				case MAP_ADVICE:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.MAP_ADVICE;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.MAP_ADVICE;
					}
				case MAP_CATEGORY_ID:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.MAP_CATEGORY_ID;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.MAP_CATEGORY_ID;
					}
				case MAP_GROUP:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.MAP_GROUP;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.MAP_GROUP;
					}
				case MAP_PRIORITY:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.MAP_PRIORITY;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.MAP_PRIORITY;
					}
				case MAP_RULE:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.MAP_RULE;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.MAP_RULE;
					}
				case MAP_TARGET:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.MAP_TARGET;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.MAP_TARGET;
					}
				case QUERY:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.QUERY;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.QUERY;
					}
				case REFERENCE_SET_ID:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.REFERENCE_SET_ID;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.REFERENCE_SET_ID;
					}
				case REFERENCED_COMPONENT_ID:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.REFERENCED_COMPONENT_ID;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.REFERENCED_COMPONENT_ID;
					}
				case SOURCE_EFFECTIVE_TIME:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.SOURCE_EFFECTIVE_TIME;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.SOURCE_EFFECTIVE_TIME;
					}
				case TARGET_COMPONENT_ID:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.TARGET_COMPONENT_ID;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.TARGET_COMPONENT_ID;
					}
				case TARGET_EFFECTIVE_TIME:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.TARGET_EFFECTIVE_TIME;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.TARGET_EFFECTIVE_TIME;
					}
				case TYPE:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.TYPE;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.TYPE;
					}
				case VALUE_ID:
					if (isNestedInDescription()) {
						return DescriptionMembershipFeature.VALUE_ID;
					} else if (isNestedInRelationship()) {
						return RelationshipMembershipFeature.VALUE_ID;
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
		
		private boolean isNestedInDescription() {
			return path.isPresent() ? DescriptionNestedPath.MEMBERSHIPS.equals(path.get()) : false;
		}
		
		private boolean isNestedInRelationship() {
			return path.isPresent() ? RelationshipNestedPath.MEMBERSHIPS.equals(path.get()) : false;
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
	
	public static MembershipPredicateBuilder descriptionMembership() {
		return new MembershipExpressionBuilderImpl(DescriptionNestedPath.MEMBERSHIPS);
	}
	
	public static ConcreteDomainPredicateBuilder concreteDomain() {
		return new ConcreteDomainExpressionBuilderImpl();
	}
	
	public static ConcreteDomainPredicateBuilder relationshipConcreteDomain() {
		return new ConcreteDomainExpressionBuilderImpl(RelationshipNestedPath.CONCRETE_DOMAINS);
	}
}
