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
import static com.google.common.collect.Sets.newHashSet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

/**
 * @since 5.0
 */
public class MultiIndexSearchExecutor extends DefaultSearchExecutor {

	private List<String> indexes;
	private Client client;
	private String firstIndex;
	private NegatingSearchExecutor negatingExecutor;

	public MultiIndexSearchExecutor(Client client, LinkedList<String> indexes, ObjectMapper mapper) {
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
			for (String index : indexes) {
				Stopwatch watch = Stopwatch.createStarted();
				final SearchRequestBuilder newReq = client.prepareSearch(index).setTypes(types);
				final Iterable<T> positiveResults = newHashSet(super.execute(newReq, builder, resultType));
				// put and replace all new results
				results.putAll(createIndex(idGetter, positiveResults));
				if (!firstIndex.equals(index)) {
					final Iterable<String> negativeResults = newHashSet(negatingExecutor.execute(newReq, builder, String.class));
					// run two queries if not the first index, the original and one negated
					for (String result : negativeResults) {
						results.remove(Long.valueOf(result));
					}
				}
				System.out.println(String.format("Processed '%s' index request in %s", index, watch));
			}
			return ImmutableSet.copyOf(results.values());
		} catch (Exception e) {
			throw new SnowOwlException("Failed to get idgetter %s", resultType.getName());
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
