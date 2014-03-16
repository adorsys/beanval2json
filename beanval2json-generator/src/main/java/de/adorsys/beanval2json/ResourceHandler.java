/**
 * Copyright (C) 2014 Florian Hirsch fhi@adorsys.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.adorsys.beanval2json;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public class ResourceHandler {

	private static final String MODEL_FILE = "constraints.json";
	
	private Filer filter;

	public ResourceHandler(Filer filter) {
		this.filter = filter;
	}
	
	public void writeModel(String json) throws ProcessingException {
		try {
			FileObject file = filter.createResource(StandardLocation.SOURCE_OUTPUT, "", MODEL_FILE);
			PrintWriter writer = new PrintWriter(file.openWriter());
			writer.print(json);
			writer.flush();
			writer.close();
		} catch (IOException ex) {
			throw new ProcessingException(String.format("IOException while writing file '%s': %s", MODEL_FILE, ex.getMessage()));
		}
	}
	
	public Properties loadProperties(String filename) throws ProcessingException {
		if (filename == null) {
			return null;
		}
		Properties properties = new Properties();
		try {
			properties.load(getClass().getClassLoader().getResourceAsStream(filename));
			return properties;
		} catch (IOException ex) {
			throw new ProcessingException(String.format("IOException while loading properties '%s': %s", filename, ex.getMessage()));
		}
	}
	
	public Set<String> loadIgnoreFile(String filename) throws ProcessingException {
		if (filename == null) {
			return null;
		}
		try {
			URI fileURI = getClass().getClassLoader().getResource(filename).toURI();
			List<String> allLines = Files.readAllLines(Paths.get(fileURI), Charset.forName("UTF-8"));
			return new HashSet<String>(allLines);			
		} catch (IOException | URISyntaxException ex) {
			throw new ProcessingException(String.format("Exception while loading ignoreFile '%s': %s", filename, ex.getMessage()));
		}
	}
	
}
