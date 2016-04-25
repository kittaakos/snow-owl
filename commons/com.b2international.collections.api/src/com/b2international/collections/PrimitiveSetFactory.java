/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.collections;

import java.util.Set;

import com.b2international.collections.bytes.ByteCollection;
import com.b2international.collections.bytes.ByteSet;
import com.b2international.collections.ints.IntSet;
import com.b2international.collections.longs.LongCollection;
import com.b2international.collections.longs.LongSet;
import com.google.common.hash.HashFunction;

/**
 * @since 4.7
 */
public interface PrimitiveSetFactory {

	ByteSet newByteOpenHashSet(ByteCollection source);
	
	IntSet newIntOpenHashSet();

	IntSet newIntOpenHashSet(int expectedSize);
	
	LongSet newLongOpenHashSet();

	LongSet newLongOpenHashSet(HashFunction hashFunction);

	LongSet newLongOpenHashSet(int expectedSize);

	LongSet newLongOpenHashSet(int expectedSize, double fillFactor);

	LongSet newLongOpenHashSet(long[] source);

	LongSet newLongOpenHashSet(LongCollection source);
	
	LongSet newUnmodifiableLongSet(LongSet source);
	
	Set<Long> newLongSetToSetAdapter(LongSet source);
}
