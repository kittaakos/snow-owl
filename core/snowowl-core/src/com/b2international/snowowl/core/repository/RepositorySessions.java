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
package com.b2international.snowowl.core.repository;

import java.util.Collection;

import com.b2international.snowowl.core.session.Session;

/**
 * @since 5.0
 */
public interface RepositorySessions {

	/**
	 * Opens a {@link RepositorySession} for the given user name or returns an already existing one.
	 * 
	 * @param user
	 *            - the user name
	 * @param token
	 *            - application generated unique token
	 * @return a {@link RepositorySession} instance
	 */
	RepositorySession open(String user, char[] token);

	/**
	 * Opens a {@link RepositorySession} for the given global application level session or returns an existing one, if already exists.
	 * 
	 * @param session
	 *            - the session to create {@link RepositorySession} for
	 * @return a {@link RepositorySession} instance
	 */
	RepositorySession open(Session session);

	/**
	 * Returns all opened sessions.
	 * 
	 * @return
	 */
	Collection<RepositorySession> getSessions();

}
