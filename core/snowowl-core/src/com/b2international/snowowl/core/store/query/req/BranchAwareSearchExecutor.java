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
package com.b2international.snowowl.core.store.query.req;

import static com.google.common.base.Preconditions.checkNotNull;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.b2international.commons.ClassUtils;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.store.index.tx.DefaultTransactionalQueryBuilder;
import com.b2international.snowowl.core.store.index.tx.Revision;
import com.b2international.snowowl.core.store.query.DefaultQueryBuilder;

/**
 * @since 5.0
 */
public class BranchAwareSearchExecutor extends DefaultSearchExecutor {

	private BranchManager branchManager;

	public BranchAwareSearchExecutor(SearchResponseProcessor processor, BranchManager branchManager) {
		super(processor);
		this.branchManager = checkNotNull(branchManager, "BranchManager may not be null");
	}

	@Override
	protected QueryBuilder getQuery(DefaultQueryBuilder builder) {
		final DefaultTransactionalQueryBuilder qb = ClassUtils.checkAndCast(builder, DefaultTransactionalQueryBuilder.class);
		return QueryBuilders.filteredQuery(super.getQuery(qb), Revision.createBranchFilter(branchManager, qb.getBranchPath()));
	}

}
