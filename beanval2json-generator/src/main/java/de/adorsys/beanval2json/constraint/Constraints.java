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
package de.adorsys.beanval2json.constraint;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.gson.annotations.SerializedName;

/**
 * All constraints for one property
 * @author Florian Hirsch
 */
public class Constraints implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Constraint assertFalse;
	
	private Constraint assertTrue;
	
	private NumericConstraint decimalMin;
	
	private NumericConstraint decimalMax;
	
	private DigitsConstraint digits;
	
	private Constraint future;
	
	private NumericConstraint min;
	
	private NumericConstraint max;
	
	private Constraint notNull;
	
	@SerializedName("null")
	private Constraint nullConstraint;
	
	private Constraint past;

	private PatternConstraint pattern;

	private SizeConstraint size;

	public Constraint getAssertFalse() {
		return assertFalse;
	}

	public void setAssertFalse(Constraint assertFalse) {
		this.assertFalse = assertFalse;
	}

	public Constraint getAssertTrue() {
		return assertTrue;
	}

	public void setAssertTrue(Constraint assertTrue) {
		this.assertTrue = assertTrue;
	}

	public NumericConstraint getDecimalMin() {
		return decimalMin;
	}

	public void setDecimalMin(NumericConstraint decimalMin) {
		this.decimalMin = decimalMin;
	}

	public NumericConstraint getDecimalMax() {
		return decimalMax;
	}

	public void setDecimalMax(NumericConstraint decimalMax) {
		this.decimalMax = decimalMax;
	}

	public DigitsConstraint getDigits() {
		return digits;
	}

	public void setDigits(DigitsConstraint digits) {
		this.digits = digits;
	}

	public Constraint getFuture() {
		return future;
	}

	public void setFuture(Constraint future) {
		this.future = future;
	}

	public NumericConstraint getMin() {
		return min;
	}

	public void setMin(NumericConstraint min) {
		this.min = min;
	}

	public NumericConstraint getMax() {
		return max;
	}

	public void setMax(NumericConstraint max) {
		this.max = max;
	}

	public Constraint getNotNull() {
		return notNull;
	}

	public void setNotNull(Constraint notNull) {
		this.notNull = notNull;
	}

	public Constraint getNull() {
		return nullConstraint;
	}

	public void setNull(Constraint nullConstraint) {
		this.nullConstraint = nullConstraint;
	}

	public Constraint getPast() {
		return past;
	}

	public void setPast(Constraint past) {
		this.past = past;
	}
	
	public PatternConstraint getPattern() {
		return pattern;
	}

	public void setPattern(PatternConstraint pattern) {
		this.pattern = pattern;
	}

	public SizeConstraint getSize() {
		return size;
	}

	public void setSize(SizeConstraint size) {
		this.size = size;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
