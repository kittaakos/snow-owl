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
package com.b2international.snowowl.snomed.core.io;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.rules.ExternalResource;

/**
 * @since 5.0
 */
public class ESTransportClientRule extends ExternalResource {

	private Client client;
	private String host;
	private int port;
	private String clusterName;

	public ESTransportClientRule(String clusterName) {
		this(clusterName, "localhost", 9300);
	}
	
	public ESTransportClientRule(String clusterName, String host, int port) {
		this.clusterName = clusterName;
		this.host = host;
		this.port = port;
	}
	
	@Override
	protected void before() throws Throwable {
		super.before();
		this.client = new TransportClient(ImmutableSettings.builder().put("cluster.name", clusterName).build()).addTransportAddress(new InetSocketTransportAddress(host, port));
	}
	
	@Override
	protected void after() {
		super.after();
		this.client.close();
		this.client = null;
	}
	
	public Client client() {
		return client;
	}
	
}
