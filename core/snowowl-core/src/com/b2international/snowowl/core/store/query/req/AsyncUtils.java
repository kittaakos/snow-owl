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

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ListenableActionFuture;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @since 5.0
 */
abstract public class AsyncUtils {

	/**
	 * Wraps an Elasticsearch {@link ListenableActionFuture} into a Guava {@link ListenableFuture}.
	 * 
	 * @param listenableActionFuture the Elasticsearch {@link ListenableActionFuture} to wrap
	 * @return the Guava {@link ListenableFuture} wrapper
	 */
	public static final <T> ListenableFuture<T> toListenableFuture(ListenableActionFuture<T> listenableActionFuture) {
		final SettableFuture<T> result = SettableFuture.create();
		listenableActionFuture.addListener(new ActionListener<T>() {
			@Override
			public void onResponse(T response) {
				result.set(response);
			}

			@Override
			public void onFailure(Throwable e) {
				result.setException(e);
			}
		});
		return result;
	}
	
	private AsyncUtils() {}
}
