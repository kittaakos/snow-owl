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
package com.b2international.snowowl.core.event.util;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Method;
import java.util.Collections;

import org.eclipse.xtext.util.PolymorphicDispatcher;
import org.elasticsearch.common.base.Strings;

import com.b2international.snowowl.core.event.Event;
import com.b2international.snowowl.core.exceptions.ApiException;
import com.b2international.snowowl.core.exceptions.NotImplementedException;
import com.b2international.snowowl.core.exceptions.SnowOwlException;
import com.b2international.snowowl.core.session.Session;
import com.b2international.snowowl.core.session.SessionContext;
import com.b2international.snowowl.core.session.SessionManager;
import com.b2international.snowowl.eventbus.IHandler;
import com.b2international.snowowl.eventbus.IMessage;
import com.google.common.base.Predicate;

/**
 * @since 4.1
 */
public abstract class ApiEventHandler implements IHandler<IMessage> {

	private PolymorphicDispatcher<Object> handlerDispatcher = new PolymorphicDispatcher<Object>(Collections.singletonList(this), new Predicate<Method>() {
		@Override
		public boolean apply(Method input) { 
			return input.getAnnotation(Handler.class) != null; 
		}
	}, new PolymorphicDispatcher.DefaultErrorHandler<Object>() {
		@Override
		public Object handle(Object[] params, Throwable e) {
			if (e instanceof NoSuchMethodException) {
				throw new NotImplementedException("Event handling not implemented: %s", params, e);
			}
			return super.handle(params, e);
		}
	});
	
	private SessionManager sessionManager;
	
	protected ApiEventHandler(SessionManager sessionManager) {
		this.sessionManager = checkNotNull(sessionManager, "sessionManager");
	}
	
	@Override
	public final void handle(IMessage message) {
		try {
			// TODO support serializationed forms
			final Object body = message.body();
			checkState(body instanceof Event, "Message body should be an instance of Event");
			final String sessionId = ((Event) body).metadata().getString(Event.Headers.SESSION_ID);
			
			if (Strings.isNullOrEmpty(sessionId)) {
				throw new SnowOwlException("Missing session identifier when handling event '%s'", message);
			}
			
			final Session session = sessionManager.getSession(sessionId);
			if (session == null) {
				throw new SnowOwlException("No session found for identifier '%s'", sessionId);
			}
			SessionContext.setSession(session);
			
			message.reply(handlerDispatcher.invoke(body));
		} catch (ApiException e) {
			message.fail(e);
		} finally {
			SessionContext.setSession(null);
		}
	}
	
}
