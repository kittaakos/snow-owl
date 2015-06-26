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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.b2international.snowowl.core.ESRule;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.branch.MockBranchManager;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.store.index.DefaultIndex;
import com.b2international.snowowl.core.store.index.Mappings;
import com.b2international.snowowl.core.terminology.Component;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 5.0
 */
public class DefaultTransactionalIndexTest extends PersonFixtures {
	
	private static final String COMMIT_MESSAGE = "Commit Message XXX";
	
	@Rule
	public ESRule es = new ESRule();

	private TransactionalIndex index;
	
	private AtomicLong timestampProvider = new AtomicLong(0L);
	private AtomicInteger commitIdProvider = new AtomicInteger(0);
	private Branch main;

	private BranchManager manager = new MockBranchManager();
	
	@Before
	public void givenTransactionalIndex() {
		// branch support
		main = manager.getMainBranch();
		// json support
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NON_PRIVATE);
		mapper.setVisibility(PropertyAccessor.CREATOR, Visibility.NON_PRIVATE);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// create transactional index
		this.index = new DefaultTransactionalIndex(new DefaultIndex(es.client(), getClass().getSimpleName().toLowerCase(), Mappings.of(mapper, Person.class)), mapper, manager);
		final TransactionalIndexAdmin admin = this.index.admin();
		admin.delete();
		admin.create();
	}

	@Test(expected = NotFoundException.class)
	public void loadingMissingRevision_ShouldThrowNotFoundException() throws Exception {
		this.index.loadRevision(PERSON_TYPE, Branch.MAIN_PATH, PERSON_1_KEY);
	}
	
	@Test
	public void whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex() throws Exception {
		final IndexTransaction tx = openTransaction(main);
		final Person p1 = createPerson1();
		tx.add(p1);
		tx.commit(COMMIT_MESSAGE);
		
		final Map<String, Object> p = (Map<String, Object>) index.loadRevision(PERSON_TYPE, Branch.MAIN_PATH, PERSON_1_KEY);
		assertNotNull(p);
		assertThat(p).isNotEmpty();
	}
	
	@Test
	public void whenCommittingTwoRevisionsOnMAIN_ThenLoadShouldReturnTheModifiedEntity() throws Exception {
		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
		
		final IndexTransaction tx2 = openTransaction(main);
		final Person person = createPerson1();
		person.setYob(1997);
		tx2.add(person);
		tx2.commit(COMMIT_MESSAGE);
		
		final Map<String, Object> personRev = index.loadRevision(PERSON_TYPE, Branch.MAIN_PATH, PERSON_1_KEY);
		assertThat(personRev)
			.containsEntry("id", PERSON_1_KEY)
			.containsEntry("yob", 1997)
			.containsEntry("firstName", "Foo")
			.containsEntry("lastName", "Bar");
	}
	
	@Test
	public void whenCommittingFirstRevisionOnMAIN_AndCreatingEmptyBranch_ThenQueryOnBranchShouldReturnTheRevisionFromMAIN() throws Exception {
		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
		final Branch branchA = createBranch(Branch.MAIN_PATH, "a");
		final Map<String, Object> personRev = index.loadRevision(PERSON_TYPE, branchA.path(), PERSON_1_KEY);
		assertThat(personRev)
			.containsEntry("id", PERSON_1_KEY)
			.containsEntry("yob", 2015)
			.containsEntry("firstName", "Foo")
			.containsEntry("lastName", "Bar");
	}

	@Test
	public void whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch() throws Exception {
		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
		
		// open branch
		final Branch branchA = createBranch(Branch.MAIN_PATH, "a");
		
		// make commit on branch
		final IndexTransaction tx = openTransaction(branchA);
		final Person person = createPerson1();
		person.setYob(1997);
		tx.add(person);
		tx.commit(COMMIT_MESSAGE);
		// verify that MAIN version still has yob 2015
		final Map<String, Object> personMain = index.loadRevision(PERSON_TYPE, Branch.MAIN_PATH, PERSON_1_KEY);
		assertThat(personMain)
			.containsEntry("id", PERSON_1_KEY)
			.containsEntry("yob", 2015)
			.containsEntry("firstName", "Foo")
			.containsEntry("lastName", "Bar");
		
		// verify that Branch A has new version with yob 1997
		final Map<String, Object> personBranch = index.loadRevision(PERSON_TYPE, branchA.path(), PERSON_1_KEY);
		assertThat(personBranch)
			.containsEntry("id", PERSON_1_KEY)
			.containsEntry("yob", 1997)
			.containsEntry("firstName", "Foo")
			.containsEntry("lastName", "Bar");
	}
	
	@Test
	public void whenCommittingOnBranchAndOnMain_ThenQueriesShouldReturnRespectiveVersions() throws Exception {
		// execute commit on Branch A
		whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch();
		
		// execute commit on MAIN
		final IndexTransaction tx = openTransaction(main);
		final Person p1 = createPerson1();
		p1.setFirstName("MAIN");
		tx.add(p1);
		tx.commit(COMMIT_MESSAGE);
		
		// verify that MAIN version still has yob 2015, but firstName 'MAIN'
		final Map<String, Object> personMain = index.loadRevision(PERSON_TYPE, Branch.MAIN_PATH, PERSON_1_KEY);
		assertThat(personMain)
			.containsEntry("id", PERSON_1_KEY)
			.containsEntry("yob", 2015)
			.containsEntry("firstName", "MAIN")
			.containsEntry("lastName", "Bar");
		
		// verify that Branch A has new version with yob 1997
		final Map<String, Object> personBranch = index.loadRevision(PERSON_TYPE, manager.getBranch("MAIN/a").path(), PERSON_1_KEY);
		assertThat(personBranch)
			.containsEntry("id", PERSON_1_KEY)
			.containsEntry("yob", 1997)
			.containsEntry("firstName", "Foo")
			.containsEntry("lastName", "Bar");
	}
	
	@Test
	public void whenRebasingBranch_ThenQueryShouldReturnNewVersionOnBranch() throws Exception {
		whenCommittingFirstRevisionOnMAIN_AndCreatingEmptyBranch_ThenQueryOnBranchShouldReturnTheRevisionFromMAIN();
		
		// make a change on MAIN, but leave branch unchanged
		final IndexTransaction tx = openTransaction(main);
		final Person p1 = createPerson1();
		p1.setFirstName("MAIN");
		tx.add(p1);
		tx.commit(COMMIT_MESSAGE);

		// verify that person has Foo before rebase
		final Map<String, Object> personBeforeRebase = index.loadRevision(PERSON_TYPE, "MAIN/a", PERSON_1_KEY);
		assertThat(personBeforeRebase).containsEntry("firstName", "Foo");
		
		// rebase branch should move baseTimestamp forward
		manager.getBranch("MAIN/a").rebase("Rebased branch A");
		// verify that person has MAIN after rebase
		final Map<String, Object> personAfterRebase = index.loadRevision(PERSON_TYPE, "MAIN/a", PERSON_1_KEY);
		assertThat(personAfterRebase).containsEntry("firstName", "MAIN");
	}
	
//	@Test
//	public void whenGettingChangesOnBranch_ThenItShouldReturnAllChanges() throws Exception {
//		whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch();
//		final IndexTransaction tx = index.transaction("MAIN/a");
//		assertThat(tx.changes()).hasSize(1);
//	}

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
	
	private IndexTransaction openTransaction(final Branch branch) {
		final int commitId = commitIdProvider.incrementAndGet();
		final long commitTimestamp = timestampProvider.incrementAndGet();
		final IndexTransaction original = index.transaction(commitId, commitTimestamp, branch.path());
		return new IndexTransaction() {
			@Override
			public void delete(String type, String id) {
				original.delete(type, id);
			}
			
			@Override
			public void commit(String commitMessage) {
				original.commit(commitMessage);
				// make commit available in the branch as timestamp
				when(branch.headTimestamp()).thenReturn(commitTimestamp);
			}
			
			@Override
			public void add(Component object) {
				original.add(object);
			}
		};
	}
	
	private Branch createBranch(String parent, String name) {
		return manager.getBranch(parent).createChild(name);
	}
	
}
