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
package com.b2international.commons.pcj;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.b2international.commons.collections.primitive.map.IntKeyMap;
import com.b2international.commons.collections.primitive.map.IntKeyMapIterator;
import com.b2international.commons.collections.primitive.set.IntSet;

/**
 * Type safe set multimap implementation using primitive integer keys. 
 */
public class IntKeySetMultimap<V> {

	private final IntKeyMap<Set<V>> map;

	public IntKeySetMultimap() {
		this(PrimitiveCollections.<Set<V>>newIntKeyOpenHashMap());
	}

	public IntKeySetMultimap(IntKeyMap<Set<V>> map) {
		this.map = map;
	}

	public void clear() {
		map.clear();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public int size() {
		return map.size();
	}

	public void trimToSize() {
		map.trimToSize();
	}

	public boolean containsKey(int key) {
		return map.containsKey(key);
	}

	public IntKeySetMultimap<V> dup() {
		return new IntKeySetMultimap<V>(map.dup());
	}

	public IntSet keySet() {
		return map.keySet();
	}

	public IntKeyMapIterator<Set<V>> mapIterator() {
		return map.mapIterator();
	}

	public Set<V> remove(int key) {
		return map.remove(key);
	}

	public Collection<Set<V>> values() {
		return map.values();
	}
	
	public boolean put(final int key, final V value) {
		Set<V> values = delegateGet(key);
		
		if (values == null) {
			values = newHashSet();
			delegatePut(key, values);
		}
		
		return values.add(value);
	}
	
	public Set<V> get(final int key) {
		Set<V> values = delegateGet(key);
		return (values != null) ? Collections.<V>unmodifiableSet(values) : Collections.<V>emptySet();
	}
	
	private Set<V> delegatePut(int key, Set<V> value) {
		return map.put(key, value);
	}

	private Set<V> delegateGet(int key) {
		return map.get(key);
	}
}
