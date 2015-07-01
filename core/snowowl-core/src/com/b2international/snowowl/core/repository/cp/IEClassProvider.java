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
package com.b2international.snowowl.core.repository.cp;

import org.eclipse.emf.ecore.EClass;

/**
 * Interface for providing {@link EClass} based on branchPath and storageKey values.
 */
public interface IEClassProvider {

	/**
	 * Returns with the {@link EClass} of an object identified by a unique storage key.
	 * 
	 * @param branchPath
	 *            - the branch path
	 * @param storageKey
	 *            - the unique storage key
	 * @return the {@link EClass}
	 */
	EClass getEClass(String branchPath, final long storageKey);

}