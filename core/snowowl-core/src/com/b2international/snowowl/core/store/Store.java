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

import java.util.Collection;

import com.b2international.snowowl.core.store.query.Query;

/**
 * @since 4.1
 * @param <T>
 *            - the type of the objects to store
 */
public interface Store<T> {

	/**
	 * Puts the value into this store.
	 * 
	 * @param value
	 */
	void put(T value);

	/**
	 * Returns the stored value for the given key.
	 * 
	 * @param key
	 * @return
	 */
	T get(String key);

	/**
	 * Completely removes the stored value on the given key from this {@link Store}.
	 * 
	 * @param key
	 * @return
	 */
	T remove(String key);

	/**
	 * Replaces the oldValue with the given newValue if and only if the oldValue and the newValue have the exact same ID, and the oldValue does not
	 * equal to the newValue, and the oldValue does equal to the currently hold value. If any of the above fails the this method return
	 * <code>false</code> otherwise replaces the value and returns <code>true</code>. Note that the {@link #put(Object)} method may replace values in
	 * the same manner as this method without checking the equality of the currently held value.
	 * 
	 * @param oldValue
	 *            - the currently held value
	 * @param newValue
	 *            - the new value
	 * @return - <code>true</code> or <code>false</code> depending on whether the replacement succeeded or not
	 */
	boolean replace(T oldValue, T newValue);

	/**
	 * Returns all currently persisted values from the store.
	 * 
	 * @return
	 */
	Collection<T> values();

	/**
	 * Clears the store by completely removing all currently persisted values.
	 */
	void clear();

	Collection<T> search(Query query);

	Collection<T> search(Query query, int offset, int limit);

	/**
	 * Returns the name of the store.
	 * 
	 * @return
	 */
	String getName();

}
