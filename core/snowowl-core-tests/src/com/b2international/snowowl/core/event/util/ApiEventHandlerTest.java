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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.b2international.snowowl.core.Metadata;
import com.b2international.snowowl.core.event.BaseEvent;
import com.b2international.snowowl.core.event.Event;
import com.b2international.snowowl.core.exceptions.SnowOwlException;
import com.b2international.snowowl.core.session.SessionContext;
import com.b2international.snowowl.core.session.SessionManager;
import com.b2international.snowowl.eventbus.IMessage;

/**
 * @since 5.0
 */
public class ApiEventHandlerTest {

	private SessionManager sessionManager = mock(SessionManager.class, RETURNS_MOCKS);
	
	private IMessage message;
	private Metadata headers;
	private TestEvent event;

	@Before
	public void givenTestEvent() {
		message = mock(IMessage.class);
		event = new TestEvent();
		headers = event.metadata();
		headers.put(Event.Headers.SESSION_ID, "random-session-identifier");
		when(message.body()).thenReturn(event);
	}
	
	@Test
	public void handlerShouldHaveSessionDuringExecution() throws Exception {
		handler.handle(message);
		verify(message, never()).fail(anyObject());
	}
	
	@Test(expected = SnowOwlException.class)
	public void handlerShouldThrowSnowOwlException_InCaseOfMissingSessionId() throws Exception {
		headers.clear();
		handler.handle(message);
		verify(message, never()).fail(anyObject());
	}

	@Test(expected = SnowOwlException.class)
	public void handlerShouldThrowSnowOwlException_InCaseOfNoActiveSessionForSessionId() throws Exception {
		when(sessionManager.getSession(anyString())).thenReturn(null);
		handler.handle(message);
		verify(message, never()).fail(anyObject());
	}
	
	private ApiEventHandler handler = new ApiEventHandler(sessionManager) {
		
		@Handler
		public Object handle(TestEvent event) {
			assertNotNull(SessionContext.getSession());
			return null;
		} 
		
	};
	
	private static class TestEvent extends BaseEvent {

		@Override
		protected String getAddress() {
			return "address";
		}
		
	}
	
}
