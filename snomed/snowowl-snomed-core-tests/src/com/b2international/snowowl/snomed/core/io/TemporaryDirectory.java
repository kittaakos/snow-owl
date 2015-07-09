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

import java.io.File;
import java.nio.file.Files;

import org.junit.rules.ExternalResource;

/**
 * @since 5.0
 */
public class TemporaryDirectory extends ExternalResource {

	private File parentDir;
	private File tmpDir;
	private String prefix;

	public TemporaryDirectory(String prefix) {
		this("target", prefix);
	}
	
	public TemporaryDirectory(String parent, String prefix) {
		parentDir = new File(parent);
		parentDir.mkdir();
		this.prefix = prefix;
	}
	
	@Override
	protected void before() throws Throwable {
		super.before();
		tmpDir = Files.createTempDirectory(parentDir.toPath(), prefix).toFile();
	}

	@Override
	protected void after() {
		super.after();
//		FileUtils.deleteDirectory(tmpDir);
		tmpDir = null;
	}
	
	public File getTmpDir() {
		return tmpDir;
	}
	
}
