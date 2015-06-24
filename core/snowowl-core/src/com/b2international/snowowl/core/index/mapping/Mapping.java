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
package com.b2international.snowowl.core.index.mapping;

import org.elasticsearch.bootstrap.Elasticsearch;

/**
 * Annotation to define the mapping of a particular type.
 * 
 * @since 5.0
 */
public @interface Mapping {

	/**
	 * The name of this type which will be used to identify elements of this type in the index.
	 * 
	 * @return
	 */
	String type();

	/**
	 * A file name to look for on the classpath of the declaring class. It is advised to keep the file in the same package as the declaring class.
	 * 
	 * @see Elasticsearch mapping definition
	 * @return
	 */
	String mapping();

	/**
	 * The id field to use when indexing the instances of this type.
	 * 
	 * @return
	 */
	String id() default "id";

}
