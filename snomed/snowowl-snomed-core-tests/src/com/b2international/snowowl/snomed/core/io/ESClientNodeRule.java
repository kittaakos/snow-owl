package com.b2international.snowowl.snomed.core.io;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

/**
 * {@link TestRule} implementation to run an Elasticsearch client node.
 * 
 * @since 5.0
 */
public class ESClientNodeRule extends ExternalResource {

	private Node node;
	
	private final String clusterName;
	
	public ESClientNodeRule(String clusterName) {
		this.clusterName = clusterName;
	}

	@Override
	protected void before() throws Throwable {
		node = NodeBuilder.nodeBuilder().clusterName(clusterName).client(true).build().start();
	}
	
	@Override
	protected void after() {
		node.close();
	}
	
	public Client client() {
		return this.node.client();
	}
	
}
