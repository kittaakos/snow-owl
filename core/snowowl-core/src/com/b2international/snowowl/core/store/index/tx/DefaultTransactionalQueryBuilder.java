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

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.store.Searchable;
import com.b2international.snowowl.core.store.query.DefaultQueryBuilder;
import com.b2international.snowowl.core.store.query.SortBy;
import com.b2international.snowowl.core.store.query.SortBy.Order;

/**
 * @since 5.0
 */
public class DefaultTransactionalQueryBuilder extends DefaultQueryBuilder implements TransactionalQueryBuilder {

	private String branchPath = Branch.MAIN_PATH;

	DefaultTransactionalQueryBuilder(Searchable searchable) {
		super(searchable);
		sortBy(SortBy.builder().add(IndexCommit.COMMIT_TIMESTAMP_FIELD, Order.DESC).add(Revision.STORAGE_KEY, Order.ASC).build());
	}

	@Override
	public TransactionalQueryBuilder on(String branchPath) {
		this.branchPath = branchPath;
		return this;
	}
	
	public String getBranchPath() {
		return branchPath;
	}

}
