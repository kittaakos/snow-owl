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
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

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
	
	protected final SearchResponseProcessor getProcessor() {
		return processor;
	}
	
	@Override
	public <T> Iterable<T> execute(SearchRequestBuilder req, AfterWhereBuilder builder, Class<T> resultType) {
		buildRequest(req, builder);
		if (resultType == String.class) {
			req.setFetchSource(false);
		}
		// TODO async responses, async response processing
		final SearchResponse response = executeRequest(req);
		return processor.process(response, resultType);
	}
	
	@Override
	public <T> ListenableFuture<Iterable<T>> executeAsync(SearchRequestBuilder req, AfterWhereBuilder builder, final Class<T> resultType) {
		buildRequest(req, builder);
		ListenableFuture<SearchResponse> responseFuture = executeRequestAsync(req);
		return Futures.transform(responseFuture, new Function<SearchResponse, Iterable<T>>() {
			@Override
			public Iterable<T> apply(SearchResponse input) {
				return processor.process(input, resultType);
			}
		});
	}

	protected SearchResponse executeRequest(SearchRequestBuilder req) {
		Stopwatch watch = Stopwatch.createStarted();
		LOG.trace("Executing query: {}", req);
		final SearchResponse response = req.get();
		LOG.info("Executed query in {}", watch);
		if (response.getFailedShards() > 0) {
			throw new FormattedRuntimeException("Failed to execute query '%s': %s", req, response);
		}
		return response;
	}
	
	protected ListenableFuture<SearchResponse> executeRequestAsync(SearchRequestBuilder req) {
		LOG.trace("Executing async query: {}", req);
		return AsyncUtils.toListenableFuture(req.execute());
	}

	protected void buildRequest(SearchRequestBuilder req, final AfterWhereBuilder builder) {
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
