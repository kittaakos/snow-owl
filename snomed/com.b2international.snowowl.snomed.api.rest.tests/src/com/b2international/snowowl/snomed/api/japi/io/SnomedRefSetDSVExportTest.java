/*
 * Copyright 2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.japi.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.b2international.commons.FileUtils;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.datastore.file.FileRegistry;
import com.b2international.snowowl.datastore.request.CommitResult;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.constraint.SnomedConcreteDomainConstraint;
import com.b2international.snowowl.snomed.core.domain.constraint.SnomedConstraint;
import com.b2international.snowowl.snomed.core.domain.constraint.SnomedDescriptionConstraint;
import com.b2international.snowowl.snomed.core.domain.constraint.SnomedRelationshipConstraint;
import com.b2international.snowowl.snomed.core.lang.LanguageSetting;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.SnomedRefSetUtil;
import com.b2international.snowowl.snomed.datastore.internal.rf2.AbstractSnomedDsvExportItem;
import com.b2international.snowowl.snomed.datastore.internal.rf2.ComponentIdSnomedDsvExportItem;
import com.b2international.snowowl.snomed.datastore.internal.rf2.DatatypeSnomedDsvExportItem;
import com.b2international.snowowl.snomed.datastore.internal.rf2.SimpleSnomedDsvExportItem;
import com.b2international.snowowl.snomed.datastore.internal.rf2.SnomedDsvExportItemType;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedDescriptionCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.snomedrefset.DataType;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * @since 5.11
 */
public class SnomedRefSetDSVExportTest {

	private static final String REPOSITORY_ID = SnomedDatastoreActivator.REPOSITORY_UUID;
	
	private static final String MAIN_BRANCH = Branch.MAIN_PATH;

	private static final String DELIMITER = "|";
	
	private IEventBus bus;
	
	private FileRegistry fileRegistry;

	private File tempDir;
	
	@Before
	public void setup() {
		bus = ApplicationContext.getInstance().getService(IEventBus.class);
		fileRegistry = ApplicationContext.getInstance().getService(FileRegistry.class);
		tempDir = Files.createTempDir();
	}
	
	@After
	public void destroy() {
		FileUtils.deleteDirectory(tempDir);
	}
	
	@Test
	public void simpleTypeDSVExport() throws Exception {

		String refsetId = createRefset(SnomedRefSetType.SIMPLE);
		addMember(refsetId, Concepts.SUBSTANCE);
		addMember(refsetId, Concepts.FINDING_SITE);
		
		UUID fileId = 
			SnomedRequests.dsv()
				.prepareExport()
				.setLocales(locales())
				.setDelimiter(DELIMITER)
				.setDescriptionIdExpected(true)
				.setRelationshipTargetExpected(true)
				.setRefSetId(refsetId)
				.setExportItems(createExportItems(refsetId))
				.build(REPOSITORY_ID, MAIN_BRANCH)
				.execute(bus)
				.getSync();

		File dsvExportZipFile = new File(tempDir, String.format("dsv-export-%s.zip", fileId.toString()));
		OutputStream outputStream = new FileOutputStream(dsvExportZipFile);
		fileRegistry.download(fileId, outputStream);
		Assert.assertTrue("Export archive must exist!", dsvExportZipFile.exists());

		FileUtils.decompressZipArchive(dsvExportZipFile, tempDir);
		File decompressedDsvFile = new File(tempDir, refsetId+".csv");
		Assert.assertTrue("Uncompressed file must exist.", decompressedDsvFile.exists());

		List<String> dsvExportLines = Files.readLines(decompressedDsvFile, Charsets.UTF_8);
		Assert.assertTrue(MessageFormat.format("Expected 4 lines in the exported file (2 header and 2 member lines) instead of {0} lines.", dsvExportLines.size()), dsvExportLines.size() == 4);
	}
	
	@Test
	public void mapTypeDSVExport() throws Exception {

		String refsetId = createRefset(SnomedRefSetType.SIMPLE_MAP);
		addMember(refsetId, Concepts.SUBSTANCE, Collections.singletonMap(SnomedRf2Headers.FIELD_MAP_TARGET, "XXX"));
		addMember(refsetId, Concepts.FINDING_SITE, Collections.singletonMap(SnomedRf2Headers.FIELD_MAP_TARGET, "XXX"));
		
		UUID fileId = 
			SnomedRequests.dsv()
				.prepareExport()
				.setLocales(locales())
				.setDelimiter(DELIMITER)
				.setDescriptionIdExpected(true)
				.setRelationshipTargetExpected(true)
				.setRefSetId(refsetId)
				.setExportItems(createExportItems(refsetId))
				.build(REPOSITORY_ID, MAIN_BRANCH)
				.execute(bus)
				.getSync();

		File dsvExportZipFile = new File(tempDir, String.format("dsv-export-%s.zip", fileId.toString()));
		OutputStream outputStream = new FileOutputStream(dsvExportZipFile);
		fileRegistry.download(fileId, outputStream);
		Assert.assertTrue("Export archive must exist!", dsvExportZipFile.exists());

		FileUtils.decompressZipArchive(dsvExportZipFile, tempDir);
		File decompressedDsvFile = new File(tempDir, refsetId + ".csv");
		Assert.assertTrue("Uncompressed file must exist.", decompressedDsvFile.exists());

		List<String> dsvExportLines = Files.readLines(decompressedDsvFile, Charsets.UTF_8);
		Assert.assertTrue(MessageFormat.format("Expected 3 lines in the exported file (2 header and 2 member lines) instead of {0} lines.", dsvExportLines.size()), dsvExportLines.size() == 3);
	}

	private List<ExtendedLocale> locales() {
		return ApplicationContext.getInstance().getService(LanguageSetting.class).getLanguagePreference();
	}

	private List<AbstractSnomedDsvExportItem> createExportItems(String refsetId) {
			SnomedConcepts concepts = SnomedRequests.prepareSearchConcept()
				.all()
				.filterByEcl(String.format("^%s", refsetId))
				.build(REPOSITORY_ID, MAIN_BRANCH)
				.execute(bus)
				.getSync();
		
		return transformToExportItems(getConstraints(concepts));
	}

	private Iterable<SnomedConstraint> getConstraints(SnomedConcepts concepts) {
		return SnomedRequests
				.prepareGetApplicablePredicates(MAIN_BRANCH, idsOf(concepts), ancestorsOf(concepts), Collections.emptySet()).getSync();
	}

	private Set<String> idsOf(SnomedConcepts concepts) {
		return concepts.getItems().stream()
							.map(SnomedConcept::getId)
							.collect(Collectors.toSet());
	}

	private Set<String> ancestorsOf(SnomedConcepts concepts) {
		return concepts.getItems()
					.stream()
					.flatMap(item -> SnomedConcept.GET_ANCESTORS.apply(item).stream())
					.collect(Collectors.toSet());
	}

	private void addMember(String refsetId, String referencedComponentId) {
		addMember(refsetId, referencedComponentId, Collections.emptyMap());
	}
	
	private void addMember(String refsetId, String referencedComponentId, Map<String, Object> properties) {
		SnomedRequests.prepareNewMember()
				.setModuleId(Concepts.MODULE_SCT_CORE)
				.setActive(true)
				.setReferenceSetId(refsetId)
				.setReferencedComponentId(referencedComponentId)
				.setProperties(properties)
				.setId(UUID.randomUUID().toString())
				.build(REPOSITORY_ID, MAIN_BRANCH, "test", "test")
				.execute(bus)
				.getSync();
	}

	private String createRefset(SnomedRefSetType type) {
		SnomedDescriptionCreateRequestBuilder fsn = toDescriptionRequest(Concepts.FULLY_SPECIFIED_NAME, "term-test");
		SnomedDescriptionCreateRequestBuilder pt = toDescriptionRequest(Concepts.SYNONYM, "test");

		SnomedRelationshipCreateRequestBuilder statedIsA = toRelationshipRequest(Concepts.IS_A, CharacteristicType.STATED_RELATIONSHIP, SnomedRefSetUtil.getConceptId(type));
		SnomedRelationshipCreateRequestBuilder inferredIsA = toRelationshipRequest(Concepts.IS_A, CharacteristicType.INFERRED_RELATIONSHIP, SnomedRefSetUtil.getConceptId(type));

		SnomedRefSetCreateRequestBuilder refSet = toRefSetRequest(type);
		
		String conceptId = generateId();
		createConcept(fsn, pt, statedIsA, inferredIsA, refSet, conceptId);
		
		return conceptId;
	}

	private SnomedRefSetCreateRequestBuilder toRefSetRequest(SnomedRefSetType type) {
		return SnomedRequests.prepareNewRefSet()
				.setType(type)
				.setReferencedComponentType(SnomedTerminologyComponentConstants.CONCEPT);
	}

	private CommitResult createConcept(
			SnomedDescriptionCreateRequestBuilder fsn,
			SnomedDescriptionCreateRequestBuilder pt, 
			SnomedRelationshipCreateRequestBuilder statedIsA,
			SnomedRelationshipCreateRequestBuilder inferredIsA, 
			SnomedRefSetCreateRequestBuilder refSet,
			String conceptId) {
		
		SnomedConceptCreateRequestBuilder builder = 
				SnomedRequests.prepareNewConcept()
					.setId(conceptId)
					.setModuleId(Concepts.MODULE_SCT_CORE)
					.addDescription(fsn)
					.addDescription(pt)
					.addRelationship(statedIsA)
					.addRelationship(inferredIsA);
		if (refSet != null)
			builder.setRefSet(refSet);
		
		return builder
					.build(REPOSITORY_ID, MAIN_BRANCH, "test", "test")
					.execute(bus)
					.getSync();
	}

	private String generateId() {
		return SnomedRequests.identifiers()
					.prepareGenerate()
					.setNamespace(Concepts.B2I_NAMESPACE)
					.setCategory(ComponentCategory.CONCEPT)
					.setQuantity(1)
					.build(REPOSITORY_ID)
					.execute(bus)
				.getSync().first().orElseThrow(() -> new IllegalStateException("Couldn't generate identifier concept ID"));
	}

	private SnomedRelationshipCreateRequestBuilder toRelationshipRequest(String typeId, CharacteristicType characteristicType, String desctinationId) {
		return SnomedRequests.prepareNewRelationship()
				.setIdFromNamespace(Concepts.B2I_NAMESPACE)
				.setModuleId(Concepts.MODULE_SCT_CORE)
				.setDestinationId(desctinationId)
				.setTypeId(typeId)
				.setCharacteristicType(characteristicType);
	}

	private SnomedDescriptionCreateRequestBuilder toDescriptionRequest(String typeId, String term) {
		return SnomedRequests.prepareNewDescription()
				.setIdFromNamespace(Concepts.B2I_NAMESPACE)
				.setModuleId(Concepts.MODULE_SCT_CORE)
				.setTerm(term)
				.setTypeId(typeId)
				.preferredIn(Concepts.REFSET_LANGUAGE_TYPE_UK);
	}
	
	private List<AbstractSnomedDsvExportItem> transformToExportItems(final Iterable<SnomedConstraint> constraints) {

		List<AbstractSnomedDsvExportItem> results = Lists.newArrayList();
		

		for (final SnomedConstraint constraint : constraints) {
			if (constraint instanceof SnomedDescriptionConstraint) {
				final String descriptionTypeId = ((SnomedDescriptionConstraint) constraint).getTypeId();
				final ComponentIdSnomedDsvExportItem descriptionExportItem = new ComponentIdSnomedDsvExportItem(SnomedDsvExportItemType.DESCRIPTION, descriptionTypeId, descriptionTypeId);
				results.add(descriptionExportItem);
			} else if (constraint instanceof SnomedRelationshipConstraint) {
				final SnomedRelationshipConstraint relationshipConstraint = (SnomedRelationshipConstraint) constraint;
				final String matchingType = relationshipConstraint.getType();
				final ComponentIdSnomedDsvExportItem relationshipExportItem = new ComponentIdSnomedDsvExportItem(SnomedDsvExportItemType.RELATIONSHIP, matchingType, matchingType);
				results.add(relationshipExportItem);
			} else if (constraint instanceof SnomedConcreteDomainConstraint) {
				final SnomedConcreteDomainConstraint concreteDomainConstraint = (SnomedConcreteDomainConstraint) constraint;
				final String dataTypeName = concreteDomainConstraint.getValueType().getName();
				final boolean dataTypeBoolean = DataType.BOOLEAN.equals(concreteDomainConstraint.getValueType());
				
				final DatatypeSnomedDsvExportItem datatypeExportItem = new DatatypeSnomedDsvExportItem(SnomedDsvExportItemType.DATAYPE, dataTypeName, dataTypeBoolean);
				results.add(datatypeExportItem);
			}
		}
		
		results.add(new SimpleSnomedDsvExportItem(SnomedDsvExportItemType.PREFERRED_TERM));
		results.add(new SimpleSnomedDsvExportItem(SnomedDsvExportItemType.MODULE));
		results.add(new SimpleSnomedDsvExportItem(SnomedDsvExportItemType.EFFECTIVE_TIME));
		results.add(new SimpleSnomedDsvExportItem(SnomedDsvExportItemType.STATUS_LABEL));
		results.add(new SimpleSnomedDsvExportItem(SnomedDsvExportItemType.DEFINITION_STATUS));
		
		return results;
	}
}
