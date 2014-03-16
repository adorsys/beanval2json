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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.google.gson.GsonBuilder;

import de.adorsys.beanval2json.constraint.Constraints;
import de.adorsys.beanval2json.converter.BeanvalConverter;
import de.adorsys.beanval2json.converter.DigitsConverter;
import de.adorsys.beanval2json.converter.NumericConverter;
import de.adorsys.beanval2json.converter.PatternConverter;
import de.adorsys.beanval2json.converter.SizeConverter;

/**
 * AnnotationProcessor to convert Beanvalidation-Annotations to JSON
 * @author Florian Hirsch
 */
@SupportedAnnotationTypes({ "javax.validation.constraints.*" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({ "mappingFile", "messagesFile", "ignoreFile" })
public class BeanvalAnnotationProcessor extends AbstractProcessor {

	public static final String OPTION_MAPPING_FILE = "mappingFile";
	public static final String OPTION_MESSAGES_FILE = "messagesFile";
	public static final String OPTION_IGNORE_FILE = "ignoreFile";
	
	private Set<BeanvalConverter> converters;
	
	private ResourceHandler resourceHandler;
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		resourceHandler = new ResourceHandler(processingEnv.getFiler());
		Properties messages = null;
		Set<String> ignoredProperties = null;
		try {
			messages = resourceHandler.loadProperties(processingEnv.getOptions().get(OPTION_MESSAGES_FILE));
			ignoredProperties = resourceHandler.loadIgnoreFile(processingEnv.getOptions().get(OPTION_IGNORE_FILE));
		} catch (ProcessingException ex) {
			processingEnv.getMessager().printMessage(Kind.WARNING, ex.getMessage());
		}
		ConverterContext ctx = new ConverterContext(processingEnv.getTypeUtils(), processingEnv.getElementUtils(), messages, ignoredProperties);
		converters = new HashSet<>();
		converters.add(new BeanvalConverter(ctx));
		converters.add(new NumericConverter(ctx));
		converters.add(new DigitsConverter(ctx));
		converters.add(new PatternConverter(ctx));
		converters.add(new SizeConverter(ctx));		
 	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (!roundEnv.errorRaised() && !roundEnv.processingOver()) {
			try {
				processRound(annotations, roundEnv);
			} catch (ProcessingException ex) {
				processingEnv.getMessager().printMessage(Kind.WARNING, ex.getMessage());
			}
		}
		return false;
	}

	private void processRound(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws ProcessingException {
		Map<String, Constraints> constraints = new HashMap<>();
		for (TypeElement typeElement : annotations) {		
			BeanvalConverter converter = converterFor(typeElement);
			if (converter != null) {
				converter.addConstraints(typeElement, roundEnv, constraints);
			}
		}
		Map<String, Constraints> mappedConstraints = mapConstraints(constraints);
		resourceHandler.writeModel(toJson(mappedConstraints));
		processingEnv.getMessager().printMessage(Kind.NOTE, "Successfully converted Beanvalidation-Annotations to JSON");
	}
	
	private BeanvalConverter converterFor(TypeElement typeElement) {
		for (BeanvalConverter converter : converters) {
			if (converter.accepts(typeElement)) {
				return converter;
			}
		}
		String message = String.format("No Converter registered for typeElement '%s'", typeElement);
		processingEnv.getMessager().printMessage(Kind.WARNING, message);
		return null;
	}
	
	private Map<String, Constraints> mapConstraints(Map<String, Constraints> constraints) throws ProcessingException {
		Properties mapping = resourceHandler.loadProperties(processingEnv.getOptions().get(OPTION_MAPPING_FILE));
		if (mapping == null) {
			return constraints;
		}
		Map<String, Constraints> result = new HashMap<>();
		for (Map.Entry<String, Constraints> entry : constraints.entrySet()) {
			String key = mapping.containsKey(entry.getKey()) 
							? mapping.getProperty(entry.getKey()) 
							: entry.getKey();
			result.put(key, entry.getValue());
		}
		return result;
	}
	
	private String toJson(Map<String, Constraints> constraints) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setPrettyPrinting();
		return gsonBuilder.create().toJson(constraints);
	}
		
}
