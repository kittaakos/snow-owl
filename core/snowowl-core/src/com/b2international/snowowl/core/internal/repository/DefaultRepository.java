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
package com.b2international.snowowl.core.internal.repository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.eclipse.emf.cdo.common.branch.CDOBranch;
import org.eclipse.emf.cdo.common.branch.CDOBranchManager;
import org.eclipse.emf.cdo.common.model.EMFUtil;
import org.eclipse.emf.cdo.server.CDOServerUtil;
import org.eclipse.emf.cdo.server.IRepository;
import org.eclipse.emf.cdo.server.db.CDODBUtil;
import org.eclipse.emf.cdo.server.db.IDBStore;
import org.eclipse.emf.cdo.server.db.mapping.IMappingStrategy;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.net4j.db.DBUtil;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.IDBConnectionProvider;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.container.IPluginContainer;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.slf4j.Logger;

import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.conflict.ICDOConflictProcessor;
import com.b2international.snowowl.core.log.Loggers;
import com.b2international.snowowl.core.repository.config.RepositoryConfiguration;
import com.b2international.snowowl.core.terminology.Component;

/**
 * @since 5.0
 */
public class DefaultRepository implements InternalRepository {

	private static final Logger LOG = Loggers.REPOSITORY.log();

	private String id;
	private String name;
	private Collection<Class<? extends Component>> components;
	private RepositoryConfiguration configuration;

	private org.eclipse.emf.cdo.spi.server.InternalRepository cdoRepository;

	private Collection<EPackage> ePackages;

	// TODO create customized local RepositoryConfiguration and RepositoryInfo
	/*package*/ DefaultRepository(String name, Collection<Class<? extends Component>> components, Collection<EPackage> ePackages, RepositoryConfiguration configuration) {
		this.id = name.replaceAll(" ", "_").toLowerCase(); 
		this.name = name;
		this.components = components;
		checkArgument(!this.components.isEmpty(), "At least one component is required repository '%s'", name);
		this.ePackages = ePackages;
		checkArgument(!this.ePackages.isEmpty(), "At least one ePackage is required repository '%s'", name);
		this.configuration = checkNotNull(configuration, "Configuration may not be null");
	}
	
	@Override
	public ICDOConflictProcessor getConflictProcessor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public BranchManager branching() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String name() {
		return this.name;
	}
	
	@Override
	public String id() {
		return this.id;
	}

	@Override
	public CDOBranch getCdoMainBranch() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IRepository getCdoRepository() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CDOBranchManager getCdoBranchManager() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CDOTransaction createTransaction(CDOBranch branch) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void activate() {
		// TODO remove implicit container reference, minor issue
		final IPluginContainer container = IPluginContainer.INSTANCE;
		final Map<Object, Object> datasourceProperties = configuration.getDatasourceProperties(id());
		LOG.info("Starting '{}' repository, config: {}", name(), datasourceProperties);
		this.cdoRepository = createRepository(container, datasourceProperties);
		this.cdoRepository.setInitialPackages(getEPackages());
		// TODO add change processor
//		repository.addHandler(changeManager);
		CDOServerUtil.addRepository(container, this.cdoRepository);
		
//		final IIDHandler idHandler =
//				new org.eclipse.emf.cdo.server.internal.db.LongIDHandler((org.eclipse.emf.cdo.server.internal.db.DBStore) dbStore);
//	
//		idHandler.setLastObjectID(CDOIDUtil.createLong(((long) getNamespaceId()) << 56L));
//		((org.eclipse.emf.cdo.server.internal.db.DBStore) dbStore).setIdHandler(idHandler);
		
		LOG.info("Successfully started '{}' repository", name());
	}

	@Override
	public void deactivate() {
		LOG.info("Stopping '{}' repository", name());
		LifecycleUtil.deactivate(cdoRepository);
		LOG.info("Successfully stopped '{}' repository", name());
	}
	
	private org.eclipse.emf.cdo.spi.server.InternalRepository createRepository(final IManagedContainer container, final Map<Object, Object> datasourceProperties) {

		final DataSource dataSource = DBUtil.createDataSource(datasourceProperties, ""); // no namespace required
		final IDBConnectionProvider connectionProvider = DBUtil.createConnectionProvider(dataSource);
		final IDBStore dbStore = createDBStore(connectionProvider);
		
		final Map<String, String> properties = new HashMap<String, String>();
		// TODO do we need overriden UUID
//		properties.put(IRepository.Props.OVERRIDE_UUID, getUuid());
		properties.put(IRepository.Props.SUPPORTING_AUDITS, Boolean.TRUE.toString());
		properties.put(IRepository.Props.SUPPORTING_BRANCHES, Boolean.TRUE.toString());

		final org.eclipse.emf.cdo.spi.server.InternalRepository repository = (org.eclipse.emf.cdo.spi.server.InternalRepository) CDOServerUtil.createRepository(this.id, dbStore, properties);

//		final SnowowlSessionManager sessionManager = new SnowowlSessionManager();

//		final UserManager userManager = new UserManager();
//		userManager.activate();
//		((InternalSessionManager)sessionManager).setUserManager(userManager);
//		repository.setSessionManager(sessionManager);

//		LOGGER.info(MessageFormat.format("Starting repository ''{0}'' with JDBC URL ''{1}''.",
//				name(), configuration.getDatabaseUrl().build(getStoreName())));
		

		return repository;
	}
	
	private IDBStore createDBStore(final IDBConnectionProvider connectionProvider) {
		final IMappingStrategy mappingStrategy = CDODBUtil.createHorizontalMappingStrategy(true, true, true); // with ranges, audit and branching

		final Map<String, String> properties = new HashMap<String, String>();
		properties.put(IMappingStrategy.PROP_QUALIFIED_NAMES, "true");
		mappingStrategy.setProperties(properties);

		final String databaseType = configuration.getDatabaseConfiguration().getType();
		final IDBAdapter dbAdapter = DBUtil.getDBAdapter(databaseType);
		checkArgument(dbAdapter != null, "DB adapter not found for id: %s", databaseType);
	    return CDODBUtil.createStore(mappingStrategy, dbAdapter, connectionProvider);
	}
	
	private EPackage[] getEPackages() {
		final Set<EPackage> ePackageSet = newHashSet();
		for (final EPackage ePackage : ePackages) {
			if (ePackageSet.add(ePackage)) {
				collectDependencies(ePackage, ePackageSet);
			}
		}
		return ePackageSet.toArray(new EPackage[ePackageSet.size()]);
	}
	
	/*Collects dependencies for an EPackage*/
	private static void collectDependencies(final EPackage ePackage, final Set<EPackage> dependencies) {
		
		final Resource eResource = ePackage.eResource();
		final Collection<EObject> crossReferencedElements = EcoreUtil.ExternalCrossReferencer.find(eResource).keySet();
		
		for (final Object crossReferencedElement : crossReferencedElements) {
			
			if (!(crossReferencedElement instanceof EClassifier)) {
				continue;
			}
			
			final EClassifier eClass = (EClassifier) crossReferencedElement;
			final EPackage referencedPackage = eClass.getEPackage();
			
			if (referencedPackage == null) {
				continue;
			}

			final EPackage topPackage = EMFUtil.getTopLevelPackage(referencedPackage);
			
			if (dependencies.add(topPackage)) {
				collectDependencies(topPackage, dependencies);
			}
		}
	}

}
