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

import static com.b2international.snowowl.core.tests.person.PersonFixtures.PASS;
import static com.b2international.snowowl.core.tests.person.PersonFixtures.REPO_NAME;
import static com.b2international.snowowl.core.tests.person.PersonFixtures.REPO_NAME_2;
import static com.b2international.snowowl.core.tests.person.PersonFixtures.USER;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Collection;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.h2.H2Adapter;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import person.PersonPackage;

import com.b2international.snowowl.core.exceptions.SnowOwlException;
import com.b2international.snowowl.core.repository.Repository;
import com.b2international.snowowl.core.repository.RepositorySession;
import com.b2international.snowowl.core.terminology.Component;
import com.b2international.snowowl.core.tests.TemporaryDirectory;
import com.b2international.snowowl.core.tests.person.Person;
import com.b2international.snowowl.core.tests.person.PersonFixtures;



/**
 * TODO test automatic session close (1 sec wait then check that the session has been closed)
 * 
 * @since 5.0
 */
public class DefaultRepositoryTest {

	private Collection<Class<? extends Component>> components = newHashSet();
	private Collection<EPackage> ePackages = newHashSet();
	
	private Repository repository;
	
	@Rule
	public TemporaryDirectory dir = new TemporaryDirectory(getClass().getSimpleName().toLowerCase());
	
	@Before
	public void givenRepositoryConfiguration() throws Exception {
		
		// manually set up h2 adapter instance
		IDBAdapter.REGISTRY.put("h2", new H2Adapter());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void createRepositoryWithoutComponents() throws Exception {
		new DefaultRepository(REPO_NAME, components, ePackages, null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void createRepositoryWithoutEPackages() throws Exception {
		components.add(Person.class);
		new DefaultRepository(REPO_NAME, components, ePackages, null);
	}
	
	@Test(expected = NullPointerException.class)
	public void createRepositoryWithoutConfig() throws Exception {
		components.add(Person.class);
		ePackages.add(PersonPackage.eINSTANCE);
		new DefaultRepository(REPO_NAME, components, ePackages, null);
	}
	
	@Test
	public void createValidRepository() throws Exception {
		repository = createPersonRepository();
		assertThat(repository.name()).isEqualTo(REPO_NAME);
	}

	@Test
	public void createAndActivateRepository_ShouldCreateAllRequiredStores() throws Exception {
		createPersonRepository().activate();
		assertThat(new File(dir.getTmpDir(), REPO_NAME+".h2.db")).exists();
	}
	
	@Test
	public void createAndActivateRepository_ShouldConvertRepositoryNameToFilePathWithoutSpaces() throws Exception {
		PersonFixtures.createPersonRepository(dir.getTmpDir().getPath(), REPO_NAME_2).activate();
		assertThat(new File(dir.getTmpDir(), REPO_NAME_2.replaceAll(" ", "_").toLowerCase()+".h2.db")).exists();
	}
	
	@Test(expected = IllegalStateException.class)
	public void getSessionsOnInactiveRepository_ShouldThrowSnowOwlException() throws Exception {
		final Repository repository = createPersonRepository();
		repository.sessions();
	}
	
	@Test
	public void createAndActivateRepository_ShouldCreateBasicRepositoryServices() throws Exception {
		final Repository repository = createPersonRepository();
		repository.activate();
		assertNotNull(repository.branching());
		assertNotNull(repository.sessions());
	}
	
	@Test(expected = SnowOwlException.class)
	public void openSessionForUnknownUser_ShouldThrowException() throws Exception {
		final Repository repository = createPersonRepository();
		repository.activate();
		repository.sessions().open(USER, PASS);
	}
	
	@Test
	public void openSessionWithKnownUser_ShouldBeSuccessful() throws Exception {
		final Repository repository = createPersonRepository();
		repository.activate();
		// mocked app login
		((InternalRepository)repository).addUser(USER, PASS);
		final RepositorySession session = repository.sessions().open(USER, PASS);
		assertThat(repository.sessions().getSessions()).contains(session);
	}
	
	@After
	public void after() {
		if (LifecycleUtil.isActive(repository)) {
			LifecycleUtil.deactivate(repository);
		}
	}
	
	private Repository createPersonRepository() {
		return PersonFixtures.createPersonRepository(dir.getTmpDir().getPath());
	}
	
}
