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
package com.b2international.snowowl.core.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.b2international.snowowl.core.store.Types.Data;

/**
 * @since 5.0
 */
public abstract class BaseStoreTest {

	private static final String KEY = "key";
	private static final String KEY2 = "key2";
	
	private Store<Data> store;
	
	@Before
	public void givenStore() {
		store = createStore(Data.class);
	}
	
	@Test
	public void whenGettingMissingData_ThenThrowNotFoundException() throws Exception {
		assertNull(store.get(KEY));
	}
	
	@Test
	public void whenStoringData_ThenItCanBeRetrieved() throws Exception {
		final Data value = storeData(KEY);
		final Data actual = store.get(KEY);
		assertEquals(value, actual);
	}
	
	@Test
	public void whenStoringDataOnSameKey_ThenReplaceData() throws Exception {
		storeData(KEY);
		final Data newData = newData(KEY);
		store.put(newData);
		assertEquals(newData, store.get(KEY));
	}

	@Test
	public void whenRemovingDataFromStore_ThenItShouldBeRemoved() throws Exception {
		final Data value = storeData(KEY);
		final Data removed = store.remove(KEY);
		assertEquals(value, removed);
		assertNull(store.get(KEY));
	}
	
	@Test
	public void whenStoringMultipleData_ThenAllCanBeRetrievedViaValues() throws Exception {
		final Data value = storeData(KEY);
		final Data value2 = storeData(KEY2);
		assertThat(store.values()).containsOnly(value, value2);
	}
	
	@Test
	public void whenClearingStoredData_ThenValuesShouldReturnEmptyCollection() throws Exception {
		whenStoringMultipleData_ThenAllCanBeRetrievedViaValues();
		store.clear();
		assertThat(store.values()).isEmpty();
	}
	
	@Test
	public void whenReplacingDataWithSameValue_ThenDoNothing() throws Exception {
		Data value = storeData(KEY);
		assertFalse(store.replace(value, value));
	}
	
	@Test
	public void whenReplacingDataWithInvalidOldValue_ThenDoNothing() throws Exception {
		storeData(KEY);
		final Data invalidOldValue = newData(KEY);
		assertFalse(store.replace(invalidOldValue, newData(KEY)));
	}
	
	@Test
	public void whenReplacingDataWithNewValue_ThenReplaceDataAndRetrieve() throws Exception {
		final Data value = storeData(KEY);
		final Data newData = newData(KEY);
		assertTrue(store.replace(value, newData));
		assertEquals(newData, store.get(KEY));
	}
	
	private Data storeData(String key) {
		final Data value = newData(key);
		store.put(value);
		return value;
	}

	private static Data newData(String id) {
		return new Data(id, UUID.randomUUID().toString());
	}

	protected abstract <T> Store<T> createStore(Class<T> type);
	
}
