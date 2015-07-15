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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;

import com.google.common.collect.AbstractIterator;

/**
 * Elasticsearch {@link SearchHit search hit} iterator implementation backed by a scan type search.
 */
public class ScanningSearchHitIterator extends AbstractIterator<SearchHit> {

	private static final String DEFAULT_KEEP_ALIVE = "1m";
	private static final int DEFAULT_SIZE = 100;
	
	// configured in constructor
	private final Client client;
	private final QueryBuilder queryBuilder;
	private final String indexName;
	private final long timeout;
	
	// configured using setters
	private String keepAlive = DEFAULT_KEEP_ALIVE;
	private int size = DEFAULT_SIZE;
	private String typeName = null;
	private boolean fetchSource = true;
	private List<String> fields = Collections.emptyList();

	// internal state
	private String scrollId;
	private Iterator<SearchHit> searchHitsIterator;
	
	public ScanningSearchHitIterator(Client client, QueryBuilder queryBuilder, String indexName, long timeout) {
		this.client = client;
		this.queryBuilder = queryBuilder;
		this.indexName = indexName;
		this.timeout = timeout;
	}

	public void setKeepAlive(String keepAlive) {
		this.keepAlive = keepAlive;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	public void setFetchSource(boolean fetchSource) {
		this.fetchSource = fetchSource;
	}
	
	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	@Override
	protected SearchHit computeNext() {
		if (scrollId == null) {
			// if the scroll ID is null, then we haven't started scrolling yet
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).
					setTimeout(new TimeValue(timeout)).
					setQuery(queryBuilder).
					setScroll(keepAlive).
					setSize(size).
					setSearchType(SearchType.SCAN).
					setFetchSource(fetchSource).
					addFields(fields.toArray(new String[0]));
			if (typeName != null) {
				searchRequestBuilder.setTypes(typeName);
			}
			SearchResponse scrollResp = searchRequestBuilder.execute().actionGet(timeout);
			scrollId = scrollResp.getScrollId();
			searchHitsIterator = scrollResp.getHits().iterator();
		}
		if (searchHitsIterator.hasNext()) {
			return searchHitsIterator.next();
		} else {
			// the iterator has run out of elements, try to get more by scrolling 
			SearchResponse scrollResp = client.prepareSearchScroll(scrollId).setScroll(keepAlive).execute().actionGet(timeout);
			// get new scrollId
			scrollId = scrollResp.getScrollId();
			searchHitsIterator = scrollResp.getHits().iterator();
			if (searchHitsIterator.hasNext()) {
				return searchHitsIterator.next();
			} else {
				// scrolling provided no additional elements, indicate that there are no more elements left in this iterator
				return endOfData();
			}
		}
	}

}
