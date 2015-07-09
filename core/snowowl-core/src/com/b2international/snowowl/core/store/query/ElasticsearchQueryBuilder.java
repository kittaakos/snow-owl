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
package com.b2international.snowowl.core.store.query;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Deque;

import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;

import com.b2international.snowowl.core.store.query.TextPredicate.Operator;
import com.google.common.collect.Queues;

public class ElasticsearchQueryBuilder implements ExpressionQueryBuilder {

	private static final class DequeItem {
		private final ToXContent builder;
		
		public DequeItem(QueryBuilder builder) {
			this.builder = builder;
		}
		
		public DequeItem(FilterBuilder builder) {
			this.builder = builder;
		}

		public boolean isQueryBuilder() {
			return builder instanceof QueryBuilder;
		}
		
		public boolean isFilterBuilder() {
			return builder instanceof FilterBuilder;
		}
		
		public QueryBuilder getQueryBuilder() {
			return (QueryBuilder) builder;
		}
		
		public FilterBuilder getFilterBuilder() {
			return (FilterBuilder) builder;
		}
		
		@Override
		public String toString() {
			return builder.toString();
		}
	}
	
	private final Deque<DequeItem> deque = Queues.newLinkedBlockingDeque();
	
	@Override
	public QueryBuilder build(Expression expression) {
		checkNotNull(expression, "expression");
		traversePostOrder(expression);
		if (deque.size() == 1) {
			DequeItem item = deque.pop();
			if (item.isQueryBuilder()) {
				return item.getQueryBuilder();
			} else if (item.isFilterBuilder()) {
				return QueryBuilders.constantScoreQuery(item.getFilterBuilder());
			} else {
				handleIllegalDequeState();
			}
		} else {
			handleIllegalDequeState();
		}
		return null;
	}

	private void visit(Expression expression) {
		if (expression instanceof MatchAll) {
			deque.push(new DequeItem(QueryBuilders.matchAllQuery()));
		} else if (expression instanceof And) {
			And and = (And) expression;
			visit(and);
		} else if (expression instanceof Or) {
			Or or = (Or) expression;
			visit(or);
		} else if (expression instanceof Not) {
			Not not = (Not) expression;
			visit(not);
		} else if (expression instanceof Same) {
			Same same = (Same) expression;
			visit(same);
		} else if (expression instanceof Group) {
			Group group = (Group) expression;
			visit(group);
		} else if (expression instanceof StringPredicate) {
			StringPredicate predicate = (StringPredicate) expression;
			visit(predicate);
		} else if (expression instanceof BooleanPredicate) {
			BooleanPredicate predicate = (BooleanPredicate) expression;
			visit(predicate);
		} else if (expression instanceof StringSetPredicate) {
			StringSetPredicate predicate = (StringSetPredicate) expression;
			visit(predicate);
		} else if (expression instanceof LongPredicate) {
			visit((LongPredicate) expression);
		} else if (expression instanceof TextPredicate) {
			TextPredicate predicate = (TextPredicate) expression;
			visit(predicate);
		} else if (expression instanceof DateRangePredicate) {
			DateRangePredicate predicate = (DateRangePredicate) expression;
			visit(predicate);
		} else {
			throw new IllegalArgumentException("Unexpected expression: " + expression);
		}
	}

	private void visit(TextPredicate predicate) {
		Feature feature = predicate.getFeature();
		Operator operator = predicate.getOperator();
		switch (operator) {
		case ALL:
			QueryBuilder queryBuilder = QueryBuilders.matchQuery(feature.getField(), predicate.getText()).operator(MatchQueryBuilder.Operator.AND);
			deque.push(new DequeItem(queryBuilder));
			break;
		case ANY:
			queryBuilder = QueryBuilders.matchQuery(feature.getField(), predicate.getText()).operator(MatchQueryBuilder.Operator.OR);
			deque.push(new DequeItem(queryBuilder));
			break;
		case EXACT:
			queryBuilder = QueryBuilders.matchQuery(feature.getField(), predicate.getText()).type(MatchQueryBuilder.Type.PHRASE);
			deque.push(new DequeItem(queryBuilder));
			break;
		case NONE:
			queryBuilder = QueryBuilders.boolQuery().mustNot(
					QueryBuilders.matchQuery(feature.getField(), predicate.getText()).operator(MatchQueryBuilder.Operator.OR));
			deque.push(new DequeItem(queryBuilder));
			break;
		default:
			throw new IllegalArgumentException("Unexpected operator: " + operator);
		}
	}
	
	private void visit(StringSetPredicate predicate) {
		Feature feature = predicate.getFeature();
		FilterBuilder filter = FilterBuilders.termsFilter(feature.getField(), predicate.getArgument());
		deque.push(new DequeItem(filter));
	}
	
	private void visit(BooleanPredicate predicate) {
		Feature feature = predicate.getFeature();
		FilterBuilder filter = FilterBuilders.termFilter(feature.getField(), predicate.getArgument());
		deque.push(new DequeItem(filter));
	}

	private void visit(StringPredicate predicate) {
		Feature feature = predicate.getFeature();
		FilterBuilder filter = FilterBuilders.termFilter(feature.getField(), predicate.getArgument());
		deque.push(new DequeItem(filter));
	}
	
	private void visit(LongPredicate predicate) {
		Feature feature = predicate.getFeature();
		FilterBuilder filter = FilterBuilders.termFilter(feature.getField(), predicate.getArgument());
		deque.push(new DequeItem(filter));
	}
	
	private void visit(DateRangePredicate predicate) {
		Feature feature = predicate.getFeature();
		RangeFilterBuilder filter = FilterBuilders.rangeFilter(feature.getField());
		if (predicate.getStart().isPresent()) {
			filter.from(predicate.getStart().get()).includeLower(predicate.isStartInclusive());
		}
		if (predicate.getEnd().isPresent()) {
			filter.to(predicate.getEnd().get()).includeUpper(predicate.isEndInclusive());
		}
		deque.push(new DequeItem(filter));
	}

	private void visit(And and) {
		if (and.getRight().isPresent() && deque.size() >= 2) {
			DequeItem right = deque.pop();
			DequeItem left = deque.pop();
			if (right.isFilterBuilder() && left.isFilterBuilder()) {
				deque.push(new DequeItem(FilterBuilders.boolFilter().must(left.getFilterBuilder()).must(right.getFilterBuilder())));
			} else if (right.isFilterBuilder() && left.isQueryBuilder()) {
				deque.push(new DequeItem(QueryBuilders.boolQuery().must(QueryBuilders.constantScoreQuery(right.getFilterBuilder())).must(left.getQueryBuilder())));
			} else if (right.isQueryBuilder() && left.isFilterBuilder()) {
				deque.push(new DequeItem(QueryBuilders.boolQuery().must(QueryBuilders.constantScoreQuery(left.getFilterBuilder())).must(right.getQueryBuilder())));
			} else if (right.isQueryBuilder() && left.isQueryBuilder()) {
				deque.push(new DequeItem(QueryBuilders.boolQuery().must(left.getQueryBuilder()).must(right.getQueryBuilder())));
			}
		} else if (deque.size() >= 1) {
			DequeItem item = deque.pop();
			if (item.isFilterBuilder()) {
				deque.push(new DequeItem(FilterBuilders.boolFilter().must(item.getFilterBuilder())));
			} else if (item.isQueryBuilder()) {
				deque.push(new DequeItem(QueryBuilders.boolQuery().must(item.getQueryBuilder())));
			} else {
				handleIllegalDequeState();
			}
		} else {
			handleIllegalDequeState();
		}
	}

	private void visit(Or or) {
		if (or.getRight().isPresent() && deque.size() >= 2) {
			DequeItem right = deque.pop();
			DequeItem left = deque.pop();
			if (right.isFilterBuilder() && left.isFilterBuilder()) {
				deque.push(new DequeItem(FilterBuilders.boolFilter().should(left.getFilterBuilder()).should(right.getFilterBuilder())));
			} else if (right.isFilterBuilder() && left.isQueryBuilder()) {
				deque.push(new DequeItem(QueryBuilders.boolQuery().should(QueryBuilders.constantScoreQuery(right.getFilterBuilder())).should(left.getQueryBuilder())));
			} else if (right.isQueryBuilder() && left.isFilterBuilder()) {
				deque.push(new DequeItem(QueryBuilders.boolQuery().should(QueryBuilders.constantScoreQuery(left.getFilterBuilder())).should(right.getQueryBuilder())));
			} else if (right.isQueryBuilder() && left.isQueryBuilder()) {
				deque.push(new DequeItem(QueryBuilders.boolQuery().should(left.getQueryBuilder()).should(right.getQueryBuilder())));
			}
		} else if (deque.size() >= 1) {
			DequeItem item = deque.pop();
			if (item.isFilterBuilder()) {
				deque.push(new DequeItem(FilterBuilders.boolFilter().should(item.getFilterBuilder())));
			} else if (item.isQueryBuilder()) {
				deque.push(new DequeItem(QueryBuilders.boolQuery().should(item.getQueryBuilder())));
			} else {
				handleIllegalDequeState();
			}
		} else {
			handleIllegalDequeState();
		}
	}
	
	private void visit(Not not) {
		if (deque.size() >= 1) {
			DequeItem item = deque.pop();
			if (item.isFilterBuilder()) {
				deque.push(new DequeItem(FilterBuilders.notFilter(item.getFilterBuilder())));
			} else if (item.isQueryBuilder()) {
				deque.push(new DequeItem(QueryBuilders.boolQuery().mustNot(item.getQueryBuilder())));
			} else {
				handleIllegalDequeState();
			}
		} else {
			handleIllegalDequeState();
		}
	}

	private void visit(Same same) {
		if (deque.size() >= 1) {
			DequeItem item = deque.pop();
			if (item.isFilterBuilder()) {
				deque.push(new DequeItem(FilterBuilders.nestedFilter(same.getPath().getPath(), item.getFilterBuilder())));
			} else if (item.isQueryBuilder()) {
				deque.push(new DequeItem(QueryBuilders.nestedQuery(same.getPath().getPath(), item.getQueryBuilder())));
			} else {
				handleIllegalDequeState();
			}
		} else {
			handleIllegalDequeState();
		}
	}
	
	private void visit(Group parenthesis) {
		// carries no real meaning, skip it
	}

	private void handleIllegalDequeState() {
		throw new IllegalStateException("Illegal internal stack state: " + deque);
	}

	private void traversePostOrder(Expression node) {
		if (node instanceof BinaryOperator) {
			BinaryOperator binaryOperator = (BinaryOperator) node;
			Expression left = binaryOperator.getLeft();
			traversePostOrder(left);
			if (binaryOperator.getRight().isPresent()) {
				Expression right = binaryOperator.getRight().get();
				traversePostOrder(right);
			}
		} else if (node instanceof UnaryOperator) {
			UnaryOperator unaryOperator = (UnaryOperator) node;
			Expression right = unaryOperator.getRight();
			traversePostOrder(right);
		}
		visit(node);
	}

}
