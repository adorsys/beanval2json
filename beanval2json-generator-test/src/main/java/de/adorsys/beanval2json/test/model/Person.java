package de.adorsys.beanval2json.test.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Person implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull(groups = { Groups.User.class, Groups.Admin.class })
	private Integer id;
	
	@Size(min = 2, max = 32, payload = { Severity.Error.class })
	private String firstname;
	
	@Pattern(regexp = "[a-zA-Z]+", flags = { Flag.CASE_INSENSITIVE }, message = "Letters only")
	private String lastname;
	
	@NotNull(message = "Please enter your eMail-Address")
	private String eMail;
	
	@Past
	private Date birthdate;
	
	@Future(message = "{de.adorsys.validation.constraints.Future.message}")
	private Date somedate;

	@AssertTrue(message = "This value should be true")
	private Boolean somethingTrue;
	
	@AssertFalse
	private Boolean somethingFalse;
	
	@NotNull
	private String ignoredProperty;
	
	@Null
	private String shouldBeNull;
	
	private Income income;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String geteMail() {
		return eMail;
	}

	public void seteMail(String eMail) {
		this.eMail = eMail;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public Date getSomedate() {
		return somedate;
	}

	public void setSomedate(Date somedate) {
		this.somedate = somedate;
	}

	public Boolean getSomethingTrue() {
		return somethingTrue;
	}

	public void setSomethingTrue(Boolean somethingTrue) {
		this.somethingTrue = somethingTrue;
	}

	public Boolean getSomethingFalse() {
		return somethingFalse;
	}

	public void setSomethingFalse(Boolean somethingFalse) {
		this.somethingFalse = somethingFalse;
	}

	public String getIgnoredProperty() {
		return ignoredProperty;
	}

	public void setIgnoredProperty(String ignoredProperty) {
		this.ignoredProperty = ignoredProperty;
	}

	public String getShouldBeNull() {
		return shouldBeNull;
	}

	public void setShouldBeNull(String shouldBeNull) {
		this.shouldBeNull = shouldBeNull;
	}

	public Income getIncome() {
		return income;
	}

	public void setIncome(Income income) {
		this.income = income;
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
