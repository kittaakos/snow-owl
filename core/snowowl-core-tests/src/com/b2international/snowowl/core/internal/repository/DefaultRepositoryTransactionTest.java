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

import com.b2international.snowowl.core.repository.Repository;
import com.b2international.snowowl.core.repository.RepositorySession;
import com.b2international.snowowl.core.repository.cp.IEClassProvider;
import com.b2international.snowowl.core.store.index.DefaultBulkIndex;
import com.b2international.snowowl.core.store.index.Mappings;
import com.b2international.snowowl.core.store.index.tx.DefaultTransactionalIndex;
import com.b2international.snowowl.core.store.index.tx.TransactionalIndex;
import com.b2international.snowowl.core.store.index.tx.TransactionalIndexAdmin;
import com.b2international.snowowl.core.tests.ESRule;
import com.b2international.snowowl.core.tests.person.Person;
import com.b2international.snowowl.core.tests.person.PersonChangeProcessorFactory;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 5.0
 */
public class DefaultRepositoryTransactionTest {

	@Rule
	public ESRule es = new ESRule();
	
	private Repository repository;
	private TransactionalIndex index;
	
	@Before
	public void givenInfrastructure() {
		// manually set up h2 adapter instance
		IDBAdapter.REGISTRY.put("h2", new H2Adapter());
		// json support
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NON_PRIVATE);
		mapper.setVisibility(PropertyAccessor.CREATOR, Visibility.NON_PRIVATE);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// create repository
		final IEClassProvider eClassProvider = mock(IEClassProvider.class);
		when(eClassProvider.getEClass(anyString(), anyLong())).thenReturn(PersonPackage.Literals.PERSON);
		PersonChangeProcessorFactory factory = new PersonChangeProcessorFactory();
		repository = createPersonRepository(REPO_NAME, factory, eClassProvider);
		repository.activate();
		// user is logged in
		((InternalRepository)repository).addUser(USER, PASS);
		
		// create transactional index
		this.index = new DefaultTransactionalIndex(new DefaultBulkIndex(es.client(), getClass().getSimpleName().toLowerCase(), Mappings.of(mapper, Person.class)), mapper, repository.branching());
		final TransactionalIndexAdmin admin = this.index.admin();
		admin.delete();
		admin.create();
		factory.setIndex(index);
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
		
//		index.loadRevision(PERSON_TYPE, "MAIN", PERSON);
	}
	
}
