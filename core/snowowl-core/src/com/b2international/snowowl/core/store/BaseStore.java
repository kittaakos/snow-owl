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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import com.b2international.commons.exceptions.FormattedRuntimeException;

/**
 * @since 5.0
 * @param <T>
 *            - the type of the objects to store in any kind of storage facility
 */
public abstract class BaseStore<T> implements Store<T> {

	private Class<T> type;
	private Field idField;
	private Method idGetter;

	public BaseStore(Class<T> type) {
		this.type = checkNotNull(type, "Type may not be null");
		this.idField = getIdField(type);
		if (this.idField != null) {
			this.idField.setAccessible(true);
		} else {
			this.idGetter = getIdGetter(type);
			checkArgument(this.idGetter != null, "Type '%s' should mark one field or public method with the Id annotation", type);
		}
	}

	/**
	 * Returns the type of the 
	 * @return
	 */
	protected Class<T> getTypeClass() {
		return type;
	}

	@Override
	public final void put(T value) {
		final String id = extractId(value);
		doPut(id, value);
	}

	@Override
	public final boolean replace(T oldValue, T newValue) {
		final String oldValueId = extractId(oldValue);
		final String newValueId = extractId(newValue);
		if (!Objects.equals(oldValueId, newValueId) || oldValue.equals(newValue) || !oldValue.equals(get(oldValueId))) {
			return false;
		} else {
			doPut(oldValueId, newValue);
			return true;
		}
	}

	/**
	 * 
	 * @param key
	 *            - the identifier of the given value object
	 * @param value
	 *            - the value to store in this storage
	 */
	protected abstract void doPut(String key, T value);

	/*Extracts the ID value from the given T value*/
	private String extractId(T value) {
		try {
			return String.valueOf(idField.get(value));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new FormattedRuntimeException("Cannot extract identifier from: %s", value);
		}
	}
	
	/*Extract the field marked with Id annotation, the field should be a String field*/
	private static Field getIdField(Class<?> type) {
		for (Field field : type.getDeclaredFields()) {
			if (field.isAnnotationPresent(Id.class)) {
				checkArgument(field.getType() == String.class, "The field '%s' marked with Id annotation should have the type String", field.getName());
				return field;
			}
		}
		if (type.getSuperclass() != null) {
			return getIdField(type.getSuperclass());
		}
		return null;
	}
	
	private static Method getIdGetter(Class<?> type) {
		for (Method method : type.getMethods()) {
			if (method.isAnnotationPresent(Id.class)) {
				return method;
			}
		}
		if (type.getSuperclass() != null) {
			final Method idGetter = getIdGetter(type.getSuperclass());
			if (idGetter != null) {
				return idGetter;
			}
		}
		for (Class<?> iface : type.getInterfaces()) {
			final Method idGetter = getIdGetter(iface);
			if (idGetter != null) {
				return idGetter;
			}
		}
		return null;
	}

}
