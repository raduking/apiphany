package org.apiphany.json.jackson2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apiphany.ApiMimeType;
import org.apiphany.io.ContentType;
import org.junit.jupiter.api.Test;
import org.morphix.convert.ObjectConverterException;
import org.morphix.reflection.GenericClass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

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

		List<Person> result = converter.from(json, null, new GenericClass<>() {
			// empty
		});

		assertThat(result.getFirst().name, equalTo("John"));
		assertThat(result.getFirst().age, equalTo(30));
	}

	@Test
	void shouldReturnNullWhenJsonIsNull() {
		var converter = new Jackson2JsonHttpContentConverter<Person>();

		Person result = converter.from(null, null, Person.class);

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullWhenGenericJsonIsNull() {
		var converter = new Jackson2JsonHttpContentConverter<List<Person>>();

		List<Person> result = converter.from(null, null, new GenericClass<>() {
			// empty
		});

		assertThat(result, nullValue());
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

	@Test
	void shouldConvertObjectToJsonString() {
		var converter = new Jackson2JsonHttpContentConverter<Person>();

		String result = converter.toString(JSON_STRING, null);

		assertThat(result, equalTo(Jackson2JsonBuilder.toJson(JSON_STRING)));
	}

	@Test
	void shouldThrowWhenTheGivenObjectMapperIsNull() {
		assertThrows(NullPointerException.class, () -> new Jackson2JsonHttpContentConverter<String>(null));
	}

	@Test
	void shouldWriteWithTheGivenMapperWithoutConfiguringIt() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		var converter = new Jackson2JsonHttpContentConverter<String>(objectMapper);

		NamedPerson person = new NamedPerson();
		person.setFirstName("Ada");

		String result = converter.toString(person, ContentType.APPLICATION_JSON);

		assertThat(result, equalTo(objectMapper.writeValueAsString(person)));
	}

	@Test
	void shouldReadAStringBodyWithTheGivenMapper() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		var converter = new Jackson2JsonHttpContentConverter<String>(objectMapper);

		String json = objectMapper.writeValueAsString("Ada");

		assertThat(converter.from(json, ContentType.APPLICATION_JSON, String.class), equalTo("Ada"));
	}

	@Test
	void shouldReadAByteArrayBodyWithTheGivenMapper() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		var converter = new Jackson2JsonHttpContentConverter<String>(objectMapper);

		byte[] json = objectMapper.writeValueAsBytes("Ada");

		assertThat(converter.from(json, ContentType.APPLICATION_JSON, String.class), equalTo("Ada"));
	}

	@Test
	void shouldReadAnInputStreamBodyWithTheGivenMapper() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		var converter = new Jackson2JsonHttpContentConverter<String>(objectMapper);

		InputStream json = new ByteArrayInputStream(objectMapper.writeValueAsBytes("Ada"));

		assertThat(converter.from(json, ContentType.APPLICATION_JSON, String.class), equalTo("Ada"));
	}

	@Test
	void shouldThrowWhenReadingAnUnsupportedBodyTypeWithTheGivenMapper() {
		var converter = new Jackson2JsonHttpContentConverter<String>(new ObjectMapper());

		assertThrows(UnsupportedOperationException.class, () -> converter.from(42, ContentType.APPLICATION_JSON, String.class));
	}

	@Test
	void shouldWrapAJsonProcessingExceptionWhenReadingWithTheGivenMapper() {
		var converter = new Jackson2JsonHttpContentConverter<String>(new ObjectMapper());

		assertThrows(ObjectConverterException.class, () -> converter.from("{", ContentType.APPLICATION_JSON, String.class));
	}

	/**
	 * Bean whose property name changes under snake case, used to prove the given mapper is used as-is.
	 */
	static class NamedPerson {

		private String firstName;

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(final String firstName) {
			this.firstName = firstName;
		}
	}
}
