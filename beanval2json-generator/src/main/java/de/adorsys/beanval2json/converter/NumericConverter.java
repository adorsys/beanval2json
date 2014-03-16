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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import de.adorsys.beanval2json.ConverterContext;
import de.adorsys.beanval2json.constraint.Constraint;
import de.adorsys.beanval2json.constraint.NumericConstraint;

/**
 * converter for 
 * - javax.validation.constraints.DecimalMin
 * - javax.validation.constraints.DecimalMax
 * - javax.validation.constraints.Min
 * - javax.validation.constraints.Max
 * @author Florian Hirsch
 */
public class NumericConverter extends BeanvalConverter {

	private static final String METHOD_VALUE = "value";
	
	private static final String METHOD_INCLUSIVE = "inclusive";
	
	public NumericConverter(ConverterContext ctx) {
		super(ctx);
	}
	
	@Override
	public List<Class<? extends Annotation>> getAcceptedTypes() {
		return Arrays.asList(DecimalMin.class, DecimalMax.class, Min.class, Max.class);
	}

	@Override
	protected Constraint convertConstraint(AnnotationMirror annotationMirror) {
		NumericConstraint constraint = new NumericConstraint();
		Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues = ctx.getElementUtils().getElementValuesWithDefaults(annotationMirror);
		setConstraintDefaults(constraint, annotationValues);
		constraint.setValue(getValue(METHOD_VALUE, annotationValues).getValue().toString());
		AnnotationValue inclusive = getValue(METHOD_INCLUSIVE, annotationValues);
		constraint.setInclusive(inclusive == null ? null : (Boolean) inclusive.getValue());
		return constraint;
	}	
	
}
