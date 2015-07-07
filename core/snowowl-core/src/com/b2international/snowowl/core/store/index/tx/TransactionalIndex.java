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

import com.b2international.snowowl.core.store.Searchable;
import com.b2international.snowowl.core.store.index.Administrable;
import com.b2international.snowowl.core.store.index.IndexAdmin;
import com.b2international.snowowl.core.store.index.MappingProvider;

/**
 * @since 5.0
 */
public interface TransactionalIndex extends Administrable<IndexAdmin>, MappingProvider, Searchable {

	@Override
	TransactionalQueryBuilder query();

	/**
	 * Adds a revision to the transactional index.
	 * 
	 * @param branchPath
	 *            - the branch to use when adding the revision
	 * @param revision
	 *            - the revision
	 */
	void addRevision(String branchPath, Revision revision);

	/**
	 * Loads the latest revision of an object from the index with the given type and storageKey as identifier.
	 * 
	 * @param type
	 *            - the type of the object
	 * @param branchPath
	 *            - the branchPath to restrict the loading of the revision
	 * @param storageKey
	 *            - the storage identifier of the revision
	 * @return the loaded revision object
	 */
	<T extends Revision> T loadRevision(Class<T> type, String branchPath, long storageKey);

	/**
	 * Indexes a commit group as parent for all previously added revision (with the given commitId) available for search.
	 * 
	 * @param commitId
	 * @param commitTimestamp
	 * @param branchPath
	 * @param commitMessage
	 */
	void commit(int commitId, long commitTimestamp, String branchPath, String commitMessage);

	/**
	 * Opens a new IndexTransaction with the given id and timestamp.
	 * 
	 * @param commitId
	 * @param commitTimestamp
	 * @param branchPath
	 * @return
	 */
	IndexTransaction transaction(int commitId, long commitTimestamp, String branchPath);

}
