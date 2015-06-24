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
package com.b2international.snowowl.core.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.b2international.commons.exceptions.FormattedRuntimeException;
import com.b2international.snowowl.core.index.mapping.DefaultMappingStrategy;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;


/**
 * @since 5.0
 */
public class TransactionalIndexTest {

	private static final String PERSON_1_ID = "1";
	private static final String TEST_INDEX = "test";
	private Index index;
	private Client client;
	private Node node;
	private ObjectMapper mapper = new ObjectMapper();
	private static String PERSONS = Person.class.getSimpleName().toLowerCase();

	@Before
	public void givenIndex() throws Exception {
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		node = NodeBuilder.nodeBuilder().local(true).build();
		node.start();
		// define mapping
		final Mappings mappings = new Mappings();
		mappings.addMapping(TEST_INDEX, PERSONS, Resources.toString(Resources.getResource(TransactionalIndexTest.class, "person_mapping.json"), Charsets.UTF_8));
		mappings.addMappingStrategy(TEST_INDEX, PERSONS, new DefaultMappingStrategy<>(mapper, Person.class));
		client = node.client();
		// init index
		index = new Index(client, TEST_INDEX, mappings, mapper);
		index.delete();
		index.create();
	}
	
	@Test(expected = FormattedRuntimeException.class)//(expected = NotFoundException.class)
	public void whenQueryingForComponentInEmptyIndex_ThrowNotFoundException() throws Exception {
		index.load(PERSONS, Index.MAIN_BRANCH, PERSON_1_ID);
	}
	
	@Test
	public void whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex() throws Exception {
		final Person p1 = createPerson1();
		final IndexCommit tx1 = index.openTransaction(Index.MAIN_BRANCH);
		tx1.index(p1);
		tx1.commit();
		// start a new query to find the indexed person on MAIN branch
		final Person p = (Person) index.load(PERSONS, Index.MAIN_BRANCH, PERSON_1_ID);
		assertNotNull(p);
	}

	@Test
	public void whenCommittingTwoRevisionsOnMAIN_ThenLoadShouldReturnTheModifiedEntity() throws Exception {
		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
		
		final IndexCommit tx2 = index.openTransaction(Index.MAIN_BRANCH);
		final Person p2 = (Person) tx2.get(PERSONS, PERSON_1_ID);
		p2.setYob(1997);
		tx2.index(p2);
		tx2.commit();
		
		final Person p3 = (Person) index.load(PERSONS, Index.MAIN_BRANCH, PERSON_1_ID);
		assertEquals(1997, p3.getYob());
	}
	
	@Test
	public void whenCommittingFirstRevisionOnMAIN_AndCreatingEmptyBranch_ThenQueryOnBranchShouldReturnTheRevisionFromMAIN() throws Exception {
		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
		index.createBranch("MAIN", "a");
		final IndexCommit tx2 = index.openTransaction("MAIN/a");
		assertNotNull(tx2.get(PERSONS, PERSON_1_ID));
	}
	
	@Test
	public void whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch() throws Exception {
		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
		index.createBranch("MAIN", "a");
		final IndexCommit tx = index.openTransaction("MAIN/a");
		final Person person = (Person) tx.get(PERSONS, PERSON_1_ID);
		person.setYob(1997);
		tx.index(person);
		tx.commit();
		// verify that MAIN version is still has yob 2015
		final Person personMain = (Person) index.load(PERSONS, "MAIN", PERSON_1_ID);
		assertEquals(2015, personMain.getYob());
		// check successful commit on branch
		final Person personBranch = (Person) index.load(PERSONS, "MAIN/a", PERSON_1_ID);
		assertEquals(1997, personBranch.getYob());
	}
	
	@Test
	public void whenCommittingOnBranchAndOnMain_ThenQueriesShouldReturnRespectiveVersions() throws Exception {
		whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch();
		
		final IndexCommit tx = index.openTransaction(Index.MAIN_BRANCH);
		final Person p1 = (Person) tx.get(PERSONS, PERSON_1_ID);
		p1.setFirstName("MAIN");
		tx.index(p1);
		tx.commit();
		
		final Person personMain = (Person) index.load(PERSONS, "MAIN", PERSON_1_ID);
		assertEquals(2015, personMain.getYob());
		assertEquals("MAIN", personMain.getFirstName());
		
		final Person personBranch = (Person) index.load(PERSONS, "MAIN/a", PERSON_1_ID);
		assertEquals(1997, personBranch.getYob());
		assertEquals("Foo", personBranch.getFirstName());
	}
	
	@Test
	public void whenRebasingBranch_ThenQueryShouldReturnNewVersionOnBranch() throws Exception {
		whenCommittingFirstRevisionOnMAIN_AndCreatingEmptyBranch_ThenQueryOnBranchShouldReturnTheRevisionFromMAIN();
		
		// make a change on MAIN, but leave branch unchanged
		final IndexCommit tx = index.openTransaction(Index.MAIN_BRANCH);
		final Person p1 = (Person) tx.get(PERSONS, PERSON_1_ID);
		p1.setFirstName("MAIN");
		tx.index(p1);
		tx.commit();

		// verify that person has Foo before rebase
		final Person personBeforeRebase = (Person) index.load(PERSONS, "MAIN/a", PERSON_1_ID);
		assertEquals("Foo", personBeforeRebase.getFirstName());
		
		// rebase branch should move baseTimestamp forward
		index.rebase("MAIN/a");
		
		final Person personAfterRebase = (Person) index.load(PERSONS, "MAIN/a", PERSON_1_ID);
		assertEquals("MAIN", personAfterRebase.getFirstName());
	}
	
	@Test
	public void whenGettingChangesOnBranch_ThenItShouldReturnAllChanges() throws Exception {
		whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch();
		final IndexCommit tx = index.openTransaction("MAIN/a");
		assertThat(tx.changes()).hasSize(1);
	}
	
	@Test
	public void whenGettingMultipleChangesOnBranch_ThenItShouldReturnOnlyTheLatestOnePerComponent() throws Exception {
		whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch();
		// new commit on component 1
		final IndexCommit tx = index.openTransaction("MAIN/a");
		Person person = (Person) tx.get(PERSONS, PERSON_1_ID);
		person.setFirstName("BranchName");
		tx.index(person);
		tx.commit();
		
		final Collection<Component> changes = tx.changes();
		assertThat(changes).hasSize(1);
		final Person personLatestOnBranch = (Person) Iterables.getFirst(changes, null);
		assertEquals("BranchName", personLatestOnBranch.getFirstName());
	}
	
	@Test
	public void test_LargeChangeSet_Commit() throws Exception {
		final int changeSetSize = 1_000_000;
		final IndexCommit tx = index.openTransaction(Index.MAIN_BRANCH);
		Stopwatch watch = Stopwatch.createStarted();
		for (int i = 0; i < changeSetSize; i++) {
			tx.index(createRandomPerson(i));
		}
		tx.commit();
		System.out.println("TX Commit of 1_000_000 elements took: " + watch);
		watch.reset();
		watch.start();
		final Collection<Component> changes = tx.changes();
		System.out.println("ChangeSet computation from index took: " + watch);
		assertThat(changes).hasSize(changeSetSize);
	}
	
	private static Person createRandomPerson(int id) {
		final Person person = createPerson(String.valueOf(id), "FN" + id, "LN" + id, 2015-(id % 90));
		final Address address = createAddress("Country" + id, "City"+id % 10000, id % 10000, id+"Street");
		person.addAddress(address);
		return person;
	}

	private static Address createAddress(String country, String city, int zipCode, String street) {
		final Address address = new Address();
		address.setCountry(country);
		address.setCity(city);
		address.setZipCode(zipCode);
		address.setStreet(street);
		return address;
	}

	@After
	public void after() {
		node.close();
	}
	
	private static Person createPerson1() {
		return createPerson(PERSON_1_ID, "Foo", "Bar", 2015);
	}
	
	private static Person createPerson(String id, String firstName, String lastName, int yob) {
		final Person person = new Person();
		person.setId(id);
		person.setFirstName(firstName);
		person.setLastName(lastName);
		person.setYob(yob);
		return person;
	}
	
	private static Person createPerson(String id, String firstName, String lastName, int yob, Collection<Map<String, Object>> addresses) {
		final Person person = createPerson(id, firstName, lastName, yob);
		for (Map<String, Object> address : addresses) {
			person.getAddresses().add(createAddress((String) address.get("country"), (String) address.get("city"), (Integer) address.get("zipCode"), (String) address.get("street")));
		}
		return person;
	}
	
}
