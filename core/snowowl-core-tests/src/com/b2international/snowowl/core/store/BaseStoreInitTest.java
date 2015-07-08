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
package com.b2international.snowowl.core.store;

import java.util.Collection;

import org.junit.Test;

import com.b2international.snowowl.core.store.Types.Data;
import com.b2international.snowowl.core.store.Types.TypeWithIdInterfaceSubclass;
import com.b2international.snowowl.core.store.Types.TypeWithIdMethod;
import com.b2international.snowowl.core.store.Types.TypeWithoutIdAnnotation;
import com.b2international.snowowl.core.store.query.Query.AfterWhereBuilder;
import com.b2international.snowowl.core.store.query.Query.QueryBuilder;

/**
 * @since 5.0
 */
public class BaseStoreInitTest {

	@Test(expected = IllegalArgumentException.class)
	public void typeWithoutAnnotation_ThrowException() throws Exception {
		new BaseStore<Types.TypeWithoutIdAnnotation>(Types.TypeWithoutIdAnnotation.class) {
			@Override
			public TypeWithoutIdAnnotation get(String key) { return null; }

			@Override
			public TypeWithoutIdAnnotation remove(String key) { return null; }

			@Override
			public Collection<TypeWithoutIdAnnotation> values() { return null; }

			@Override
			public void clear() {}

			@Override
			public QueryBuilder query() { return null; }
			
			@Override
			public Iterable<TypeWithoutIdAnnotation> search(AfterWhereBuilder query) { return null; }
			
			@Override
			public String getName() { return null; }

			@Override
			protected void doPut(String key, TypeWithoutIdAnnotation value) {}
		};
	}
	
	@Test
	public void typeWithIdFieldOnSuperType() throws Exception {
		new BaseStore<Data>(Data.class) {
			@Override
			public Data get(String key) { return null; }

			@Override
			public Data remove(String key) { return null; }

			@Override
			public Collection<Data> values() { return null; }

			@Override
			public void clear() {}

			@Override
			public QueryBuilder query() { return null; }
			
			@Override
			public Iterable<Data> search(AfterWhereBuilder query) { return null; }
			
			@Override
			public String getName() { return null; }

			@Override
			protected void doPut(String key, Data value) {}
		};
	}
	
	@Test
	public void typeWithIdMethod() throws Exception {
		new BaseStore<TypeWithIdMethod>(TypeWithIdMethod.class) {
			@Override
			public TypeWithIdMethod get(String key) { return null; }

			@Override
			public TypeWithIdMethod remove(String key) { return null; }

			@Override
			public Collection<TypeWithIdMethod> values() { return null; }

			@Override
			public void clear() {}

			@Override
			public QueryBuilder query() { return null; }

			@Override
			public Iterable<TypeWithIdMethod> search(AfterWhereBuilder query) { return null; }
			
			@Override
			public String getName() { return null; }

			@Override
			protected void doPut(String key, TypeWithIdMethod value) {}
		};
	}
	
	@Test
	public void typeWithInheritedIdInterfaceMethod() throws Exception {
		new BaseStore<TypeWithIdInterfaceSubclass>(TypeWithIdInterfaceSubclass.class) {
			@Override
			public TypeWithIdInterfaceSubclass get(String key) { return null; }

			@Override
			public TypeWithIdInterfaceSubclass remove(String key) { return null; }

			@Override
			public Collection<TypeWithIdInterfaceSubclass> values() { return null; }

			@Override
			public void clear() {}

			@Override
			public QueryBuilder query() { return null; }

			@Override
			public Iterable<TypeWithIdInterfaceSubclass> search(AfterWhereBuilder query) { return null; }

			@Override
			public String getName() { return null; }

			@Override
			protected void doPut(String key, TypeWithIdInterfaceSubclass value) {}
		};
	}
	
}
