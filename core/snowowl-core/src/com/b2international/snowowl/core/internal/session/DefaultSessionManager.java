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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentMap;

import org.elasticsearch.common.collect.MapMaker;

import com.b2international.snowowl.core.session.Session;
import com.b2international.snowowl.core.session.SessionManager;

/**
 * @since 5.0
 */
public class DefaultSessionManager implements SessionManager {

	private ConcurrentMap<String, Session> sessions = new MapMaker().makeMap(); 
	
	private SecureRandom rnd = new SecureRandom();
	
	String generate() {
		return new BigInteger(128, rnd).toString(32);
	}
	
	@Override
	public void login(String user, String password) {
		// TODO authenticate user with currently configured authenticator
		sessions.putIfAbsent(user, new DefaultSession(user, generate()));
	}

	@Override
	public void logout(String user) {
		// TODO remove all sessions, except remote job ones
		sessions.remove(user);
	}
	
}
