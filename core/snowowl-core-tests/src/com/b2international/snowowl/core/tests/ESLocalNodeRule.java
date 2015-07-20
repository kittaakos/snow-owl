package com.b2international.snowowl.core.tests;

import java.util.UUID;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

/**
 * {@link TestRule} implementation to run a local Elasticsearch node.
 * 
 * @since 5.0
 */
public class ESLocalNodeRule extends ExternalResource {

	private Node node;
	
	@Override
	protected void before() throws Throwable {
		final Builder settings = ImmutableSettings.builder();
		settings.put("script.engine.groovy.inline.aggs", "on");
		settings.put("script.engine.groovy.inline.mapping", "on");
		settings.put("script.engine.groovy.inline.search", "on");
		settings.put("script.engine.groovy.inline.update", "on");
		settings.put("script.engine.groovy.inline.plugin", "on");
		settings.put("script.inline", "on");
		settings.put("script.indexed", "on");
		node = NodeBuilder.nodeBuilder().settings(settings).clusterName(UUID.randomUUID().toString()).local(true).node();
	}
	
	@Override
	protected void after() {
		node.close();
	}
	
	public Client client() {
		return this.node.client();
	}
	
}
