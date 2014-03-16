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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.validation.constraints.Pattern;

import de.adorsys.beanval2json.ConverterContext;
import de.adorsys.beanval2json.constraint.Constraint;
import de.adorsys.beanval2json.constraint.PatternConstraint;

/**
 * converter for javax.validation.constraints.Pattern
 * @author Florian Hirsch
 */
public class PatternConverter extends BeanvalConverter {

	private static final String METHOD_REGEXP = "regexp";
	
	private static final String METHOD_FLAGS = "flags";
	
	public PatternConverter(ConverterContext ctx) {
		super(ctx);
	}
	
	@Override
	public List<Class<? extends Annotation>> getAcceptedTypes() {
		List<Class<? extends Annotation>> acceptedTypes = new ArrayList<>();
		acceptedTypes.add(Pattern.class);
		return acceptedTypes;
	}

	@Override
	protected Constraint convertConstraint(AnnotationMirror annotationMirror) {		
		PatternConstraint constraint = new PatternConstraint();
		Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues = ctx.getElementUtils().getElementValuesWithDefaults(annotationMirror);
		setConstraintDefaults(constraint, annotationValues);
		constraint.setRegexp(getValue(METHOD_REGEXP, annotationValues).getValue().toString());
		constraint.setFlags(getValue(METHOD_FLAGS, annotationValues).accept(ARRAY_VISITOR, null));
		return constraint;
	}	
	
}
