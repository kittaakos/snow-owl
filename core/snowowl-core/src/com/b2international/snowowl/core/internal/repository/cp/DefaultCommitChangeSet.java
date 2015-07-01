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
package com.b2international.snowowl.core.internal.repository.cp;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import com.b2international.snowowl.core.repository.cp.CommitChangeSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Bare minimum implementation of the {@link CommitChangeSet} interface.
 */
@Immutable
public class DefaultCommitChangeSet implements CommitChangeSet {

	private final CDOView view;
	private final String userId;
	private final Collection<CDOObject> newComponents;
	private final Collection<CDOObject> dirtyComponents;
	private final Map<CDOID, EClass> detachedComponents;
	private final Map<CDOID, CDORevisionDelta> revisionDeltas;
	private final long timestamp;
	
	private DefaultCommitChangeSet(final CDOView view, final String userId, final Iterable<EObject> newComponents, final Iterable<EObject> dirtyComponents, final Map<CDOID, EClass> detachedComponents, final Map<CDOID, CDORevisionDelta> revisionDeltas, final long timestamp) {
		this.view = view;
		this.userId = userId;
		this.newComponents = Sets.newHashSet(Iterables.filter(newComponents, CDOObject.class));
		this.dirtyComponents = Sets.newHashSet(Iterables.filter(dirtyComponents, CDOObject.class));
		this.detachedComponents = detachedComponents;
		this.revisionDeltas = revisionDeltas;
		this.timestamp = timestamp;
	}
	
	public DefaultCommitChangeSet(final CDOView view, final String userId, final EObject[] newComponents, final EObject[] dirtyComponents, final Map<CDOID, EClass> detachedComponents, final Map<CDOID, CDORevisionDelta> revisionDeltas, final long timestamp) {
		this(
			Preconditions.checkNotNull(view, "CDO view argument cannot be null."),
			Preconditions.checkNotNull(userId, "User ID argument cannot be null."),
			Arrays.asList(Preconditions.checkNotNull(newComponents, "New components argument cannot be null.")),
			Arrays.asList(Preconditions.checkNotNull(dirtyComponents, "Dirty components argument cannot be null.")),
			Preconditions.checkNotNull(detachedComponents, "Detached components argument cannot be null."),
			checkNotNull(revisionDeltas, "revisionDeltas"),
			timestamp);
	}

	@Override
	public Collection<CDOObject> getNewComponents() {
		return Collections.unmodifiableCollection(newComponents);
	}
	
	@Override
	public Collection<CDOObject> getDirtyComponents() {
		return Collections.unmodifiableCollection(dirtyComponents);
	}
	
	@Override
	public Map<CDOID, EClass> getDetachedComponents() {
		return Collections.unmodifiableMap(detachedComponents);
	}
	
	@Override
	public String getUserId() {
		return userId;
	}
	
	@Override
	public CDOView getView() {
		return view;
	}
	
	@Override
	public Map<CDOID, CDORevisionDelta> getRevisionDeltas() {
		return unmodifiableMap(revisionDeltas);
	}
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}
}