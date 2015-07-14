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
import static com.google.common.collect.Maps.newHashMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;

import com.b2international.commons.reflect.MethodInvokerUtil;
import com.b2international.snowowl.core.exceptions.SnowOwlException;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * @since 5.0
 */
public class AsyncMultiIndexSearchExecutor extends DefaultSearchExecutor {

	private List<String> indexes;
	private Client client;
	private String firstIndex;
	private NegatingSearchExecutor negatingExecutor;

	public AsyncMultiIndexSearchExecutor(Client client, LinkedList<String> indexes, ObjectMapper mapper) {
		super(new DefaultSearchResponseProcessor(mapper));
		this.client = client;
		this.indexes = Lists.reverse(indexes);
		this.firstIndex = this.indexes.get(0);
		this.negatingExecutor = new NegatingSearchExecutor(getProcessor());
	}
	
	@Override
	public <T> Iterable<T> execute(SearchRequestBuilder req, AfterWhereBuilder builder, Class<T> resultType) {
		try {
			final Method idGetter = checkNotNull(resultType.getMethod("getStorageKey"), "Type must be subclass of Revision");
			final String[] types = req.request().types();
			final Map<Long, T> results = newHashMap();
			// execute index search in reverse order, starting with the base -> first fork point
			LinkedBlockingDeque<ListenableFuture<Iterable<T>>> positiveFutureDeque = Queues.newLinkedBlockingDeque();
			LinkedBlockingDeque<ListenableFuture<Iterable<String>>> negativeFutureDeque = Queues.newLinkedBlockingDeque();
			// quick hack to remove the offset between positive and negative queues
			negativeFutureDeque.addLast(Futures.<Iterable<String>>immediateFuture(Collections.<String>emptySet()));
			for (String index : indexes) {
				Stopwatch watch = Stopwatch.createStarted();
				final SearchRequestBuilder newReq = client.prepareSearch(index).setTypes(types);
				ListenableFuture<Iterable<T>> resultFuture = super.executeAsync(newReq, builder, resultType);
				positiveFutureDeque.addLast(resultFuture);
				if (!firstIndex.equals(index)) {
					// run two queries if not the first index, the original and one negated
					ListenableFuture<Iterable<String>> negativeResultFuture = negatingExecutor.executeAsync(newReq, builder, String.class);
					negativeFutureDeque.addLast(negativeResultFuture);
				}
				System.out.println(String.format("Sent '%s' search request in %s", index, watch));
			}
			
			while (!positiveFutureDeque.isEmpty()) {
				Stopwatch watch = Stopwatch.createStarted();
				ListenableFuture<Iterable<T>> listenableFuture = positiveFutureDeque.poll();
				Iterable<T> positiveResults = listenableFuture.get();
				// put and replace all new results
				results.putAll(createIndex(idGetter, positiveResults));
				ListenableFuture<Iterable<String>> negativeListenableFuture = negativeFutureDeque.poll();
				Iterable<String> negativeResults = negativeListenableFuture.get();
				for (String result : negativeResults) {
					results.remove(Long.valueOf(result));
				}
				System.out.println(String.format("Processed index +/- responses in %s", watch));
			}
			return ImmutableSet.copyOf(results.values());
		} catch (Exception e) {
			throw new SnowOwlException("Executor failed.", e);
		}
	}

	private <T> Map<Long, T> createIndex(final Method getter, Iterable<T> results) {
		return Maps.uniqueIndex(results, new Function<T, Long>() {
			@Override
			public Long apply(T input) {
				try {
					return (Long) MethodInvokerUtil.invoke(getter, input);
				} catch (InvocationTargetException e) {
					throw new SnowOwlException("Failed to call %s", getter.getName());
				}
			}
		});
	}

}
