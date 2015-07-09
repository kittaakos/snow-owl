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
import static com.b2international.snowowl.core.tests.person.PersonFixtures.PERSON_1_KEY;
import static com.b2international.snowowl.core.tests.person.PersonFixtures.PERSON_RESOURCE;
import static com.b2international.snowowl.core.tests.person.PersonFixtures.REPO_NAME;
import static com.b2international.snowowl.core.tests.person.PersonFixtures.USER;
import static com.b2international.snowowl.core.tests.person.PersonFixtures.createEMFPerson1;
import static com.b2international.snowowl.core.tests.person.PersonFixtures.createPersonRepository;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.h2.H2Adapter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import person.PersonPackage;

import com.b2international.snowowl.core.DefaultObjectMapper;
import com.b2international.snowowl.core.repository.Repository;
import com.b2international.snowowl.core.repository.RepositorySession;
import com.b2international.snowowl.core.repository.cp.IEClassProvider;
import com.b2international.snowowl.core.store.index.DefaultBulkIndex;
import com.b2international.snowowl.core.store.index.DefaultIndex;
import com.b2international.snowowl.core.store.index.Index;
import com.b2international.snowowl.core.store.index.IndexAdmin;
import com.b2international.snowowl.core.store.index.Mappings;
import com.b2international.snowowl.core.store.index.tx.DefaultTransactionalIndex;
import com.b2international.snowowl.core.store.index.tx.TransactionalIndex;
import com.b2international.snowowl.core.store.query.Expressions;
import com.b2international.snowowl.core.tests.ESRule;
import com.b2international.snowowl.core.tests.TemporaryDirectory;
import com.b2international.snowowl.core.tests.person.Person;
import com.b2international.snowowl.core.tests.person.PersonChangeProcessorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;

/**
 * @since 5.0
 */
public class DefaultRepositoryTransactionTest {

	@Rule
	public ESRule es = new ESRule();
	
	private Repository repository;
	private TransactionalIndex txIndex;
	
	@Rule
	public TemporaryDirectory dir = new TemporaryDirectory(getClass().getSimpleName().toLowerCase());
	
	@Before
	public void givenInfrastructure() {
		// manually set up h2 adapter instance
		IDBAdapter.REGISTRY.put("h2", new H2Adapter());
		// json support
		final ObjectMapper mapper = new DefaultObjectMapper();
		// create repository
		final IEClassProvider eClassProvider = mock(IEClassProvider.class);
		when(eClassProvider.getEClass(anyString(), anyLong())).thenReturn(PersonPackage.Literals.PERSON);
		PersonChangeProcessorFactory factory = new PersonChangeProcessorFactory();
		repository = createPersonRepository(dir.getTmpDir().getPath(), REPO_NAME, factory, eClassProvider);
		// activate repository
		repository.activate();
		
		// create transactional index
		final Index index = new DefaultIndex(es.client(), getClass().getSimpleName().toLowerCase(), Mappings.of(mapper, Person.class));
		this.txIndex = new DefaultTransactionalIndex(new DefaultBulkIndex(index), repository.branching());
		final IndexAdmin admin = this.txIndex.admin();
		admin.delete();
		admin.create();
		factory.setIndex(txIndex);
		// user is logged in
		((InternalRepository)repository).addUser(USER, PASS);
	}
	
	@Test
	public void openTransactionAndCommitNewPerson() throws Exception {
		final RepositorySession session = repository.sessions().open(USER, PASS);
		
		final CDOTransaction transaction = session.openTransaction("MAIN");
		final CDOResource resource = transaction.getOrCreateResource(PERSON_RESOURCE);
		// save a Person to the resource
		resource.getContents().add(createEMFPerson1());
		transaction.setCommitComment("Added Foo Bar person");
		assertNotNull(transaction.commit());
		
		final Iterable<Person> persons = txIndex.search(txIndex.query().on("MAIN").selectAll().where(Expressions.exactMatch("id", PERSON_1_KEY)), Person.class);
		final Person person = Iterables.getFirst(persons, null);
		assertNotNull(person);
	}
	
}
