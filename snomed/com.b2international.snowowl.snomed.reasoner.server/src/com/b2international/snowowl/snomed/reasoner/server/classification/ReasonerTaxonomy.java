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
package com.b2international.snowowl.snomed.reasoner.server.classification;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.List;

import com.b2international.collections.PrimitiveLists;
import com.b2international.collections.PrimitiveMaps;
import com.b2international.collections.PrimitiveSets;
import com.b2international.collections.longs.LongCollections;
import com.b2international.collections.longs.LongIterator;
import com.b2international.collections.longs.LongKeyMap;
import com.b2international.collections.longs.LongList;
import com.b2international.collections.longs.LongSet;
import com.b2international.snowowl.core.api.IBranchPath;

/**
 */
public class ReasonerTaxonomy implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final List<LongSet> equivalentConceptIds = newArrayList();
	private final LongSet unsatisfiableConceptIds = PrimitiveSets.newLongOpenHashSet();
	private final LongKeyMap<LongSet> parentIds = PrimitiveMaps.newLongKeyOpenHashMap();
	private final LongKeyMap<LongSet> ancestorIds = PrimitiveMaps.newLongKeyOpenHashMap();
	private final LongList insertionOrderedIds = PrimitiveLists.newLongArrayList();

	private final IBranchPath branchPath;
	private final long elapsedTimeMillis;
	private volatile boolean stale = false;
	
	public ReasonerTaxonomy(final IBranchPath branchPath, final long elapsedTimeMillis) {
		this.branchPath = branchPath;
		this.elapsedTimeMillis = elapsedTimeMillis;
	}
	
	public IBranchPath getBranchPath() {
		return branchPath;
	}
	
	public long getElapsedTimeMillis() {
		return elapsedTimeMillis;
	}

	public void setStale() {
		this.stale = true;
	}
	
	public boolean isStale() {
		return stale;
	}
	
	public void addEquivalentConceptIds(final LongSet conceptIds) {
		equivalentConceptIds.add(PrimitiveSets.newLongOpenHashSet(conceptIds));
	}
	
	public List<LongSet> getEquivalentConceptIds() {
		return equivalentConceptIds;
	}

	public void addUnsatisfiableConceptIds(final LongSet conceptIds) {
		unsatisfiableConceptIds.addAll(conceptIds);
	}
	
	public LongSet getUnsatisfiableConceptIds() {
		return unsatisfiableConceptIds;
	}

	public void addEntry(final ReasonerTaxonomyEntry entry) {
		insertionOrderedIds.add(entry.getSourceId());
		getOrCreateSet(parentIds, entry.getSourceId()).addAll(entry.getParentIds());
		getOrCreateSet(ancestorIds, entry.getSourceId()).addAll(entry.getParentIds());
		for (final LongIterator itr = entry.getParentIds().iterator(); itr.hasNext(); /* empty */) {
			getOrCreateSet(ancestorIds, entry.getSourceId()).addAll(getOrCreateSet(ancestorIds, itr.next()));
		}
	}

	private LongSet getOrCreateSet(final LongKeyMap<LongSet> map, final long key) {
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			final LongSet newSet = PrimitiveSets.newLongOpenHashSet();
			map.put(key, newSet);
			return newSet;
		}
	}

	private LongSet getOrReturnEmptySet(final LongKeyMap<LongSet> map, final long key) {
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			return LongCollections.emptySet();
		}
	}
	
	public LongSet getParents(final long sourceId) {
		return getOrReturnEmptySet(parentIds, sourceId);
	}
	
	public LongSet getAncestors(final long sourceId) {
		return getOrReturnEmptySet(ancestorIds, sourceId);
	}
	
	public LongList getConceptIds() {
		return insertionOrderedIds;
	}
}