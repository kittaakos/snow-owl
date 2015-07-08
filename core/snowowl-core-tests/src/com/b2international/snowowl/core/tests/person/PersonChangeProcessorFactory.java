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
package com.b2international.snowowl.core.tests.person;

import org.eclipse.emf.cdo.common.id.CDOIDUtil;

import com.b2international.snowowl.core.repository.cp.ChangeProcessor;
import com.b2international.snowowl.core.repository.cp.ChangeProcessorFactory;
import com.b2international.snowowl.core.repository.cp.CommitChangeSet;
import com.b2international.snowowl.core.store.index.tx.IndexTransaction;
import com.b2international.snowowl.core.store.index.tx.TransactionalIndex;
import com.google.common.collect.Iterables;

/**
 * @since 5.0
 */
public class PersonChangeProcessorFactory implements ChangeProcessor, ChangeProcessorFactory {

	private TransactionalIndex index;
	private IndexTransaction tx;
	private boolean dirty;

	@Override
	public ChangeProcessor create() {
		return this;
	}
	
	public void setIndex(TransactionalIndex index) {
		this.index = index;
	}

	@Override
	public void process(CommitChangeSet changeSet) {
		// FIXME remove this hack and ensure that index is always set, via ctor preferrably
		if (index == null) {
			return;
		}
		final long commitTimestamp = changeSet.getTimestamp();
		final String branchPath = changeSet.getView().getBranch().getPathName();
		tx = index.transaction(changeSet.getView().getViewID(), commitTimestamp, branchPath);
		// TODO implement
		for (person.Person person : Iterables.filter(changeSet.getNewComponents(), person.Person.class)) {
			dirty = true;
			tx.add(CDOIDUtil.getLong(person.cdoID()), Person.of(person));
		}
	}

	@Override
	public void commit() {
		if (tx != null) {
			tx.commit("TODO");
		}
	}

	@Override
	public void rollback() {
		// if needed
//		tx.rollback();
		tx = null;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

}
