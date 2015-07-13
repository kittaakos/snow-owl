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

import static com.google.common.collect.Lists.newLinkedList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.b2international.commons.FileUtils;
import com.b2international.snowowl.core.DefaultObjectMapper;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.branch.MockBranchManager;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.internal.branch.IndexBranchManagerImpl;
import com.b2international.snowowl.core.internal.branch.InternalBranch;
import com.b2international.snowowl.core.store.Store;
import com.b2international.snowowl.core.store.index.DefaultBulkIndex;
import com.b2international.snowowl.core.store.index.DefaultIndex;
import com.b2international.snowowl.core.store.index.Index;
import com.b2international.snowowl.core.store.index.IndexAdmin;
import com.b2international.snowowl.core.store.index.Mappings;
import com.b2international.snowowl.core.store.index.MultiIndex;
import com.b2international.snowowl.core.store.mem.MemStore;
import com.b2international.snowowl.core.terminology.Component;
import com.b2international.snowowl.core.tests.ESRule;
import com.b2international.snowowl.core.tests.person.Person;
import com.b2international.snowowl.core.tests.person.PersonFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TODO remove/delete test case
 * 
 * @since 5.0
 */
public class DefaultTransactionalIndexTest extends PersonFixtures {
	
	private static final String COMMIT_MESSAGE = "Commit Message XXX";
	
	@Rule
	public ESRule es = new ESRule();

	private AtomicLong clock = new AtomicLong(0L);
	private AtomicInteger commitIdProvider = new AtomicInteger(0);
	private Branch main;
	private ObjectMapper mapper;
	private IndexBranchManagerImpl manager;

	private Person person1_2015;
	private Person person1_YobChanged;
	private Person person1_FirstNameChanged;
	
	@Before
	public void givenTransactionalIndex() {
		this.mapper = new DefaultObjectMapper();
		final Store<InternalBranch> branchStore = new MemStore<>(InternalBranch.class);
		this.manager = new IndexBranchManagerImpl(branchStore, clock.incrementAndGet(), PersonFixtures.REPO_NAME, es.client(), Mappings.of(mapper, Person.class), null, clock);
		this.main = manager.getMainBranch();
		
		this.person1_2015 = createPerson1();
		this.person1_YobChanged = createPerson1();
		this.person1_YobChanged.setYob(1997);
		this.person1_FirstNameChanged = createPerson1();
		this.person1_FirstNameChanged.setFirstName("MAIN");
	}

	@Test(expected = NotFoundException.class)
	public void loadingMissingRevision_ShouldThrowNotFoundException() throws Exception {
		TransactionalIndex txIndex = createTransactionalIndex(main);
		txIndex.loadRevision(Person.class, Branch.MAIN_PATH, PERSON_1_STORAGEKEY);
	}

	private TransactionalIndex createTransactionalIndex(String path) {
		return createTransactionalIndex(manager.getBranch(path));
	}
	
	private TransactionalIndex createTransactionalIndex(Branch branch) {
		final LinkedList<String> indexes = newLinkedList(branch.metadata().get(IndexBranchManagerImpl.BRANCH_INDEXES, List.class));
		return new DefaultTransactionalIndex(new DefaultBulkIndex(new MultiIndex(es.client(), Mappings.of(mapper, Person.class), null, indexes)), manager);
	}
	
	@Test
	public void whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex() throws Exception {
		TransactionalIndex txIndex = createTransactionalIndex(main);
		final IndexTransaction tx = openTransaction(txIndex, main);
		tx.add(PERSON_1_STORAGEKEY, person1_2015);
		tx.commit(COMMIT_MESSAGE);
		
		final Person rev = txIndex.loadRevision(Person.class, Branch.MAIN_PATH, PERSON_1_STORAGEKEY);
		assertEquals(PERSON_1_STORAGEKEY, rev.getStorageKey());
		assertEquals(person1_2015, rev);
	}
	
	@Test
	public void whenCommittingTwoRevisionsOnMAIN_ThenLoadShouldReturnTheModifiedEntity() throws Exception {
		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
		
		final TransactionalIndex txIndex = createTransactionalIndex(main);
		final IndexTransaction tx2 = openTransaction(txIndex, main);
		tx2.add(PERSON_1_STORAGEKEY, person1_YobChanged);
		tx2.commit(COMMIT_MESSAGE);
		
		final Person rev = txIndex.loadRevision(Person.class, Branch.MAIN_PATH, PERSON_1_STORAGEKEY);
		assertEquals(person1_YobChanged, rev);
	}
	
	@Test
	public void whenCommittingFirstRevisionOnMAIN_AndCreatingEmptyBranch_ThenQueryOnBranchShouldReturnTheRevisionFromMAIN() throws Exception {
		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
		
		final Branch branchA = createBranch(Branch.MAIN_PATH, "a");
		final TransactionalIndex txIndex = createTransactionalIndex(branchA);
		final Person rev = txIndex.loadRevision(Person.class, branchA.path(), PERSON_1_STORAGEKEY);
		assertEquals(person1_2015, rev);
	}

	@Test
	public void whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch() throws Exception {
		whenCommittingFirstRevisionOnMAIN_ThenItShouldBeAvailableInMAINIndex();
		
		// open branch
		final Branch branchA = createBranch(Branch.MAIN_PATH, "a");
		
		// make commit on branch
		TransactionalIndex branchAIndex = createTransactionalIndex(branchA);
		final IndexTransaction tx = openTransaction(branchAIndex, branchA);
		tx.add(PERSON_1_STORAGEKEY, person1_YobChanged);
		tx.commit(COMMIT_MESSAGE);
		// verify that MAIN version still has yob 2015
		final TransactionalIndex mainIndex = createTransactionalIndex(main);
		final Person personMain = mainIndex.loadRevision(Person.class, Branch.MAIN_PATH, PERSON_1_STORAGEKEY);
		assertEquals(person1_2015, personMain);
		
		// verify that Branch A has new version with yob 1997
		branchAIndex = createTransactionalIndex(branchA.path());
		final Person personBranch = branchAIndex.loadRevision(Person.class, branchA.path(), PERSON_1_STORAGEKEY);
		assertEquals(person1_YobChanged, personBranch);
	}
	
	@Test
	public void whenCommittingOnBranchAndOnMain_ThenQueriesShouldReturnRespectiveVersions() throws Exception {
		// execute commit on Branch A
		whenCommittingSecondRevisionOnBranch_ThenQueryOnBranchShouldReturnRevisionOnBranch();
		
		// execute commit on MAIN
		TransactionalIndex mainIndex = createTransactionalIndex(main);
		final IndexTransaction tx = openTransaction(mainIndex, main);
		tx.add(PERSON_1_STORAGEKEY, person1_FirstNameChanged);
		tx.commit(COMMIT_MESSAGE);
		
		// verify that MAIN version still has yob 2015, but firstName 'MAIN'
		final Person personMain = mainIndex.loadRevision(Person.class, Branch.MAIN_PATH, PERSON_1_STORAGEKEY);
		assertEquals(person1_FirstNameChanged, personMain);
		
		// verify that Branch A has new version with yob 1997
		TransactionalIndex branchAIndex = createTransactionalIndex("MAIN/a");
		final Person personBranch = branchAIndex.loadRevision(Person.class, "MAIN/a", PERSON_1_STORAGEKEY);
		assertEquals(person1_YobChanged, personBranch);
	}
	
	@Test
	public void whenRebasingBranch_ThenQueryShouldReturnNewVersionOnBranch() throws Exception {
		whenCommittingFirstRevisionOnMAIN_AndCreatingEmptyBranch_ThenQueryOnBranchShouldReturnTheRevisionFromMAIN();
		
		// make a change on MAIN, but leave branch unchanged
		TransactionalIndex mainIndex = createTransactionalIndex(main);
		final IndexTransaction tx = openTransaction(mainIndex, main);
		tx.add(PERSON_1_STORAGEKEY, person1_FirstNameChanged);
		tx.commit(COMMIT_MESSAGE);

		// verify that person has Foo before rebase
		TransactionalIndex branchAIndex = createTransactionalIndex("MAIN/a");
		final Person personBeforeRebase = branchAIndex.loadRevision(Person.class, "MAIN/a", PERSON_1_STORAGEKEY);
		assertEquals(person1_2015, personBeforeRebase);
		
		// rebase branch should move baseTimestamp forward
		manager.getBranch("MAIN/a").rebase("Rebased branch A");
		// verify that person has MAIN after rebase
		branchAIndex = createTransactionalIndex("MAIN/a");
		final Person personAfterRebase = branchAIndex.loadRevision(Person.class, "MAIN/a", PERSON_1_STORAGEKEY);
		assertEquals(person1_FirstNameChanged, personAfterRebase);
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
	
	private IndexTransaction openTransaction(TransactionalIndex txIndex, final Branch branch) {
		final int commitId = commitIdProvider.incrementAndGet();
		final long commitTimestamp = clock.incrementAndGet();
		final IndexTransaction original = txIndex.transaction(commitId, commitTimestamp, branch.path());
		return new IndexTransaction() {

			@Override
			public <T extends Component> void delete(long storageKey, Class<T> type) {
				original.delete(storageKey, type);				
			}
			
			@Override
			public void commit(String commitMessage) {
				original.commit(commitMessage);
				// make commit available in the branch as timestamp
//				when(branch.headTimestamp()).thenReturn(commitTimestamp);
				manager.handleCommit(branch);
			}

			@Override
			public void add(long storageKey, Component object) {
				original.add(storageKey, object);
			}
			
			@Override
			public String branch() {
				return original.branch();
			}
			
		};
	}
	
	private Branch createBranch(String parent, String name) {
		return manager.getBranch(parent).createChild(name);
	}
	
}
