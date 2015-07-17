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
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;

import java.io.IOException;
import java.util.Collection;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import com.b2international.snowowl.core.exceptions.SnowOwlException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Response processor implementation that converts each {@link SearchHit} into the desired resultType with an {@link ObjectMapper}.
 * 
 * @since 5.0
 */
public class DefaultSearchResponseProcessor implements SearchResponseProcessor {

	private ObjectMapper mapper;

	public DefaultSearchResponseProcessor(ObjectMapper mapper) {
		this.mapper = checkNotNull(mapper, "Mapper may not be null");
	}
	
	@Override
	public <T> Iterable<T> process(SearchResponse response, Class<T> resultType) {
		final Collection<T> result = newArrayListWithExpectedSize(response.getHits().getHits().length);
		for (SearchHit hit : response.getHits()) {
			if (String.class == resultType) {
				result.add((T) hit.getId());
			} else {
				try {
					result.add(mapper.readValue(hit.getSourceRef().toBytes(), resultType));
				} catch (IOException e) {
					throw new SnowOwlException("Failed to deserialize response to %s", resultType, e);
				}
			}
		}
		return result;
	}

}
