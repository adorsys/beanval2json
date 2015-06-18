/**
 * Copyright (C) 2014 Florian Hirsch
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

import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Context for the Converters
 * @author Florian Hirsch
 */
public class ConverterContext {

	private Types typeUtils;
	
	private Elements elementUtils;
	
	private Properties messages;
	
	private Set<String> ignoredProperties;

	public ConverterContext(Types typeUtils, Elements elementUtils, Properties messages, Set<String> ignoredProperties) {
		this.typeUtils = typeUtils;
		this.elementUtils = elementUtils;
		this.messages = messages;
		this.ignoredProperties = ignoredProperties;
	}

	public Types getTypeUtils() {
		return typeUtils;
	}

	public Elements getElementUtils() {
		return elementUtils;
	}
	
	public Properties getMessages() {
		return messages;
	}

	public boolean ignoreProperty(String fqn) {
		if (ignoredProperties == null) {
			return false;
		}
		for (String property : ignoredProperties) {
			if (Pattern.compile(property).matcher(fqn).matches()) {
				return true;
			}
		}
		return false;
	}
	
}
