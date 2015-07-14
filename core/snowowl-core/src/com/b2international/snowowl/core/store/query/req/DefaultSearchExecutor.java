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
package com.b2international.snowowl.core.store.query.req;

import static com.google.common.base.Preconditions.checkNotNull;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;

import com.b2international.commons.ClassUtils;
import com.b2international.commons.exceptions.FormattedRuntimeException;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.store.query.DefaultQueryBuilder;
import com.b2international.snowowl.core.store.query.ElasticsearchQueryBuilder;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.store.query.SortBy;
import com.b2international.snowowl.core.store.query.SortBy.MultiSortBy;
import com.b2international.snowowl.core.store.query.SortBy.SortByField;

/**
 * TODO move to internal package
 * 
 * @since 5.0
 */
public class DefaultSearchExecutor implements SearchExecutor {

	private static final Logger LOG = Loggers.REPOSITORY.log();
	protected final ElasticsearchQueryBuilder elasticQueryBuilder = new ElasticsearchQueryBuilder();

	private SearchResponseProcessor processor;
	
	public DefaultSearchExecutor(SearchResponseProcessor processor) {
		this.processor = checkNotNull(processor, "processor");
	}
	
	@Override
	public <T> Iterable<T> execute(SearchRequestBuilder req, AfterWhereBuilder builder, Class<T> resultType) {
		buildQuery(req, builder);
		// TODO async responses, async response processing???, convert Iterable to Observable from RX Java???
		LOG.trace("Executing query: {}", req);
		final SearchResponse response = req.get();
		if (response.getFailedShards() > 0) {
			throw new FormattedRuntimeException("Failed to execute query '%s': %s", req, response);
		}
		return processor.process(response, resultType);
	}

	protected void buildQuery(SearchRequestBuilder req, final AfterWhereBuilder builder) {
		final DefaultQueryBuilder qb = ClassUtils.checkAndCast(builder, DefaultQueryBuilder.class);
		req.setQuery(getQuery(qb)).setFrom(qb.getOffset()).setSize(qb.getLimit());
		final SortBy sortBy = qb.getSortBy();
		if (sortBy instanceof SortByField) {
			req.addSort(((SortByField) sortBy).getField(), getSortMode((SortByField) sortBy));
		} else if (sortBy instanceof MultiSortBy) {
			for (SortBy sort : ((MultiSortBy) sortBy).getItems()) {
				req.addSort(((SortByField) sort).getField(), getSortMode((SortByField) sort));
			}
		}
	}
	
	protected QueryBuilder getQuery(final DefaultQueryBuilder qb) {
		return elasticQueryBuilder.build(qb.getWhere());
	}
	
	protected SortOrder getSortMode(SortByField sortBy) {
		switch (sortBy.getOrder()) {
		case ASC:
			return SortOrder.ASC;
		case DESC:
			return SortOrder.DESC;
		default:
			throw new IllegalArgumentException("Unknown sort mode: " + sortBy.getOrder());
		}
	}

}
