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
package com.b2international.snowowl.core;

import com.b2international.snowowl.core.internal.branch.BranchImpl;
import com.b2international.snowowl.core.internal.branch.BranchImplMixin;
import com.b2international.snowowl.core.internal.branch.CDOBranchImpl;
import com.b2international.snowowl.core.internal.branch.CDOBranchImplMixin;
import com.b2international.snowowl.core.internal.branch.CDOMainBranchImpl;
import com.b2international.snowowl.core.internal.branch.CDOMainBranchImplMixin;
import com.b2international.snowowl.core.internal.branch.InternalBranch;
import com.b2international.snowowl.core.internal.branch.InternalBranchMixin;
import com.b2international.snowowl.core.internal.branch.MainBranchImpl;
import com.b2international.snowowl.core.internal.branch.MainBranchImplMixin;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

/**
 * @since 5.0
 */
public class DefaultObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = 4051979122143555810L;

	public DefaultObjectMapper() {
		super();
		setVisibility(PropertyAccessor.SETTER, Visibility.NON_PRIVATE);
		setVisibility(PropertyAccessor.CREATOR, Visibility.NON_PRIVATE);
		disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		initMixinAnnotations();
		registerModule(new AfterburnerModule());
	}
	
	private void initMixinAnnotations() {
		addMixInAnnotations(Metadata.class, MetadataMixin.class);
		addMixInAnnotations(MetadataHolder.class, MetadataHolderMixin.class);
		addMixInAnnotations(BranchImpl.class, BranchImplMixin.class);
		addMixInAnnotations(MainBranchImpl.class, MainBranchImplMixin.class);
		addMixInAnnotations(CDOBranchImpl.class, CDOBranchImplMixin.class);
		addMixInAnnotations(CDOMainBranchImpl.class, CDOMainBranchImplMixin.class);
		addMixInAnnotations(InternalBranch.class, InternalBranchMixin.class);
	}
	
}
