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
package com.b2international.snowowl.snomed.core.index;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.SimpleType;

/**
 * TypeIdResolver implementation for {@link Membership} classes.
 * 
 * @since 5.0
 */
public class MembershipTypeIdResolver extends TypeIdResolverBase {

	public static final String ASSOCIATION = "association";
	public static final String ATTRIBUTE_VALUE = "attribute-value";
	public static final String COMPLEX_MAP = "complex-map";
	public static final String DESCRIPTION_TYPE = "description-type";
	public static final String EXTENDED_MAP = "extended-map";
	public static final String LANGUAGE = "language";
	public static final String MODULE_DEPENDENCY = "module-dependency";
	public static final String QUERY = "query";
	public static final String SIMPLE_MAP = "simple-map";
	public static final String SIMPLE = "simple";

	@Override
	public Id getMechanism() {
		return Id.CUSTOM;
	}

	@Override
	public String idFromValue(Object arg0) {
		Membership membership = (Membership) arg0;
		return mapToId(membership.getType());
	}

	@Override
	public String idFromValueAndType(Object arg0, Class<?> arg1) {
		// TODO: what to do here?
		return arg1.toString();
	}

	@Override
	public JavaType typeFromId(String arg0) {
		return mapToType(arg0);
	}

	private String mapToId(ReferenceSetType type) {
		switch (type) {
		case ASSOCIATION:
			return ASSOCIATION;
		case ATTRIBUTE_VALUE:
			return ATTRIBUTE_VALUE;
		case COMPLEX_MAP:
			return COMPLEX_MAP;
		case DESCRIPTION_TYPE:
			return DESCRIPTION_TYPE;
		case EXTENDED_MAP:
			return EXTENDED_MAP;
		case LANGUAGE:
			return LANGUAGE;
		case MODULE_DEPENDENCY:
			return MODULE_DEPENDENCY;
		case QUERY:
			return QUERY;
		case SIMPLE:
			return SIMPLE;
		case SIMPLE_MAP:
			return SIMPLE_MAP;
		default:
			throw new IllegalArgumentException("Unexpected type: " + type);
		}
	}

	private JavaType mapToType(String id) {
		switch (id) {
		case ASSOCIATION:
			return SimpleType.construct(AssociationTypeMembership.class);
		case ATTRIBUTE_VALUE:
			return SimpleType.construct(AttributeValueTypeMembership.class);
		case COMPLEX_MAP:
			return SimpleType.construct(ComplexMapTypeMembership.class);
		case DESCRIPTION_TYPE:
			return SimpleType.construct(DescriptionTypeMembership.class);
		case EXTENDED_MAP:
			return SimpleType.construct(ExtendedMapTypeMembership.class);
		case LANGUAGE:
			return SimpleType.construct(LanguageTypeMembership.class);
		case MODULE_DEPENDENCY:
			return SimpleType.construct(ModuleDependencyTypeMembership.class);
		case QUERY:
			return SimpleType.construct(QueryTypeMembership.class);
		case SIMPLE:
			return SimpleType.construct(SimpleTypeMembership.class);
		case SIMPLE_MAP:
			return SimpleType.construct(SimpleMapTypeMembership.class);
		default:
			throw new IllegalArgumentException("Unexpected type ID: " + id);
		}
	}
}
