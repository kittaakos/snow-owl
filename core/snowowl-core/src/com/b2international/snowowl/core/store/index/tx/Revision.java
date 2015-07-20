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

import static com.google.common.collect.Sets.newHashSet;
import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.nestedFilter;
import static org.elasticsearch.index.query.FilterBuilders.notFilter;
import static org.elasticsearch.index.query.FilterBuilders.orFilter;
import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;

import java.util.Collection;

import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.google.common.collect.ImmutableList;


/**
 * @since 5.0
 */
public abstract class Revision {

	public static final String ADD_REPLACED_BY_ENTRY_SCRIPT_KEY = "addReplacedByEntryScript";
	public static final String ADD_REPLACED_BY_ENTRY_SCRIPT = "ctx._source.replacedIns += [branchPath: branch, commitTimestamp: timestamp]";
	public static final String STORAGE_KEY = "storageKey";
	public static final String BRANCH_PATH = "branchPath";
	public static final String COMMIT_TIMESTAMP = "commitTimestamp";

	private long storageKey;
	private long commitTimestamp;
	private String branchPath;
	private Collection<ReplacedIn> replacedIns = newHashSet();
	
	public void setBranchPath(String branchPath) {
		this.branchPath = branchPath;
	}
	
	public void setCommitTimestamp(long createdTimestamp) {
		this.commitTimestamp = createdTimestamp;
	}
	
	public void setStorageKey(long storageKey) {
		this.storageKey = storageKey;
	}
	
	public void setReplacedIns(Collection<ReplacedIn> replacedIns) {
		this.replacedIns = replacedIns;
	}
	
	public long getStorageKey() {
		return storageKey;
	}
	
	public Collection<ReplacedIn> getReplacedIns() {
		return ImmutableList.copyOf(replacedIns);
	}
	
	public String getBranchPath() {
		return branchPath;
	}
	
	public long getCommitTimestamp() {
		return commitTimestamp;
	}

	public static FilterBuilder createBranchFilter(BranchManager branchManager, String branchPath) {
		return FilterBuilders.andFilter(branchRevisionFilter(branchManager, branchPath), notReplacedInFilter(branchManager, branchPath));
	}

	private static FilterBuilder notReplacedInFilter(BranchManager branchManager, String branchPath) {
		return notFilter(nestedFilter("replacedIns", createBranchSegmentFilter(branchPath, new ReplacedSegmentFilterBuilder(branchManager))));
	}

	private static FilterBuilder branchRevisionFilter(BranchManager branchManager, String branchPath) {
		return createBranchSegmentFilter(branchPath, new RevisionSegmentFilterBuilder(branchManager));
	}

	private static FilterBuilder createBranchSegmentFilter(String branchPath, SegmentFilterBuilder builder) {
		final String[] segments = branchPath.split(Branch.SEPARATOR);
		if (segments.length > 1) {
			final OrFilterBuilder or = orFilter();
			String prev = "";
			for (int i = 0; i < segments.length; i++) {
				final String segment = segments[i];
				// we need the current segment + prevSegment to make it full path and the next one to restrict head timestamp on current based on base of the next one
				String current = "";
				String next = null;
				if (!Branch.MAIN_PATH.equals(segment)) {
					current = prev.concat(Branch.SEPARATOR);
				}
				// if not the last segment, compute next one
				current = current.concat(segment);
				if (!segments[segments.length - 1].equals(segment)) {
					if (!current.endsWith(Branch.SEPARATOR)) {
						next = current.concat(Branch.SEPARATOR);
					}
					next = next.concat(segments[i+1]);
				}
				or.add(builder.createSegmentFilter(current, next));
				prev = current;
			}
			return or;
		} else {
			return builder.createSegmentFilter(branchPath, null);
		}
	}

	private static interface SegmentFilterBuilder {
		
		FilterBuilder createSegmentFilter(String currentBranch, String nextBranch);
		
	}
	
	private static class RevisionSegmentFilterBuilder implements SegmentFilterBuilder {

		private BranchManager branchManager;

		public RevisionSegmentFilterBuilder(BranchManager branchManager) {
			this.branchManager = branchManager;
		}
		
		@Override
		public FilterBuilder createSegmentFilter(String currentBranch, String nextBranch) {
			final FilterBuilder currentBranchFilter = termFilter(Revision.BRANCH_PATH, currentBranch);
			final FilterBuilder commitTimestampFilter = nextBranch == null ? timestampFilter(branchManager, currentBranch) : timestampFilter(branchManager, currentBranch, nextBranch);
			return andFilter(currentBranchFilter, commitTimestampFilter);
		}
		
		/*restricts given branchPath's HEAD to baseTimestamp of child*/
		private static FilterBuilder timestampFilter(BranchManager branchManager, String parentBranchPath, String childToRestrict) {
			final long baseTimestamp = branchManager.getBranch(childToRestrict).baseTimestamp();
			return timestampFilter(branchManager, parentBranchPath, baseTimestamp);
		}
		
		private static FilterBuilder timestampFilter(BranchManager branchManager, String branchPath) {
			final long headTimestamp = branchManager.getBranch(branchPath).headTimestamp();
			return timestampFilter(branchManager, branchPath, headTimestamp);
		}
		
		private static FilterBuilder timestampFilter(BranchManager branchManager, String branchPath, long headTimestamp) {
			final long baseTimestamp = branchManager.getBranch(branchPath).baseTimestamp();
			return rangeFilter(Revision.COMMIT_TIMESTAMP).gte(baseTimestamp).lte(headTimestamp);
		}
		
	}
	
	private static class ReplacedSegmentFilterBuilder implements SegmentFilterBuilder {

		private BranchManager branchManager;

		public ReplacedSegmentFilterBuilder(BranchManager branchManager) {
			this.branchManager = branchManager;
		}
		
		@Override
		public FilterBuilder createSegmentFilter(String currentBranch, String nextBranch) {
			final long maxHead = nextBranch != null ? branchManager.getBranch(nextBranch).baseTimestamp() : Long.MAX_VALUE;
			final long head = Math.min(maxHead, branchManager.getBranch(currentBranch).headTimestamp());
			return andFilter(termFilter("replacedIns."+Revision.BRANCH_PATH, currentBranch), rangeFilter("replacedIns."+Revision.COMMIT_TIMESTAMP).gte(0L).lte(head));
		}
		
	}
	
}
