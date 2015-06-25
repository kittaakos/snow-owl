package com.b2international.snowowl.core;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

/**
 * {@link TestRule} implementation to run a local Elasticsearch node.
 * 
 * @since 5.0
 */
public class ESRule extends ExternalResource {

	private Node node;
	
	@Override
	protected void before() throws Throwable {
		node = NodeBuilder.nodeBuilder().local(true).node();
	}
	
	@Override
	protected void after() {
		node.close();
	}
	
	public Client client() {
		return this.node.client();
	}
	
}
