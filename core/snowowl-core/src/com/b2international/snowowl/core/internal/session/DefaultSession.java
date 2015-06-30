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
package com.b2international.snowowl.core.internal.session;

import com.b2international.snowowl.core.repository.Repository;
import com.b2international.snowowl.core.repository.RepositorySession;
import com.b2international.snowowl.core.session.Session;

/**
 * @since 5.0
 */
public class DefaultSession implements Session {

	private final String user;
	private final String sessionId;

	DefaultSession(String user, String sessionId) {
		this.user = user;
		this.sessionId = sessionId;
	}
	
	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}
	
	@Override
	public RepositorySession getRepositorySession(Repository repository) {
		return repository.sessions().open(this);
	}
	
}
