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
package com.b2international.snowowl.datastore.server.internal.review;

import com.b2international.snowowl.datastore.server.review.BranchState;
import com.b2international.snowowl.datastore.server.review.Review;
import com.b2international.snowowl.datastore.server.review.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @since 5.0
 */
public abstract class ReviewImplMixin {

    @JsonCreator
    @JsonIgnoreProperties({ "sourcePath", "targetPath" })
    private ReviewImplMixin(@JsonProperty("id") final String id, 
    		@JsonProperty("source") final BranchState source, 
    		@JsonProperty("target") final BranchState target, 
    		@JsonProperty("status") final ReviewStatus status, 
    		@JsonProperty("deleted") final boolean deleted) {
        // Empty constructor body for mixin
    }

    @JsonProperty
    public abstract String id();

    @JsonProperty
    public abstract ReviewStatus status();

    @JsonProperty
    public abstract BranchState source();

    @JsonProperty
    public abstract BranchState target();

    @JsonProperty
    public abstract Review delete();

    @JsonProperty
    public abstract boolean isDeleted();

    @JsonProperty
    public abstract String sourcePath();

    @JsonProperty
    public abstract String targetPath();
}
