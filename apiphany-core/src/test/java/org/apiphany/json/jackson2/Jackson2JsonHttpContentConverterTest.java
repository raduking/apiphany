package org.apiphany.json.jackson2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apiphany.ApiMimeType;
import org.apiphany.io.ContentType;
import org.junit.jupiter.api.Test;
import org.morphix.convert.ObjectConverterException;
import org.morphix.reflection.GenericClass;

/**
 * Test class for {@link Jackson2JsonHttpContentConverter}.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson2JsonHttpContentConverterTest {

	private static final String JSON_STRING = "{\"name\":\"John\",\"age\":30}";

	static class Person {

		public String name;
		public int age;
	}

	@Test
	void shouldConvertJsonStringToObject() {
		var converter = new Jackson2JsonHttpContentConverter<Person>();

		Person result = converter.from(JSON_STRING, null, Person.class);

		assertThat(result.name, equalTo("John"));
		assertThat(result.age, equalTo(30));
	}

	@Test
	void shouldConvertJsonStringToGenericObject() {
		Object json = "[" + JSON_STRING + "]";
		var converter = new Jackson2JsonHttpContentConverter<List<Person>>();

		List<Person> result = converter.from(json, null, new GenericClass<List<Person>>() {
			// empty
		});

		assertThat(result.getFirst().name, equalTo("John"));
		assertThat(result.getFirst().age, equalTo(30));
	}

	@Test
	void shouldThrowExceptionOnConvertWrongJsonStringToObject() {
		var converter = new Jackson2JsonHttpContentConverter<Person>();

		ObjectConverterException e = assertThrows(ObjectConverterException.class, () -> converter.from("x", null, Person.class));

		assertThat(e.getMessage(), equalTo("Error converting JSON response to " + Person.class.getName()));
	}

	@Test
	void shouldThrowExceptionOnConvertWrongConvertJsonStringToGenericObject() {
		var converter = new Jackson2JsonHttpContentConverter<List<Person>>();

		GenericClass<List<Person>> genericClass = new GenericClass<>() {
			// empty
		};
		ObjectConverterException e = assertThrows(ObjectConverterException.class, () -> converter.from("x", null, genericClass));

		assertThat(e.getMessage(), equalTo("Error converting JSON response to " + genericClass.getType().getTypeName()));
	}

	@Test
	void shouldReturnTrueIfObjectIsConvertible() {
		var converter = new Jackson2JsonHttpContentConverter<String>();
		ApiMimeType mimeType = ContentType.APPLICATION_JSON;

		boolean result = converter.isConvertible(mimeType, null, null);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseIfObjectIsNotConvertible() {
		var converter = new Jackson2JsonHttpContentConverter<String>();
		ApiMimeType mimeType = ContentType.TEXT_PLAIN;

		boolean result = converter.isConvertible(mimeType, null, null);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseIfMimeTypeIsNull() {
		var converter = new Jackson2JsonHttpContentConverter<String>();

		boolean result = converter.isConvertible(null, null, null);

		assertFalse(result);
	}
}
