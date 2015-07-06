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
package com.b2international.snowowl.core.store.index.tx;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test the serialization of {@link DefaultIndexRevision} objects.
 * 
 * @since 5.0
 */
public class IndexRevisionTest {

	private ObjectMapper mapper;
	
	@Before
	public void givenMapper() {
		mapper = new ObjectMapper();
		// any non-private setter visibility is accepted
		mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NON_PRIVATE);
	}
	
	@Test(expected = NullPointerException.class)
	public void createRevisionWithNullDataShouldThrowException() throws Exception {
		new DefaultIndexRevision(0, 0L, 1L, false, null);
	}
	
	@Test
	public void testSerializationOfIndexRevision() throws Exception {
		final Map<String, Object> data = newHashMap();
		data.put("prop", "value");
		final IndexRevision rev = new DefaultIndexRevision(0, 0L, 1L, false, data);
		
		final String json = mapper.writeValueAsString(rev);
		assertEquals("{\"commitId\":0,\"commitTimestamp\":0,\"storageKey\":1,\"deleted\":false,\"prop\":\"value\"}", json);
		
		final IndexRevision revDeserialized = mapper.readValue(json, DefaultIndexRevision.class);
		assertEquals(rev, revDeserialized);
	}
	
}
