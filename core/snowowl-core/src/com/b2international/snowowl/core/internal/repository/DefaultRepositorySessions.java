package com.b2international.snowowl.core.internal.repository;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.emf.cdo.common.revision.CDORevisionCache;
import org.eclipse.emf.cdo.common.revision.CDORevisionUtil;
import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.net4j.CDONet4jSessionConfiguration;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.jvm.JVMUtil;
import org.eclipse.net4j.util.container.IPluginContainer;
import org.eclipse.net4j.util.security.PasswordCredentialsProvider;
import org.elasticsearch.common.collect.MapMaker;

import com.b2international.snowowl.core.exceptions.SnowOwlException;
import com.b2international.snowowl.core.repository.RepositorySession;
import com.b2international.snowowl.core.repository.RepositorySessions;
import com.b2international.snowowl.core.session.Session;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

/**
 * @since 5.0
 */
public class DefaultRepositorySessions implements RepositorySessions {

	private ConcurrentMap<String, RepositorySession> activeSessions = new MapMaker().makeMap();
	
	private String repositoryId;
	private IConnector connector;

	public DefaultRepositorySessions(String repositoryId) {
		this.repositoryId = repositoryId;
		this.connector = JVMUtil.getConnector(IPluginContainer.INSTANCE, this.repositoryId);
	}
	
	@Override
	public RepositorySession open(Session session) {
		return open(session.getUser(), session.getSessionId().toCharArray());
	}
	
	@Override
	public RepositorySession open(String user, char[] token) {
		if (!activeSessions.containsKey(user)) {
			final PasswordCredentialsProvider credentials = new PasswordCredentialsProvider(user, token);
			// create CDO session
			final CDONet4jSessionConfiguration config = CDONet4jUtil.createNet4jSessionConfiguration();
			config.setRepositoryName(repositoryId);
			config.setRevisionManager(CDORevisionUtil.createRevisionManager(CDORevisionCache.NOOP));
			config.getAuthenticator().setCredentialsProvider(credentials);
			config.setConnector(connector);
			try {
				// try to open the session
				final CDONet4jSession cdoSession = config.openNet4jSession();
				// create repo level session
				final RepositorySession session = new DefaultRepositorySession(cdoSession);
				activeSessions.put(user, session);
			} catch (Exception e) {
				final Throwable cause = Throwables.getRootCause(e);
				if (cause instanceof SecurityException) {
					throw new SnowOwlException("Unknown application user %s", user);
				}
				throw e;
			}
		}
		return activeSessions.get(user);
	}

	@Override
	public Collection<RepositorySession> getSessions() {
		return ImmutableList.copyOf(activeSessions.values());
	}

}
