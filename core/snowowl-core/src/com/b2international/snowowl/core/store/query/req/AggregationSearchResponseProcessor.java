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

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;

import com.b2international.snowowl.core.store.index.tx.Revision;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;


/**
 * @since 5.0
 */
public class AggregationSearchResponseProcessor implements SearchResponseProcessor {

	private ObjectMapper mapper;

	public AggregationSearchResponseProcessor(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	@Override
	public <T> Iterable<T> process(SearchResponse response, Class<T> resultType) {
		final Map<Long, T> latestRevisions = newHashMap();
		final Terms agg = response.getAggregations().get(AggregatingBranchSearchExecutor.STORAGE_KEY_GROUPING_AGG);
		for (Terms.Bucket bucket : agg.getBuckets()) {
			final TopHits topHits = bucket.getAggregations().get(AggregatingBranchSearchExecutor.TOP_HIT_DOCS_AGG);
			final Long storageKey = Long.valueOf(bucket.getKey());
			final Map<String, Object> source = topHits.getHits().hits()[0].getSource();
			final boolean deleted = (boolean) source.get(Revision.DELETED);
			if (!deleted) {
				latestRevisions.put(storageKey, mapper.convertValue(source, resultType));
			}
		}
		return ImmutableList.copyOf(latestRevisions.values());
	}
	
}
