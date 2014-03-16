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
package de.adorsys.beanval2json.converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleAnnotationValueVisitor7;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;

import org.apache.commons.lang3.StringUtils;

import de.adorsys.beanval2json.ConverterContext;
import de.adorsys.beanval2json.ProcessingException;
import de.adorsys.beanval2json.constraint.Constraint;
import de.adorsys.beanval2json.constraint.Constraints;

/**
 * Default BeanvalConverter
 * Convertes Annotations listet by getAcceptedTypes to a Constraint.
 * Used for 
 * - javax.validation.constraints.AssertFalse
 * - javax.validation.constraints.AssertTrue
 * - javax.validation.constraints.Future
 * - javax.validation.constraints.NotNull
 * - javax.validation.constraints.Null
 * - javax.validation.constraints.Past
 * @author Florian Hirsch
 */
public class BeanvalConverter {

	private static final String METHOD_MESSAGE = "message";

	private static final String METHOD_PAYLOAD = "payload";
	
	private static final String METHOD_GROUPS = "groups";

	private static final String IDENTIFIER_MSG_KEY_START = "{";
	
	private static final String IDENTIFIER_MSG_KEY_END = "}";
	
	private static final String IDENTIFIER_SETTER = "set";
	
	protected ConverterContext ctx;
	
	public BeanvalConverter(ConverterContext ctx) {
		this.ctx = ctx;
	}

	/**
	 * @return all Annotations which can be converted by this Converter
	 */
	public List<Class<? extends Annotation>> getAcceptedTypes() {
		return Arrays.asList(AssertFalse.class, AssertTrue.class, Future.class, NotNull.class, Null.class, Past.class);
	}
	
	/**
	 * @return true if this converter can handle the given TypeElement
	 */
	public boolean accepts(TypeElement typeElement) {
		if (typeElement == null || getAcceptedTypes() == null) {
			return false;
		}
		for (Class<? extends Annotation> annotation : getAcceptedTypes()) {
			TypeElement annotationType = ctx.getElementUtils().getTypeElement(annotation.getName());
			if (ctx.getTypeUtils().isSameType(typeElement.asType(), annotationType.asType())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Searches for all properties which are annotated with given TypeElement and
	 * updates the constraintsMap with the according constraints
	 */
	public void addConstraints(TypeElement typeElement, RoundEnvironment roundEnv, Map<String, Constraints> constraintsMap) throws ProcessingException {
		for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
			String name = getFqn(element);
			if (ctx.ignoreProperty(name)) {
				continue;
			}
			for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
				if (!ctx.getTypeUtils().isSameType(typeElement.asType(), annotationMirror.getAnnotationType())) {
					continue;
				}
				Constraint constraint = convertConstraint(annotationMirror);
				Constraints constraints = getConstraints(constraintsMap, name);
				try {
					String methodName = String.format("%s%s", IDENTIFIER_SETTER, typeElement.getSimpleName());
					constraints.getClass().getMethod(methodName, constraint.getClass()).invoke(constraints, constraint);
				} catch (IllegalAccessException 
							| IllegalArgumentException 
							| InvocationTargetException 
							| NoSuchMethodException
							| SecurityException ex) {
					throw new ProcessingException(String.format("Could not add %s-Constraint from %s: %s", 
									typeElement.getSimpleName(), element.getSimpleName(), ex.getMessage()));					
				}
			}
		}
	}
	
	/**
	 * Converts an AnnotationMirror to a constraint. 
	 * Should be overriden by specific converters. 
	 */
	protected Constraint convertConstraint(AnnotationMirror annotationMirror) {
		Constraint constraint = new Constraint();
		setConstraintDefaults(constraint, ctx.getElementUtils().getElementValuesWithDefaults(annotationMirror));
		return constraint;
	}
	
	/**
	 * sets the defaultValues of a constraint
	 */
	protected void setConstraintDefaults(Constraint constraint, Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues) {
		constraint.setMessage(getValue(METHOD_MESSAGE, annotationValues).accept(MESSAGE_VISITOR, ctx.getMessages()));
		constraint.setPayload(getValue(METHOD_PAYLOAD, annotationValues).accept(ARRAY_VISITOR, null));
		constraint.setGroups(getValue(METHOD_GROUPS, annotationValues).accept(ARRAY_VISITOR, null));
	}

	/**
	 * @return the Constraints for given propertyName
	 * or a new one if not present in the map.
	 */
	private Constraints getConstraints(Map<String, Constraints> constraintsMap, String propertyName) {
		Constraints constraints = constraintsMap.get(propertyName);
		if (constraints == null) {
			constraints = new Constraints();
			constraintsMap.put(propertyName, constraints);
		}
		return constraints;
	}
	
	/**
	 * @return the full qualified name for given element
	 */
	private String getFqn(Element element) {
		if (element == null) {
			return "";
		}		
		return String.format("%s.%s.%s", ctx.getElementUtils().getPackageOf(element), element.getEnclosingElement().getSimpleName(), element.getSimpleName().toString());
	}
	
	/**
	 * Something like map.get(key). Anyone knows how to easily create an ExecutableElement?
	 */
	protected AnnotationValue getValue(String methodName, Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues) {
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationValues.entrySet()) {			
			if (entry.getKey().getSimpleName().contentEquals(methodName)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	/**
	 * Visitor which parses the message from a javax.validation.Constraint.
	 * If the message is parenthesized in curley brackets like 
	 * {javax.validation.constraints.NotNull.message} this method will check
	 * if there is a according property in the messagesFile. 
	 */
	private static final AnnotationValueVisitor<String, Properties> MESSAGE_VISITOR = new SimpleAnnotationValueVisitor7<String, Properties>() {
		@Override
		public String visitString(String value, Properties messages) {
			if (StringUtils.isBlank(value)) {
				return null;
			}
			if (value.startsWith(IDENTIFIER_MSG_KEY_START) 
					&& value.endsWith(IDENTIFIER_MSG_KEY_END)
					&& messages != null) {
				return messages.getProperty(value.substring(1, value.length() - 1));
			}
			return value;
		}
	};
	
	/**
	 * Visitor which parses all ArrayProperties from a javax.validation.constraint
	 */
	public static final AnnotationValueVisitor<List<String>, Void> ARRAY_VISITOR = new SimpleAnnotationValueVisitor7<List<String>, Void>() {
		@Override
		public List<String> visitArray(List<? extends AnnotationValue> values, Void p) {
			if (values == null || values.isEmpty()) {
				return null;
			}
			List<String> result = new ArrayList<>();
			for (AnnotationValue value : values) {
				result.add(value.getValue().toString());
			}
			return result;
		}
	};
	
}
