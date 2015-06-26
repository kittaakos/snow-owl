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

import java.util.Map;

import com.b2international.snowowl.core.store.index.Administrable;
import com.b2international.snowowl.core.store.index.MappingProvider;

/**
 * @since 5.0
 */
public interface TransactionalIndex extends Administrable<TransactionalIndexAdmin>, MappingProvider {

	/**
	 * Loads the latest revision (Map of String, Object value pairs) from the index with the given type and key as identifier.
	 * 
	 * @param type
	 *            - the type of the data object held by the returned revision
	 * @param key
	 *            - the ID of the data object held by the returned revision
	 * @return the loaded data object, holding all values of the revision
	 */
	Map<String, Object> loadRevision(String type, String branchPath, String key);

	// TODO Map<String, Object> loadRevision(String type, String branchPath, long timestamp, String key);

	/**
	 * Adds a revision of a given type to the index using the given commitId and commitTimestamp as revision properties.
	 * 
	 * @param commitId
	 *            - commit group this revision belongs to
	 * @param commitTimestamp
	 *            - the commit group's timestamp
	 * @param type
	 *            - the type of the data object
	 * @param data
	 *            - the data object itself
	 */
	void addRevision(int commitId, long commitTimestamp, String branchPath, String type, Map<String, Object> data);

	/**
	 * Marks the latest revision of the object identified by the given key as deleted and reindexes it.
	 * 
	 * @param commitId
	 *            - commit group id this revision belongs to
	 * @param commitTimestamp
	 *            - the commit group's timestamp
	 * @param branchPath
	 * @param type
	 * @param key
	 *            - the unique identifier of the component, which has been deleted since the latest change processing
	 */
	void remove(int commitId, long commitTimestamp, String branchPath, String type, String key);

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
