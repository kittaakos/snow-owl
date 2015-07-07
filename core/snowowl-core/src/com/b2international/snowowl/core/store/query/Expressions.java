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
package com.b2international.snowowl.core.store.query;

/**
 * @since 5.0
 */
public class Expressions {

	public static Expression prefixMatch(final String field, final String prefix) {
		return new PrefixPredicate(AnyType.INSTANCE, field(field), prefix);
	}
	
	public static Feature field(final String field) {
		return new Feature() {
			@Override
			public String getField() {
				return field;
			}
		};
	}

	public static Expression exactMatch(String field, String value) {
		return new StringPredicate(AnyType.INSTANCE, field(field), value);
	}
	
	public static Expression exactMatch(String field, Long value) {
		return new LongPredicate(AnyType.INSTANCE, field(field), value);
	}

}
