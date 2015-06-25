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
package com.b2international.snowowl.core.store.index.tx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.b2international.snowowl.core.ESRule;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.store.index.ElasticsearchIndex;
import com.b2international.snowowl.core.store.index.Mappings;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 5.0
 */
public class DefaultTransactionalIndexTest extends PersonFixtures {
	
	private static final String TEST_INDEX = "test";
	private static final String COMMIT_MESSAGE = "Commit Message XXX";
	
	@Rule
	public ESRule es = new ESRule();

	private TransactionalIndex index;
	
	private AtomicLong timestampProvider = new AtomicLong(0L);
	private AtomicInteger commitIdProvider = new AtomicInteger(0);
	
	@Before
	public void givenTransactionalIndex() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NON_PRIVATE);
		mapper.setVisibility(PropertyAccessor.CREATOR, Visibility.NON_PRIVATE);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		this.index = new DefaultTransactionalIndex(new ElasticsearchIndex(es.client(), TEST_INDEX), mapper);
		final TransactionalIndexAdmin admin = this.index.admin();
		admin.delete();
		admin.create(Mappings.of(mapper, Person.class));
	}

	@Test(expected = NotFoundException.class)
	public void loadingMissingRevision_ShouldThrowNotFoundException() throws Exception {
		this.index.loadRevision(PERSON_TYPE, Branch.MAIN_PATH, PERSON_1_KEY);
	}
	
	@Test
	public void whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex() throws Exception {
		final Person p1 = createPerson1();
		final IndexTransaction tx = openTransaction(Branch.MAIN_PATH);
		tx.add(p1);
		tx.commit(COMMIT_MESSAGE);
		// start a new query to find the indexed person on MAIN branch
		final Map<String, Object> p = (Map<String, Object>) index.loadRevision(PERSON_TYPE, Branch.MAIN_PATH, PERSON_1_KEY);
		assertNotNull(p);
		assertThat(p).isNotEmpty();
	}
	
	@Test
	public void whenCommittingTwoRevisionsOnMAIN_ThenLoadShouldReturnTheModifiedEntity() throws Exception {
		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
		
		final IndexTransaction tx2 = openTransaction(Branch.MAIN_PATH);
		final Person person = createPerson1();
		person.setYob(1997);
		tx2.add(person);
		tx2.commit(COMMIT_MESSAGE);
		
		final Map<String, Object> p3 = index.loadRevision(PERSON_TYPE, Branch.MAIN_PATH, PERSON_1_KEY);
		assertEquals(1997, p3.get("yob"));
	}
	
//	@Test
//	public void whenCommittingFirstRevisionOnMAIN_AndCreatingEmptyBranch_ThenQueryOnBranchShouldReturnTheRevisionFromMAIN() throws Exception {
//		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
//		index.createBranch("MAIN", "a");
//		final IndexTransaction tx2 = index.transaction("MAIN/a");
//		assertNotNull(tx2.get(PERSONS, PERSON_1_ID));
//	}
//	
//	@Test
//	public void whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch() throws Exception {
//		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
//		index.createBranch("MAIN", "a");
//		final IndexTransaction tx = index.transaction("MAIN/a");
//		final Person person = (Person) tx.get(PERSONS, PERSON_1_ID);
//		person.setYob(1997);
//		tx.index(person);
//		tx.commit();
//		// verify that MAIN version is still has yob 2015
//		final Person personMain = (Person) index.load(PERSONS, "MAIN", PERSON_1_ID);
//		assertEquals(2015, personMain.getYob());
//		// check successful commit on branch
//		final Person personBranch = (Person) index.load(PERSONS, "MAIN/a", PERSON_1_ID);
//		assertEquals(1997, personBranch.getYob());
//	}
//	
//	@Test
//	public void whenCommittingOnBranchAndOnMain_ThenQueriesShouldReturnRespectiveVersions() throws Exception {
//		whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch();
//		
//		final IndexTransaction tx = index.transaction(RevisionIndex.MAIN_BRANCH);
//		final Person p1 = (Person) tx.get(PERSONS, PERSON_1_ID);
//		p1.setFirstName("MAIN");
//		tx.index(p1);
//		tx.commit();
//		
//		final Person personMain = (Person) index.load(PERSONS, "MAIN", PERSON_1_ID);
//		assertEquals(2015, personMain.getYob());
//		assertEquals("MAIN", personMain.getFirstName());
//		
//		final Person personBranch = (Person) index.load(PERSONS, "MAIN/a", PERSON_1_ID);
//		assertEquals(1997, personBranch.getYob());
//		assertEquals("Foo", personBranch.getFirstName());
//	}
//	
//	@Test
//	public void whenRebasingBranch_ThenQueryShouldReturnNewVersionOnBranch() throws Exception {
//		whenCommittingFirstRevisionOnMAIN_AndCreatingEmptyBranch_ThenQueryOnBranchShouldReturnTheRevisionFromMAIN();
//		
//		// make a change on MAIN, but leave branch unchanged
//		final IndexTransaction tx = index.transaction(RevisionIndex.MAIN_BRANCH);
//		final Person p1 = (Person) tx.get(PERSONS, PERSON_1_ID);
//		p1.setFirstName("MAIN");
//		tx.index(p1);
//		tx.commit();
//
//		// verify that person has Foo before rebase
//		final Person personBeforeRebase = (Person) index.load(PERSONS, "MAIN/a", PERSON_1_ID);
//		assertEquals("Foo", personBeforeRebase.getFirstName());
//		
//		// rebase branch should move baseTimestamp forward
//		index.rebase("MAIN/a");
//		
//		final Person personAfterRebase = (Person) index.load(PERSONS, "MAIN/a", PERSON_1_ID);
//		assertEquals("MAIN", personAfterRebase.getFirstName());
//	}
//	
//	@Test
//	public void whenGettingChangesOnBranch_ThenItShouldReturnAllChanges() throws Exception {
//		whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch();
//		final IndexTransaction tx = index.transaction("MAIN/a");
//		assertThat(tx.changes()).hasSize(1);
//	}
//	
//	@Test
//	public void whenGettingMultipleChangesOnBranch_ThenItShouldReturnOnlyTheLatestOnePerComponent() throws Exception {
//		whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch();
//		// new commit on component 1
//		final IndexTransaction tx = index.transaction("MAIN/a");
//		Person person = (Person) tx.get(PERSONS, PERSON_1_ID);
//		person.setFirstName("BranchName");
//		tx.index(person);
//		tx.commit();
//		
//		final Collection<Component> changes = tx.changes();
//		assertThat(changes).hasSize(1);
//		final Person personLatestOnBranch = (Person) Iterables.getFirst(changes, null);
//		assertEquals("BranchName", personLatestOnBranch.getFirstName());
//	}
//	
//	@Test
//	public void test_LargeChangeSet_Commit() throws Exception {
//		final int changeSetSize = 1_000_000;
//		final IndexTransaction tx = index.transaction(RevisionIndex.MAIN_BRANCH);
//		Stopwatch watch = Stopwatch.createStarted();
//		for (int i = 0; i < changeSetSize; i++) {
//			tx.index(createRandomPerson(i));
//		}
//		tx.commit();
//		System.out.println("TX Commit of 1_000_000 elements took: " + watch);
//		watch.reset();
//		watch.start();
//		final Collection<Component> changes = tx.changes();
//		System.out.println("ChangeSet computation from index took: " + watch);
//		assertThat(changes).hasSize(changeSetSize);
//	}
	
	private IndexTransaction openTransaction(String branchPath) {
		return index.transaction(commitIdProvider.getAndIncrement(), timestampProvider.getAndIncrement(), branchPath);
	}
	
}
