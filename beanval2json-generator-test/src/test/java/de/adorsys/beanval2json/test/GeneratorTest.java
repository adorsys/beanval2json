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
package de.adorsys.beanval2json.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;

import javax.validation.constraints.Pattern.Flag;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.adorsys.beanval2json.test.model.Groups;
import de.adorsys.beanval2json.test.model.Income;
import de.adorsys.beanval2json.test.model.Person;
import de.adorsys.beanval2json.test.model.Severity;

/**
 * @author Florian Hirsch
 */
public class GeneratorTest {

	private static final String CONSTRAINTS_FILENAME = "target/beanval2json/js/constraints.json";

	private static JsonObject constraints;
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		File constraintFile = new File(System.getProperty("user.dir"), CONSTRAINTS_FILENAME);
		InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(constraintFile), "UTF-8");
		constraints = new Gson().fromJson(inputStreamReader, JsonObject.class);
		inputStreamReader.close();
	}
	
	@Test
	public void testConstraints() {
		// Person.id
		JsonObject notNull = getJsonObject(Person.class, "id", "notNull");
		JsonArray groups = notNull.get("groups").getAsJsonArray();
		assertNotNull(groups);
		checkArrayContains(groups, Groups.User.class.getCanonicalName());
		checkArrayContains(groups, Groups.Admin.class.getCanonicalName());
		
		// Person.firstname
		JsonObject size = getJsonObject(Person.class, "firstname", "size");
		assertEquals(2, size.get("min").getAsInt());
		assertEquals(32, size.get("max").getAsInt());
		checkArrayContains(size.get("payload").getAsJsonArray(), Severity.Error.class.getCanonicalName());
		
		// Person.lastname
		JsonObject pattern = getJsonObject(Person.class, "lastname", "pattern");
		assertEquals("[a-zA-Z]+", pattern.get("regexp").getAsString());
		checkArrayContains(pattern.get("flags").getAsJsonArray(), Flag.CASE_INSENSITIVE.name());
		
		// Person.birthdate
		JsonObject past = getJsonObject(Person.class, "birthdate", "past");
		assertNotNull(past);
		
		// Person.somedate
		JsonObject future = getJsonObject(Person.class, "somedate", "future");
		assertNotNull(future);
		
		// Person.somethingTrue
		JsonObject assertTrue = getJsonObject(Person.class, "somethingTrue", "assertTrue");
		assertNotNull(assertTrue);
		
		// Person.somethingFalse
		JsonObject assertFalse = getJsonObject(Person.class, "somethingFalse", "assertFalse");
		assertNotNull(assertFalse);

		// Person.shouldBeNull
		JsonObject nullConstraint = getJsonObject(Person.class, "shouldBeNull", "null");
		assertNotNull(nullConstraint);
		
		// Person.income
		JsonElement income = constraints.get(String.format("%s.%s", Person.class.getName(), "income"));
		assertNull(income);
		
		// Income.salary
		JsonObject decimalMin = getJsonObject(Income.class, "salary", "decimalMin");
		assertEquals("500,00", decimalMin.get("value").getAsString());
		assertTrue(decimalMin.get("inclusive").getAsBoolean());
		
		JsonObject decimalMax = getJsonObject(Income.class, "salary", "decimalMax");
		assertEquals("5000,00", decimalMax.get("value").getAsString());
		assertFalse(decimalMax.get("inclusive").getAsBoolean());
		
		JsonObject digits = getJsonObject(Income.class, "salary", "digits");
		assertEquals(4, digits.get("integer").getAsInt());
		assertEquals(2, digits.get("fraction").getAsInt());
		
		// Income.bonus
		JsonObject min = getJsonObject(Income.class, "bonus", "min");
		assertEquals(50, min.get("value").getAsInt());
		
		JsonObject max = getJsonObject(Income.class, "bonus", "max");
		assertEquals(500, max.get("value").getAsInt());
	}

	@Test
	public void testMapping() {
		assertNull(constraints.get(String.format("%s.%s", Person.class.getName(), "eMail")));
		assertNotNull(constraints.get("eMail").getAsJsonObject().get("notNull"));
	}
	
	@Test
	public void testIgnore() {
		for (Entry<String, JsonElement> entry : constraints.entrySet()) {
			if (entry.getKey().startsWith("de.adorsys.beanval2json.test.ignored.model")) {
				fail("de.adorsys.beanval2json.test.ignored.model.* should be ignored!");
			}
			if (entry.getKey().equals("de.adorsys.beanval2json.test.model.Person.ignoredProperty")) {
				fail("de.adorsys.beanval2json.test.model.Person.ignoredProperty should be ignored!");
			}			
		}
	}	
	
	@Test
	public void testMessages() {
		checkMessage(Person.class, "birthdate", "past", "The date should be in the past");
		checkMessage(Person.class, "somedate", "future", "The date should be in the future");
		checkMessage(Person.class, "somethingTrue", "assertTrue", "This value should be true");
		checkMessage(Person.class, "somethingFalse", "assertFalse", null);	
	}
	
	private JsonObject getJsonObject(Class<?> clazz, String property, String type) {
		JsonElement jsonElement = constraints.get(String.format("%s.%s", clazz.getName(), property));
		return jsonElement.getAsJsonObject().get(type).getAsJsonObject();
	}
	
	private void checkMessage(Class<?> clazz, String property, String type, String expectedMessage) {
		JsonElement message = getJsonObject(clazz, property, type).getAsJsonObject().get("message");
		assertEquals(expectedMessage, message != null ? message.getAsString() : null);
	}
	
	private void checkArrayContains(JsonArray array, String content) {
		for (JsonElement jsonElement : array) {
			if (jsonElement.getAsString().equals(content)) {
				return;
			}			
		}
		fail(String.format("Content '%s' not found in Array '%s'", content, array.toString()));
	}
	
}
