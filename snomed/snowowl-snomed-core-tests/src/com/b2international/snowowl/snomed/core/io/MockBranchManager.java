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
package com.b2international.snowowl.snomed.core.io;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;

/**
 * @since 5.0
 */
public class MockBranchManager implements BranchManager {
	
	private Map<String, Branch> branches = newHashMap();
	private Answer<Branch> createChildAnswer = new Answer<Branch>() {
		@Override
		public Branch answer(InvocationOnMock invocation) throws Throwable {
			final Branch parent = (Branch) invocation.getMock();
			final String name = (String) invocation.getArguments()[0];
			final long head = parent.headTimestamp();
			return createMock(parent.path(), name, head, head);
		}
	};
	
	public MockBranchManager() {
		createMock(null, Branch.MAIN_PATH, 0L, 0L);
	}
	
	private Branch createMock(String parent, String name, long base, long head) {
		checkNotNull(name);
		final Branch branch = mock(Branch.class);
		when(branch.name()).thenReturn(name);
		when(branch.baseTimestamp()).thenReturn(base);
		when(branch.headTimestamp()).thenReturn(head);
		final String path = parent == null ? name : parent + Branch.SEPARATOR + name;
		when(branch.path()).thenReturn(path);
		if (parent != null) {
			when(branch.parent()).thenReturn(getBranch(parent));
		}
		when(branch.createChild(anyString())).thenAnswer(createChildAnswer);
		mockRebase(branch);
		branches.put(path, branch);
		return branch;
	}


	private void mockRebase(final Branch branch) {
		when(branch.rebase(anyString())).thenAnswer(new Answer<Branch>() {
			@Override
			public Branch answer(InvocationOnMock invocation) throws Throwable {
				final Branch parent = branch.parent();
				final long head = parent.headTimestamp(); 
				when(branch.baseTimestamp()).thenReturn(head);
				when(branch.headTimestamp()).thenReturn(head);
				// TODO do we have to apply changes to reopened branch, just like in the impl???
				return branch;
			}
		});
	}

	@Override
	public Branch getMainBranch() {
		return getBranch(Branch.MAIN_PATH);
	}

	@Override
	public Branch getBranch(String path) {
		return branches.get(path);
	}

	@Override
	public Collection<? extends Branch> getBranches() {
		return branches.values();
	}

}
