/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.datastore.server.cdo;

import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.spi.cdo.DefaultCDOMerger.ChangedInSourceAndDetachedInTargetConflict;
import org.eclipse.emf.spi.cdo.DefaultCDOMerger.ChangedInSourceAndTargetConflict;
import org.eclipse.emf.spi.cdo.DefaultCDOMerger.ChangedInTargetAndDetachedInSourceConflict;
import org.eclipse.emf.spi.cdo.DefaultCDOMerger.Conflict;

import com.b2international.snowowl.core.merge.MergeConflict;
import com.b2international.snowowl.core.merge.MergeConflict.ConflictType;
import com.b2international.snowowl.core.merge.MergeConflictImpl;

/**
 * @since 4.7
 */
public class ConflictMapper {

	public static MergeConflict convert(final Conflict conflict, final CDOTransaction sourceTransaction, final CDOTransaction targetTransaction) {
		if (conflict instanceof ChangedInSourceAndTargetConflict) {
			return from((ChangedInSourceAndTargetConflict) conflict);
		} else if (conflict instanceof ChangedInSourceAndDetachedInTargetConflict) {
			return from((ChangedInSourceAndDetachedInTargetConflict) conflict);
		} else if (conflict instanceof ChangedInTargetAndDetachedInSourceConflict) {
			return from((ChangedInTargetAndDetachedInSourceConflict) conflict);
		} else if (conflict instanceof AddedInSourceAndTargetConflict) {
			return from((AddedInSourceAndTargetConflict) conflict, sourceTransaction);
		} else if (conflict instanceof AddedInSourceAndDetachedInTargetConflict) {
			return from((AddedInSourceAndDetachedInTargetConflict) conflict, sourceTransaction);
		} else if (conflict instanceof AddedInTargetAndDetachedInSourceConflict) {
			return from((AddedInTargetAndDetachedInSourceConflict) conflict, targetTransaction);
		}
		throw new IllegalArgumentException("Unknown conflict type: " + conflict);
	}
	
	public static MergeConflict from(final ChangedInSourceAndTargetConflict conflict) {
		return MergeConflictImpl.builder()
				.withArtefactId(conflict.getTargetDelta().getID().toString())
				.withType(ConflictType.CONFLICTING_CHANGE)
				.build();
	}
	
	public static MergeConflict from(final ChangedInSourceAndDetachedInTargetConflict conflict) {
		return MergeConflictImpl.builder()
				.withArtefactId(conflict.getSourceDelta().getID().toString())
				.withType(ConflictType.DELETED_WHILE_CHANGED)
				.build();
	}
	
	public static MergeConflict from(final ChangedInTargetAndDetachedInSourceConflict conflict) {
		return MergeConflictImpl.builder()
				.withArtefactId(conflict.getTargetDelta().getID().toString())
				.withType(ConflictType.CHANGED_WHILE_DELETED)
				.build();
	}
	
	public static MergeConflict from(final AddedInSourceAndTargetConflict conflict, final CDOTransaction sourceTransaction) {
		return MergeConflictImpl.builder()
				.withArtefactId(conflict.getTargetId().toString())
				.withType(ConflictType.CONFLICTING_CHANGE)
				.build();
	}
	
	public static MergeConflict from(final AddedInSourceAndDetachedInTargetConflict conflict, final CDOTransaction sourceTransaction) {
		return MergeConflictImpl.builder()
				.withArtefactId(conflict.getTargetId().toString())
				.withType(ConflictType.CAUSES_MISSING_REFERENCE)
				.build();
	}
	
	public static MergeConflict from(final AddedInTargetAndDetachedInSourceConflict conflict, final CDOTransaction targetTransaction) {
		return MergeConflictImpl.builder()
				.withArtefactId(conflict.getTargetId().toString())
				.withType(ConflictType.HAS_MISSING_REFERENCE)
				.build();
	}
	
	public static Conflict invert(final Conflict conflict) {
		if (conflict instanceof ChangedInSourceAndDetachedInTargetConflict) {
			final ChangedInSourceAndDetachedInTargetConflict oldConflict = (ChangedInSourceAndDetachedInTargetConflict) conflict;
			return new ChangedInTargetAndDetachedInSourceConflict(oldConflict.getSourceDelta());
		} else if (conflict instanceof ChangedInTargetAndDetachedInSourceConflict) {
			final ChangedInTargetAndDetachedInSourceConflict oldConflict = (ChangedInTargetAndDetachedInSourceConflict) conflict;
			return new ChangedInSourceAndDetachedInTargetConflict(oldConflict.getTargetDelta());
		} else if (conflict instanceof ChangedInSourceAndTargetConflict) {
			final ChangedInSourceAndTargetConflict oldConflict = (ChangedInSourceAndTargetConflict) conflict;
			return new ChangedInSourceAndTargetConflict(oldConflict.getTargetDelta(), oldConflict.getSourceDelta());
		} else if (conflict instanceof AddedInSourceAndDetachedInTargetConflict) {
			final AddedInSourceAndDetachedInTargetConflict oldConflict = (AddedInSourceAndDetachedInTargetConflict) conflict;
			return new AddedInTargetAndDetachedInSourceConflict(oldConflict.getTargetId(), oldConflict.getSourceId(), oldConflict.getFeatureName());
		} else if (conflict instanceof AddedInTargetAndDetachedInSourceConflict) {
			final AddedInTargetAndDetachedInSourceConflict oldConflict = (AddedInTargetAndDetachedInSourceConflict) conflict;
			return new AddedInSourceAndDetachedInTargetConflict(oldConflict.getTargetId(), oldConflict.getSourceId(), oldConflict.getFeatureName());
		} else if (conflict instanceof AddedInSourceAndTargetConflict) {
			AddedInSourceAndTargetConflict oldConflict = (AddedInSourceAndTargetConflict) conflict;
			return new AddedInSourceAndTargetConflict(oldConflict.getTargetId(), oldConflict.getSourceId(), oldConflict.getMessage(), !oldConflict.isAddedInSource());
		}
		return conflict;
	}
	
	private ConflictMapper() { /* prevent instantiation */ }
}