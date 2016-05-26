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
package com.b2international.snowowl.snomed.datastore.index.entry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import com.b2international.snowowl.core.api.IComponent;
import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.datastore.index.RevisionDocument;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Predicate;

/**
 * Common superclass for SNOMED CT transfer objects.
 */
public abstract class SnomedDocument extends RevisionDocument implements IComponent<String>, Serializable {

	public static final Predicate<SnomedDocument> ACTIVE_PREDICATE = new Predicate<SnomedDocument>() {
		@Override
		public boolean apply(SnomedDocument input) {
			return input.isActive();
		}
	};

	// XXX: Type parameter reveals subclass to AbstractBuilder for fluent API
	protected static abstract class SnomedDocumentBuilder<B extends SnomedDocumentBuilder<B>> extends RevisionDocumentBuilder<B> {

		protected String moduleId;
		protected boolean active;
		protected boolean released;
		protected long effectiveTime;

		public B moduleId(final String moduleId) {
			this.moduleId = moduleId;
			return getSelf();
		}

		public B active(final boolean active) {
			this.active = active;
			return getSelf();
		}

		public B released(final boolean released) {
			this.released = released;
			return getSelf();
		}

		public B effectiveTime(final long effectiveTime) {
			this.effectiveTime = effectiveTime;
			return getSelf();
		}

	}
	
	public static class Fields {
		public static final String MODULE_ID = "moduleId";
		public static final String RELEASED = "released";
		public static final String ACTIVE = "active";
		public static final String EFFECTIVE_TIME = "effectiveTime";
	}

	protected final String moduleId;
	protected final boolean released;
	protected final boolean active;
	protected final long effectiveTime;

	protected SnomedDocument(final String id,
			final String label,
			final String iconId, 
			final String moduleId, 
			final boolean released, 
			final boolean active, 
			final long effectiveTime) {
		super(id, 
				label == null ? String.format("!!!%s!!!", id) : label, // XXX use ID with markers to indicate problems when fetching entries without label on the client side
				iconId);

		checkArgument(effectiveTime >= EffectiveTimes.UNSET_EFFECTIVE_TIME, "Effective time argument '%s' is invalid.", effectiveTime);
		this.moduleId = checkNotNull(moduleId, "Component module identifier may not be null.");
		this.released = released;
		this.active = active;
		this.effectiveTime = effectiveTime;
	}

	/**
	 * @return {@code true} if the component has already appeared in an RF2 release, {@code false} otherwise
	 */
	public boolean isReleased() {
		return released;
	}

	/**
	 * @return {@code true} if the component is active, {@code false} otherwise
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @return the module concept identifier of this component
	 */
	public String getModuleId() {
		return moduleId;
	}

	/**
	 * @return the effective time of the component, or {@link EffectiveTimes#UNSET_EFFECTIVE_TIME} if the component currently has
	 *         no effective time set
	 */
	public long getEffectiveTime() {
		return effectiveTime;
	}

	/**
	 * @return the effective time of the component formatted using {@link EffectiveTimes#format(Object)}, or
	 *         {@link EffectiveTimes#UNSET_EFFECTIVE_TIME_LABEL} if the component currently has no effective time set
	 */
	@JsonIgnore
	public String getEffectiveTimeAsString() {
		return EffectiveTimes.format(effectiveTime);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		final SnomedDocument other = (SnomedDocument) obj;
		return Objects.equal(getId(), other.getId());
	}

	protected ToStringHelper toStringHelper() {
		return Objects.toStringHelper(this)
				.add("id", getId())
				.add("label", getLabel())
				.add("iconId", getIconId())
				.add("moduleId", moduleId)
				.add("released", released)
				.add("active", active)
				.add("effectiveTime", effectiveTime);
	}
}