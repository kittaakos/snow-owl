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
package com.b2international.snowowl.core.repository;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.net4j.util.lifecycle.ILifecycle;

import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.conflict.ICDOConflictProcessor;
import com.b2international.snowowl.core.repository.cp.ChangeProcessorFactory;
import com.b2international.snowowl.core.repository.cp.IEClassProvider;
import com.b2international.snowowl.core.terminology.Component;

/**
 * @since 5.0
 */
public interface Repository extends ILifecycle {

	// TODO is this the proper place for this method???
	ICDOConflictProcessor getConflictProcessor();

	/**
	 * Returns the branch manager of this repository.
	 * 
	 * @return
	 */
	BranchManager branching();

	/**
	 * Returns the repository session management.
	 * 
	 * @return
	 */
	RepositorySessions sessions();

	/**
	 * Returns a human-readable repository name.
	 * 
	 * @return
	 */
	String name();

	/**
	 * Returns the identifier of the repository.
	 * 
	 * @return
	 */
	String id();

	/**
	 * @since 5.0
	 */
	interface Builder {

		Builder addComponent(Class<? extends Component> component);

		Builder addEPackage(EPackage ePackage);
		
		Builder addChangeProcessor(ChangeProcessorFactory factory);
		
		Builder addEClassProvider(IEClassProvider eClassProvider);
		
		Repository build();

	}

}
