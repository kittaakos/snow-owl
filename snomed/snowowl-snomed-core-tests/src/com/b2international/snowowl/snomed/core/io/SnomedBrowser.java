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
package com.b2international.snowowl.snomed.core.io;

import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.store.index.tx.TransactionalIndex;
import com.b2international.snowowl.core.store.query.Expressions;
import com.b2international.snowowl.snomed.core.store.index.Concept;
import com.b2international.snowowl.snomed.core.store.index.SnomedComponent;
import com.b2international.snowowl.snomed.core.store.query.builder.ConceptExpressions;
import com.google.common.collect.Iterables;

/**
 * @since 5.0
 */
public class SnomedBrowser {

	private TransactionalIndex index;

	public SnomedBrowser(TransactionalIndex index) {
		this.index = index;
	}
	
	public Concept getConcept(String branch, String conceptId) {
		return getComponent(branch, "id", conceptId, Concept.class);
	}
	
	private <T extends SnomedComponent> T getComponent(String branch, String field, String id, Class<T> type) {
		final T concept = Iterables.getOnlyElement(index.search(index.query().on(branch).selectAll().where(Expressions.exactMatch("id", id)).limit(1), type), null);
		if (concept == null) {
			throw new NotFoundException(type.getName(), id);
		}
		return concept;
	}

	public Iterable<Concept> getChildren(String branchPath, String conceptId, int from, int size) {
		return index.search(index.query().on(branchPath).selectAll().where(ConceptExpressions.concept().parent(conceptId).build()).offset(from).limit(size), Concept.class);
	}
	
	public Iterable<Concept> getDescendants(String branchPath, String conceptId, int from, int size) {
		return index.search(index.query().on(branchPath).selectAll().where(ConceptExpressions.concept().ancestor(conceptId).build()).offset(from).limit(size), Concept.class);
	}
	
}
