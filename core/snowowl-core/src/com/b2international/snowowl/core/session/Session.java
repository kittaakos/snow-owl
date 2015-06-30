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

import com.b2international.snowowl.core.repository.Repository;
import com.b2international.snowowl.core.repository.RepositorySession;

/**
 * Represents a global application level session for a single logged in user.
 * 
 * @since 5.0
 */
public interface Session {

	/**
	 * Returns the user name.
	 * 
	 * @return
	 */
	String getUser();

	/**
	 * Returns the application generated unique session identifier.
	 * 
	 * @return
	 */
	String getSessionId();

	/**
	 * Returns a {@link RepositorySession} from the given {@link Repository} for the user represented by this {@link Session} object.
	 * 
	 * @param repository
	 *            - the repository where the user needs a session
	 * @return the {@link RepositorySession} instance
	 */
	RepositorySession getRepositorySession(Repository repository);

}
