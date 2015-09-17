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
package com.b2international.snowowl.snomed.datastore.index.update;

import static com.b2international.snowowl.snomed.datastore.browser.SnomedIndexBrowserConstants.CONCEPT_DEGREE_OF_INTEREST;

import com.b2international.snowowl.datastore.index.DocumentUpdaterBase;
import com.b2international.snowowl.datastore.index.mapping.Mappings;
import com.b2international.snowowl.snomed.datastore.index.mapping.SnomedDocumentBuilder;

/**
 * @since 4.3
 */
public class ConceptDoiUpdater extends DocumentUpdaterBase<SnomedDocumentBuilder> {

	public static final float DEFAULT_DOI = 1.0f;
	private float doi;

	public ConceptDoiUpdater(String componentId) {
		this(componentId, DEFAULT_DOI);
	}

	public ConceptDoiUpdater(String componentId, float doi) {
		super(componentId);
		this.doi = doi;
	}

	@Override
	public void update(SnomedDocumentBuilder doc) {
		doc.removeAll(Mappings.floatDocValuesField(CONCEPT_DEGREE_OF_INTEREST));
		doc.docValuesField(CONCEPT_DEGREE_OF_INTEREST, doi);
	}

}
