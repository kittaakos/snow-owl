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
package com.b2international.snowowl.core.session;

import static com.google.common.base.Preconditions.checkNotNull;

import com.b2international.snowowl.core.repository.Repository;
import com.b2international.snowowl.core.repository.RepositorySession;

/**
 * @since 5.0
 */
public final class SessionContext {

	private static ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<>();

	public static void setSession(Session session) {
		checkNotNull(session, "Session may not be null");
		sessionThreadLocal.set(session);
	}

	/**
	 * Returns the current {@link Session}.
	 * 
	 * @return
	 */
	public static Session getSession() {
		return sessionThreadLocal.get();
	}

	/**
	 * Returns a {@link RepositorySession} for the currently executing {@link Session}.
	 * 
	 * @param repository
	 * @return
	 */
	public static RepositorySession getRepositorySession(Repository repository) {
		return getSession().getRepositorySession(repository);
	}

}
