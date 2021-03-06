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
package com.b2international.snowowl.terminologymetadata.validation;

import com.b2international.snowowl.terminologymetadata.CodeSystemVersion;

import org.eclipse.emf.common.util.EList;

/**
 * A sample validator interface for {@link com.b2international.snowowl.terminologymetadata.CodeSystem}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface CodeSystemValidator {
	boolean validate();

	boolean validateCodeSystemOID(String value);
	boolean validateName(String value);
	boolean validateShortName(String value);
	boolean validateMaintainingOrganizationLink(String value);

	boolean validateMaintainingOrganization(String value);
	boolean validateLanguage(String value);
	boolean validateCitation(String value);
	boolean validateIconPath(String value);

	boolean validateSnowOwlTerminologyComponentId(String value);

	boolean validateSnowOwlTerminologyId(String value);

	boolean validateVersions(EList<CodeSystemVersion> value);
}