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

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 5.0
 */
public class ElasticsearchQueryBuilderTest {

	private static final String TEST_STRING_ARGUMENT_1 = "test1";
	private static final String TEST_STRING_ARGUMENT_2 = "test2";
	private static final String TEST_TEXT_ARGUMENT_1 = "test text argument";
	private static final String TEST_TEXT_ARGUMENT_2 = "yet another text test argument";
	private static final Boolean TEST_BOOLEAN_ARGUMENT = true;
	private static final Date TEST_START_DATE = new Date();
	private static final Date TEST_END_DATE = new Date();

	private ElasticsearchQueryBuilder builder;

	@Before
	public void before() {
		builder = new ElasticsearchQueryBuilder();
	}

	@Test(expected = NullPointerException.class)
	public void whenExpressionIsNull_ThenThrowNPE() {
		builder.build(null);
	}

	@Test
	public void whenMatchAll_ThenBuildMatchAllQuery() {
		QueryBuilder result = builder.build(new MatchAll());
		QueryBuilder expected = QueryBuilders.matchAllQuery();
		assertEquals(expected.toString(), result.toString());
	}

	/*
	 * StringPredicate
	 */

	@Test
	public void whenStringPredicate_ThenBuildConstantScoreQueryWithTermFilter() {
		StringPredicate predicate = new StringPredicate(MockFeature.FOO, TEST_STRING_ARGUMENT_1);
		QueryBuilder result = builder.build(predicate);
		QueryBuilder expected = QueryBuilders.constantScoreQuery(FilterBuilders.termFilter(MockFeature.FOO.getField(), TEST_STRING_ARGUMENT_1));
		assertEquals(expected.toString(), result.toString());
	}

	/*
	 * TextPredicate
	 */

	@Test
	public void whenAllTextPredicate_ThenBuildMatchQuery() {
		TextPredicate predicate = new TextPredicate(MockFeature.FOO, TEST_TEXT_ARGUMENT_1, TextPredicate.Operator.ALL);
		QueryBuilder result = builder.build(predicate);
		QueryBuilder expected = QueryBuilders.matchQuery(MockFeature.FOO.getField(), TEST_TEXT_ARGUMENT_1).operator(MatchQueryBuilder.Operator.AND);
		assertEquals(expected.toString(), result.toString());
	}

	@Test
	public void whenAnyTextPredicate_ThenBuildMatchQuery() {
		TextPredicate predicate = new TextPredicate(MockFeature.FOO, TEST_TEXT_ARGUMENT_1, TextPredicate.Operator.ANY);
		QueryBuilder result = builder.build(predicate);
		QueryBuilder expected = QueryBuilders.matchQuery(MockFeature.FOO.getField(), TEST_TEXT_ARGUMENT_1).operator(MatchQueryBuilder.Operator.OR);
		assertEquals(expected.toString(), result.toString());
	}

	@Test
	public void whenNoneTextPredicate_ThenBuildMatchQuery() {
		TextPredicate predicate = new TextPredicate(MockFeature.FOO, TEST_TEXT_ARGUMENT_1, TextPredicate.Operator.NONE);
		QueryBuilder result = builder.build(predicate);
		QueryBuilder expected = QueryBuilders.boolQuery().mustNot(
				QueryBuilders.matchQuery(MockFeature.FOO.getField(), TEST_TEXT_ARGUMENT_1).operator(MatchQueryBuilder.Operator.OR));
		assertEquals(expected.toString(), result.toString());
	}

	@Test
	public void whenExactTextPredicate_ThenBuildMatchQuery() {
		TextPredicate predicate = new TextPredicate(MockFeature.FOO, TEST_TEXT_ARGUMENT_1, TextPredicate.Operator.EXACT);
		QueryBuilder result = builder.build(predicate);
		QueryBuilder expected = QueryBuilders.matchQuery(MockFeature.FOO.getField(), TEST_TEXT_ARGUMENT_1).type(MatchQueryBuilder.Type.PHRASE);
		assertEquals(expected.toString(), result.toString());
	}

	/*
	 * BooleanPredicate
	 */

	@Test
	public void whenBooleanPredicate_ThenBuildConstantScoreQueryWithTermFilter() {
		BooleanPredicate predicate = new BooleanPredicate(MockFeature.FOO, TEST_BOOLEAN_ARGUMENT);
		QueryBuilder result = builder.build(predicate);
		QueryBuilder expected = QueryBuilders.constantScoreQuery(FilterBuilders.termFilter(MockFeature.FOO.getField(), TEST_BOOLEAN_ARGUMENT));
		assertEquals(expected.toString(), result.toString());
	}

	/*
	 * And
	 */

	@Test
	public void whenAndWithTwoFilters_ThenBuildConstantScoreQueryWithBooleanFilter() {
		StringPredicate left = new StringPredicate(MockFeature.BAR, TEST_STRING_ARGUMENT_1);
		StringPredicate right = new StringPredicate(MockFeature.FOO, TEST_STRING_ARGUMENT_2);
		And and = new And(left, right);
		QueryBuilder result = builder.build(and);
		QueryBuilder expected = QueryBuilders.constantScoreQuery(FilterBuilders.boolFilter().must(
				FilterBuilders.termFilter(MockFeature.BAR.getField(), TEST_STRING_ARGUMENT_1),
				FilterBuilders.termFilter(MockFeature.FOO.getField(), TEST_STRING_ARGUMENT_2)));
		assertEquals(expected.toString(), result.toString());
	}

	@Test
	public void whenAndWithTwoQueries_ThenBuildBooleanQuery() {
		TextPredicate left = new TextPredicate(MockFeature.BAR, TEST_TEXT_ARGUMENT_1, TextPredicate.Operator.ALL);
		TextPredicate right = new TextPredicate(MockFeature.FOO, TEST_TEXT_ARGUMENT_2, TextPredicate.Operator.ALL);
		And and = new And(left, right);
		QueryBuilder result = builder.build(and);
		QueryBuilder expected = QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery(MockFeature.BAR.getField(), TEST_TEXT_ARGUMENT_1).operator(MatchQueryBuilder.Operator.AND))
				.must(QueryBuilders.matchQuery(MockFeature.FOO.getField(), TEST_TEXT_ARGUMENT_2).operator(MatchQueryBuilder.Operator.AND));
		assertEquals(expected.toString(), result.toString());
	}

	@Test
	public void whenAndWithMixedFilterAndQuery_ThenBuildBooleanQueryAndWrapFilter() {
		StringPredicate left = new StringPredicate(MockFeature.BAR, TEST_STRING_ARGUMENT_1);
		TextPredicate right = new TextPredicate(MockFeature.FOO, TEST_TEXT_ARGUMENT_2, TextPredicate.Operator.ALL);
		And and = new And(left, right);
		QueryBuilder result = builder.build(and);
		QueryBuilder expected = QueryBuilders.boolQuery()
				.must(QueryBuilders.constantScoreQuery(FilterBuilders.termFilter(MockFeature.BAR.getField(), TEST_STRING_ARGUMENT_1)))
				.must(QueryBuilders.matchQuery(MockFeature.FOO.getField(), TEST_TEXT_ARGUMENT_2).operator(MatchQueryBuilder.Operator.AND));
		assertEquals(expected.toString(), result.toString());
	}

	/*
	 * Or
	 */

	@Test
	public void whenOrWithTwoFilters_ThenBuildConstantScoreQueryWithBooleanFilter() {
		StringPredicate left = new StringPredicate(MockFeature.BAR, TEST_STRING_ARGUMENT_1);
		StringPredicate right = new StringPredicate(MockFeature.FOO, TEST_STRING_ARGUMENT_2);
		Or or = new Or(left, right);
		QueryBuilder result = builder.build(or);
		QueryBuilder expected = QueryBuilders.constantScoreQuery(FilterBuilders.boolFilter().should(
				FilterBuilders.termFilter(MockFeature.BAR.getField(), TEST_STRING_ARGUMENT_1),
				FilterBuilders.termFilter(MockFeature.FOO.getField(), TEST_STRING_ARGUMENT_2)));
		assertEquals(expected.toString(), result.toString());
	}

	@Test
	public void whenOrWithTwoQueries_ThenBuildBooleanQuery() {
		TextPredicate left = new TextPredicate(MockFeature.BAR, TEST_TEXT_ARGUMENT_1, TextPredicate.Operator.ALL);
		TextPredicate right = new TextPredicate(MockFeature.FOO, TEST_TEXT_ARGUMENT_2, TextPredicate.Operator.ALL);
		Or or = new Or(left, right);
		QueryBuilder result = builder.build(or);
		QueryBuilder expected = QueryBuilders.boolQuery()
				.should(QueryBuilders.matchQuery(MockFeature.BAR.getField(), TEST_TEXT_ARGUMENT_1).operator(MatchQueryBuilder.Operator.AND))
				.should(QueryBuilders.matchQuery(MockFeature.FOO.getField(), TEST_TEXT_ARGUMENT_2).operator(MatchQueryBuilder.Operator.AND));
		assertEquals(expected.toString(), result.toString());
	}

	@Test
	public void whenOrWithMixedFilterAndQuery_ThenBuildBooleanQueryAndWrapFilter() {
		StringPredicate left = new StringPredicate(MockFeature.BAR, TEST_STRING_ARGUMENT_1);
		TextPredicate right = new TextPredicate(MockFeature.FOO, TEST_TEXT_ARGUMENT_2, TextPredicate.Operator.ALL);
		Or or = new Or(left, right);
		QueryBuilder result = builder.build(or);
		QueryBuilder expected = QueryBuilders.boolQuery()
				.should(QueryBuilders.constantScoreQuery(FilterBuilders.termFilter(MockFeature.BAR.getField(), TEST_STRING_ARGUMENT_1)))
				.should(QueryBuilders.matchQuery(MockFeature.FOO.getField(), TEST_TEXT_ARGUMENT_2).operator(MatchQueryBuilder.Operator.AND));
		assertEquals(expected.toString(), result.toString());
	}

	/*
	 * Not
	 */

	@Test
	public void whenNotWithQuery_ThenBooleanQuery() {
		TextPredicate predicate = new TextPredicate(MockFeature.FOO, TEST_TEXT_ARGUMENT_1, TextPredicate.Operator.ALL);
		Not not = new Not(predicate);
		QueryBuilder result = builder.build(not);
		QueryBuilder expected = QueryBuilders.boolQuery().mustNot(
				QueryBuilders.matchQuery(MockFeature.FOO.getField(), TEST_TEXT_ARGUMENT_1).operator(MatchQueryBuilder.Operator.AND));
		assertEquals(expected.toString(), result.toString());
	}

	@Test
	public void whenNotWithFilter_ThenConstantScoreQueryWithNotFilter() {
		StringPredicate predicate = new StringPredicate(MockFeature.FOO, TEST_STRING_ARGUMENT_1);
		Not not = new Not(predicate);
		QueryBuilder result = builder.build(not);
		QueryBuilder expected = QueryBuilders.constantScoreQuery(FilterBuilders.notFilter(FilterBuilders.termFilter(MockFeature.FOO.getField(),
				TEST_STRING_ARGUMENT_1)));
		assertEquals(expected.toString(), result.toString());
	}

	/*
	 * Same
	 */

	@Test
	public void whenSameWithFilter_ThenConstantScoreQueryWithNestedFilter() {
		StringPredicate predicate = new StringPredicate(MockNestedFeature.BAR, TEST_STRING_ARGUMENT_1);
		Same same = new Same(MockNestedPath.INSTANCE, predicate);
		QueryBuilder result = builder.build(same);
		QueryBuilder expected = QueryBuilders.constantScoreQuery(FilterBuilders.nestedFilter(MockNestedPath.INSTANCE.getPath(),
				FilterBuilders.termFilter(MockNestedFeature.BAR.getField(), TEST_STRING_ARGUMENT_1)));
		assertEquals(expected.toString(), result.toString());
	}

	@Test
	public void whenSameWithQuery_ThenNestedQuery() {
		TextPredicate predicate = new TextPredicate(MockNestedFeature.FOO, TEST_TEXT_ARGUMENT_1, TextPredicate.Operator.ALL);
		Same same = new Same(MockNestedPath.INSTANCE, predicate);
		QueryBuilder result = builder.build(same);
		QueryBuilder expected = QueryBuilders.nestedQuery(MockNestedPath.INSTANCE.getPath(),
				QueryBuilders.matchQuery(MockNestedFeature.FOO.getField(), TEST_TEXT_ARGUMENT_1).operator(MatchQueryBuilder.Operator.AND));
		assertEquals(expected.toString(), result.toString());
	}
	
	@Test
	public void whenDateRangePredicate_ThenConstantScoreQueryWithRangeFilter() {
		DateRangePredicate predicate = new DateRangePredicate(MockFeature.FOO, TEST_START_DATE, TEST_END_DATE);
		QueryBuilder result = builder.build(predicate);
		QueryBuilder expected = QueryBuilders.constantScoreQuery(FilterBuilders.rangeFilter(MockFeature.FOO.getField()).from(TEST_START_DATE)
				.to(TEST_END_DATE));
		assertEquals(expected.toString(), result.toString());
	}
	
	@Test
	public void whenDateRangePredicateStartExcluded_ThenConstantScoreQueryWithRangeFilter() {
		DateRangePredicate predicate = new DateRangePredicate(MockFeature.FOO, TEST_START_DATE, TEST_END_DATE, false, true);
		QueryBuilder result = builder.build(predicate);
		QueryBuilder expected = QueryBuilders.constantScoreQuery(FilterBuilders.rangeFilter(MockFeature.FOO.getField()).from(TEST_START_DATE)
				.to(TEST_END_DATE).includeLower(false).includeUpper(true));
		assertEquals(expected.toString(), result.toString());
	}
	
	@Test
	public void whenDateRangePredicateEndExcluded_ThenConstantScoreQueryWithRangeFilter() {
		DateRangePredicate predicate = new DateRangePredicate(MockFeature.FOO, TEST_START_DATE, TEST_END_DATE, true, false);
		QueryBuilder result = builder.build(predicate);
		QueryBuilder expected = QueryBuilders.constantScoreQuery(FilterBuilders.rangeFilter(MockFeature.FOO.getField()).from(TEST_START_DATE)
				.to(TEST_END_DATE).includeLower(true).includeUpper(false));
		assertEquals(expected.toString(), result.toString());
	}
	
	@Test
	public void whenDateRangePredicateNoStart_ThenConstantScoreQueryWithRangeFilter() {
		DateRangePredicate predicate = new DateRangePredicate(MockFeature.FOO, null, TEST_END_DATE);
		QueryBuilder result = builder.build(predicate);
		QueryBuilder expected = QueryBuilders.constantScoreQuery(FilterBuilders.rangeFilter(MockFeature.FOO.getField()).to(TEST_END_DATE));
		assertEquals(expected.toString(), result.toString());
	}
	
	@Test
	public void whenDateRangePredicateNoEnd_ThenConstantScoreQueryWithRangeFilter() {
		DateRangePredicate predicate = new DateRangePredicate(MockFeature.FOO, TEST_START_DATE, null);
		QueryBuilder result = builder.build(predicate);
		QueryBuilder expected = QueryBuilders.constantScoreQuery(FilterBuilders.rangeFilter(MockFeature.FOO.getField()).from(TEST_END_DATE));
		assertEquals(expected.toString(), result.toString());
	}
}
