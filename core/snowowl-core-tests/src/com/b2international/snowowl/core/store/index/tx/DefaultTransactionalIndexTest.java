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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.b2international.snowowl.core.DefaultObjectMapper;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.branch.MockBranchManager;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.store.index.DefaultBulkIndex;
import com.b2international.snowowl.core.store.index.DefaultIndex;
import com.b2international.snowowl.core.store.index.Mappings;
import com.b2international.snowowl.core.terminology.Component;
import com.b2international.snowowl.core.tests.ESLocalNodeRule;
import com.b2international.snowowl.core.tests.person.Person;
import com.b2international.snowowl.core.tests.person.PersonFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 5.0
 */
public class DefaultTransactionalIndexTest extends PersonFixtures {

	private static final String COMMIT_MESSAGE = "Commit Message XXX";

	@Rule
	public ESLocalNodeRule es = new ESLocalNodeRule();

	private AtomicLong clock = new AtomicLong(0L);
	private AtomicInteger commitIdProvider = new AtomicInteger(0);
	private Branch main;
	private ObjectMapper mapper;
	private BranchManager manager;
	private TransactionalIndex txIndex;

	private Person person1_2015;
	private Person person1_YobChanged;
	private Person person1_FirstNameChanged;

	@Before
	public void givenTransactionalIndex() {
		this.mapper = new DefaultObjectMapper();
		this.manager = new MockBranchManager();
		this.main = manager.getMainBranch();

		this.txIndex = createTransactionalIndex();
		this.txIndex.admin().delete();
		this.txIndex.admin().create();

		this.person1_2015 = createPerson1();
		this.person1_YobChanged = createPerson1();
		this.person1_YobChanged.setYob(1997);
		this.person1_FirstNameChanged = createPerson1();
		this.person1_FirstNameChanged.setFirstName("MAIN");
	}

	private TransactionalIndex createTransactionalIndex() {
		return new DefaultTransactionalIndex(new DefaultBulkIndex(new DefaultIndex(es.client(), getClass().getSimpleName().toLowerCase(),
				Mappings.of(mapper, Person.class))), manager);
	}

	@Test(expected = NotFoundException.class)
	public void loadingMissingRevision_ShouldThrowNotFoundException() throws Exception {
		txIndex.loadRevision(Person.class, Branch.MAIN_PATH, PERSON_1_STORAGEKEY);
	}

	@Test
	public void commitOnMAIN_ShouldBeAvailable() throws Exception {
		final IndexTransaction tx = openTransaction(main);
		tx.add(PERSON_1_STORAGEKEY, person1_2015);
		tx.commit(COMMIT_MESSAGE);

		final Person rev = assertPersonAvailable("MAIN", PERSON_1_STORAGEKEY);
		assertEquals(person1_2015, rev);
	}

	@Test
	public void commitTwiceOnBranch_ShouldReturnLatestRevision() throws Exception {
		commitOnMAIN_ShouldBeAvailable();

		final IndexTransaction tx = openTransaction(main);
		tx.add(PERSON_1_STORAGEKEY, person1_YobChanged);
		tx.commit(COMMIT_MESSAGE);

		final Person rev = assertPersonAvailable("MAIN", PERSON_1_STORAGEKEY);
		assertEquals(person1_YobChanged, rev);
	}

	@Test
	public void emptyBranch_ShouldReturnRevisionFromParent() throws Exception {
		commitOnMAIN_ShouldBeAvailable();
		createBranch("MAIN", "a");
		final Person rev = assertPersonAvailable("MAIN/a", PERSON_1_STORAGEKEY);
		assertEquals(person1_2015, rev);
	}

	@Test
	public void commitOnBranch_ShouldBeAvailable() throws Exception {
		commitOnMAIN_ShouldBeAvailable();
		createBranch("MAIN", "a");
		commitOnBranch_ShouldBeAvailable("MAIN/a");
		// verify that Branch A has new version with yob 1997
		final Person personBranch = assertPersonAvailable("MAIN/a", PERSON_1_STORAGEKEY);
		assertEquals(person1_YobChanged, personBranch);
		// verify that MAIN version still has yob 2015
		final Person personMain = assertPersonAvailable("MAIN", PERSON_1_STORAGEKEY);
		assertEquals(person1_2015, personMain);
	}

	@Test
	public void commitOnMAINAndOnBranch_LoadShouldReturnRespectiveVersions() throws Exception {
		commitOnBranch_ShouldBeAvailable();

		final IndexTransaction tx = openTransaction(main);
		tx.add(PERSON_1_STORAGEKEY, person1_FirstNameChanged);
		tx.commit(COMMIT_MESSAGE);

		// verify that MAIN version still has yob 2015, but firstName 'MAIN'
		final Person personMain = assertPersonAvailable("MAIN", PERSON_1_STORAGEKEY);
		assertEquals(person1_FirstNameChanged, personMain);

		// verify that Branch A has new version with yob 1997
		final Person personBranch = assertPersonAvailable("MAIN/a", PERSON_1_STORAGEKEY);
		assertEquals(person1_YobChanged, personBranch);
	}

	@Ignore
	@Test
	public void whenRebasingBranch_ThenQueryShouldReturnNewVersionOnBranch() throws Exception {
		emptyBranch_ShouldReturnRevisionFromParent();

		// make a change on MAIN, but leave branch unchanged
		final IndexTransaction tx = openTransaction(main);
		tx.add(PERSON_1_STORAGEKEY, person1_FirstNameChanged);
		tx.commit(COMMIT_MESSAGE);

		// verify that person has Foo before rebase
		final Person personBeforeRebase = assertPersonAvailable("MAIN/a", PERSON_1_STORAGEKEY);
		assertEquals(person1_2015, personBeforeRebase);

		// rebase branch should move baseTimestamp forward
		manager.getBranch("MAIN/a").rebase("Rebased branch A");
		// verify that person has MAIN after rebase
		final Person personAfterRebase = assertPersonAvailable("MAIN/a", PERSON_1_STORAGEKEY);
		assertEquals(person1_FirstNameChanged, personAfterRebase);
	}

	@Test
	public void deleteRevisionOnBranch_ShouldThrowNotFound() throws Exception {
		commitOnMAIN_ShouldBeAvailable();
		deletePerson("MAIN", PERSON_1_STORAGEKEY);
		assertPersonNotAvailable("MAIN", PERSON_1_STORAGEKEY);
	}

	@Test
	public void deleteRevisionOnChild_ShouldThrowNotFoundOnChildButStillAvailableOnParent() throws Exception {
		commitOnMAIN_ShouldBeAvailable();
		createBranch("MAIN", "a");
		deletePerson("MAIN/a", PERSON_1_STORAGEKEY);
		assertPersonNotAvailable("MAIN/a", PERSON_1_STORAGEKEY);
		assertPersonAvailable("MAIN", PERSON_1_STORAGEKEY);
	}

	private Person assertPersonAvailable(String branch, long storageKey) {
		try {
			final Person person = txIndex.loadRevision(Person.class, branch, storageKey);
			assertNotNull("Person revision was empty", person);
			return person;
		} catch (NotFoundException e) {
			throw new AssertionError("Person is not available, branch: " + branch + ", person: " + storageKey, e);
		}
	}

	private void deletePerson(final String branchPath, final long storageKey) {
		final IndexTransaction tx = openTransaction(branchPath);
		tx.delete(storageKey, Person.class);
		tx.commit(COMMIT_MESSAGE);
	}

	private void assertPersonNotAvailable(final String branchPath, final long storageKey) {
		try {
			final Person p = txIndex.loadRevision(Person.class, branchPath, storageKey);
			fail("Person " + storageKey + "is still available on " + branchPath + " after deleting it: " + p);
		} catch (NotFoundException e) {
			// expected
		}
	}

	private void commitOnBranch_ShouldBeAvailable(String branchPath) {
		final IndexTransaction tx = openTransaction(branchPath);
		tx.add(PERSON_1_STORAGEKEY, person1_YobChanged);
		tx.commit(COMMIT_MESSAGE);
	}

	private IndexTransaction openTransaction(final String branch) {
		return openTransaction(manager.getBranch(branch));
	}

	private IndexTransaction openTransaction(final Branch branch) {
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
				when(branch.headTimestamp()).thenReturn(commitTimestamp);
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
