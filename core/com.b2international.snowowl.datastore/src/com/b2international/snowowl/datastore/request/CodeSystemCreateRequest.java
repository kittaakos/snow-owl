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
package com.b2international.snowowl.datastore.request;

import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.BaseRequest;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.datastore.TerminologyRegistryService;
import com.b2international.snowowl.datastore.UserBranchPathMap;
import com.b2international.snowowl.terminologymetadata.CodeSystem;
import com.b2international.snowowl.terminologymetadata.TerminologymetadataFactory;

/**
 * @since 4.7
 */
public final class CodeSystemCreateRequest extends BaseRequest<TransactionContext, String> {

	private static final long serialVersionUID = 1L;

	private String branchPath;
	private String citation;
	private String oid;
	private String iconPath;
	private String language;
	private String link;
	private String name;
	private String repositoryUuid;
	private String shortName;
	private String terminologyId;

	CodeSystemCreateRequest() {
	}

	void setBranchPath(String branchPath) {
		this.branchPath = branchPath;
	}

	void setCitation(String citation) {
		this.citation = citation;
	}

	void setOid(String oid) {
		this.oid = oid;
	}

	void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	void setLanguage(String language) {
		this.language = language;
	}

	void setLink(String link) {
		this.link = link;
	}

	void setName(String name) {
		this.name = name;
	}

	void setRepositoryUuid(String repositoryUuid) {
		this.repositoryUuid = repositoryUuid;
	}

	void setShortName(String shortName) {
		this.shortName = shortName;
	}

	void setTerminologyId(String terminologyId) {
		this.terminologyId = terminologyId;
	}

	@Override
	public String execute(final TransactionContext context) {
		ensureOidAndShortNameUnique(context);
		final CodeSystem codeSystem = createCodeSystem();
		context.add(codeSystem);

		return codeSystem.getShortName();
	}

	private void ensureOidAndShortNameUnique(final TransactionContext context) {
		final UserBranchPathMap branchPathMap = new UserBranchPathMap();
		branchPathMap.putBranchPath(repositoryUuid, BranchPathUtils.createMainPath());

		final TerminologyRegistryService registryService = context.service(TerminologyRegistryService.class);

		if (registryService.getCodeSystemByOid(branchPathMap, oid) != null) {
			throw new BadRequestException("Couldn't create code system with OID %s as it is not unique.", oid);
		}

		if (registryService.getCodeSystemByShortName(branchPathMap, shortName) != null) {
			throw new BadRequestException("Couldn't create code system with short name %s as it is not unique.", shortName);
		}
	}

	private CodeSystem createCodeSystem() {
		final CodeSystem codeSystem = TerminologymetadataFactory.eINSTANCE.createCodeSystem();
		codeSystem.setBranchPath(branchPath);
		codeSystem.setCitation(citation);
		codeSystem.setCodeSystemOID(oid);
		codeSystem.setIconPath(iconPath);
		codeSystem.setLanguage(language);
		codeSystem.setMaintainingOrganizationLink(link);
		codeSystem.setName(name);
		codeSystem.setRepositoryUuid(repositoryUuid);
		codeSystem.setShortName(shortName);
		codeSystem.setTerminologyComponentId(terminologyId);

		return codeSystem;
	}

	@Override
	protected Class<String> getReturnType() {
		return String.class;
	}

}