/*
 * Copyright 2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.datastore.request.job;

import com.b2international.index.query.Expression;
import com.b2international.index.query.Expressions;
import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.events.BaseRequest;
import com.b2international.snowowl.datastore.remotejobs.RemoteJobTracker;
import com.b2international.snowowl.datastore.remotejobs.RemoteJobs;

/**
 * @since 5.7
 */
final class SearchJobRequest extends BaseRequest<ServiceProvider, RemoteJobs> {

	// TODO add filters, paging, expand options
	
	SearchJobRequest() {
	}
	
	@Override
	public RemoteJobs execute(ServiceProvider context) {
		final Expression query = Expressions.matchAll();
		final int offset = 0;
		final int limit = Integer.MAX_VALUE;
		return context.service(RemoteJobTracker.class).search(query, offset, limit);
	}

	@Override
	protected Class<RemoteJobs> getReturnType() {
		return RemoteJobs.class;
	}

}
