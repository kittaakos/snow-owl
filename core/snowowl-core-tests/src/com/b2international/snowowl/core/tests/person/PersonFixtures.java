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
package com.b2international.snowowl.core.tests.person;

import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.Map;

import person.PersonPackage;

import com.b2international.snowowl.core.internal.repository.DefaultRepositoryBuilder;
import com.b2international.snowowl.core.repository.Repository;
import com.b2international.snowowl.core.repository.config.RepositoryConfiguration;
import com.b2international.snowowl.core.repository.cp.ChangeProcessorFactory;
import com.b2international.snowowl.core.repository.cp.IEClassProvider;

/**
 * @since 5.0
 */
public class PersonFixtures {

	public static final String LOC = "target/store";
	
	public static final String REPO_NAME = "person";
	public static final String REPO_NAME_2 = "Person Store";
	
	public static final String PERSON_TYPE = "person";
	public static final long PERSON_1_STORAGEKEY = 1L;
	public static final String PERSON_1_KEY = "1";
	
	public static Person createRandomPerson(int id) {
		final Person person = createPerson(String.valueOf(id), "FN" + id, "LN" + id, 2015-(id % 90));
		final Address address = createAddress("Country" + id, "City"+id % 10000, id % 10000, id+"Street");
		person.addAddress(address);
		return person;
	}

	public static Address createAddress(String country, String city, int zipCode, String street) {
		final Address address = new Address();
		address.setCountry(country);
		address.setCity(city);
		address.setZipCode(zipCode);
		address.setStreet(street);
		return address;
	}

	public static Person createPerson1() {
		return createPerson(PERSON_1_KEY, "Foo", "Bar", 2015);
	}
	
	public static Person createPerson(String id, String firstName, String lastName, int yob) {
		final Person person = new Person();
		person.setId(id);
		person.setFirstName(firstName);
		person.setLastName(lastName);
		person.setYob(yob);
		return person;
	}
	
	public static Person createPerson(String id, String firstName, String lastName, int yob, Collection<Map<String, Object>> addresses) {
		final Person person = createPerson(id, firstName, lastName, yob);
		for (Map<String, Object> address : addresses) {
			person.getAddresses().add(createAddress((String) address.get("country"), (String) address.get("city"), (Integer) address.get("zipCode"), (String) address.get("street")));
		}
		return person;
	}
	
	public static Repository createPersonRepository() {
		return createPersonRepository(REPO_NAME);
	}

	public static Repository createPersonRepository(final String name) {
		return createPersonRepository(name, mock(ChangeProcessorFactory.class), mock(IEClassProvider.class));
	}
	
	public static Repository createPersonRepository(final String name, ChangeProcessorFactory factory, IEClassProvider eClassProvider) {
		final RepositoryConfiguration config = new RepositoryConfiguration();
		// set location explicitly for tests
		config.getDatabaseConfiguration().setLocation(LOC);
		
		return new DefaultRepositoryBuilder(name, config)
			.addComponent(Person.class)
			.addEPackage(PersonPackage.eINSTANCE)
			.addChangeProcessor(factory)
			.addEClassProvider(eClassProvider)
			.build();
	}
	
}
