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
import com.b2international.snowowl.snomed.core.store.query.DescriptionMembershipFeature;
import com.b2international.snowowl.snomed.core.store.query.DescriptionNestedPath;
import com.b2international.snowowl.snomed.core.store.query.MembershipFeature;
import com.b2international.snowowl.snomed.core.store.query.RelationshipMembershipFeature;
import com.b2international.snowowl.snomed.core.store.query.RelationshipNestedPath;
import com.google.common.base.Optional;

/**
 * @since 5.0
 */
abstract public class MembershipExpressions {
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
	
	public static MembershipPredicateBuilder membership() {
		return new MembershipExpressionBuilderImpl();
	}
	
	public static MembershipPredicateBuilder descriptionMembership() {
		return new MembershipExpressionBuilderImpl(DescriptionNestedPath.MEMBERSHIPS);
	}
}
