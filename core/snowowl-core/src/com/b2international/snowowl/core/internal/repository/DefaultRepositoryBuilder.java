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
package com.b2international.snowowl.core.internal.repository;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;

import org.eclipse.emf.ecore.EPackage;

import com.b2international.snowowl.core.repository.Repository;
import com.b2international.snowowl.core.repository.config.RepositoryConfiguration;
import com.b2international.snowowl.core.terminology.Component;

/**
 * @since 5.0
 */
public final class DefaultRepositoryBuilder implements Repository.Builder {

	private String name;
	private Collection<Class<? extends Component>> components = newHashSet();
	private Collection<EPackage> ePackages = newHashSet();
	private RepositoryConfiguration configuration;

	public DefaultRepositoryBuilder(String name) {
		this.name = name;
		this.configuration = new RepositoryConfiguration(); // TODO get this via constructor
	}
	
	@Override
	public DefaultRepositoryBuilder addComponent(Class<? extends Component> component) {
		this.components.add(component);
		return this;
	}
	
	@Override
	public DefaultRepositoryBuilder addEPackage(EPackage ePackage) {
		this.ePackages.add(ePackage);
		return this;
	}
	
	@Override
	public Repository build() {
		return new DefaultRepository(name, components, ePackages, configuration);
	}
	
}
