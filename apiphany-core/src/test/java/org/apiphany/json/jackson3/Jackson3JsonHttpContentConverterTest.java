package org.apiphany.json.jackson3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apiphany.ApiMimeType;
import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.annotation.FieldName;
import org.junit.jupiter.api.Test;
import org.morphix.convert.ObjectConverterException;
import org.morphix.reflection.GenericClass;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * Test class for {@link Jackson3JsonHttpContentConverter}.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson3JsonHttpContentConverterTest extends Jackson3Test {

	private static final String JSON_STRING = "{\"name\":\"John\",\"age\":30}";

	static class Person {

		public String name;
		public int age;
	}

	@Test
	void shouldConvertJsonStringToObject() {
		var converter = new Jackson3JsonHttpContentConverter<Person>();

		Person result = converter.from(JSON_STRING, null, Person.class);

		assertThat(result.name, equalTo("John"));
		assertThat(result.age, equalTo(30));
	}

	@Test
	void shouldConvertJsonStringToGenericObject() {
		Object json = "[" + JSON_STRING + "]";
		var converter = new Jackson3JsonHttpContentConverter<List<Person>>();

		List<Person> result = converter.from(json, null, new GenericClass<>() {
			// empty
		});

		assertThat(result.getFirst().name, equalTo("John"));
		assertThat(result.getFirst().age, equalTo(30));
	}

	@Test
	void shouldReturnNullWhenJsonIsNull() {
		var converter = new Jackson3JsonHttpContentConverter<Person>();

		Person result = converter.from(null, null, Person.class);

		assertThat(result, nullValue());
	}

	@Test
	void shouldReturnNullWhenGenericJsonIsNull() {
		var converter = new Jackson3JsonHttpContentConverter<List<Person>>();

		List<Person> result = converter.from(null, null, new GenericClass<>() {
			// empty
		});

		assertThat(result, nullValue());
	}

	@Test
	void shouldThrowExceptionOnConvertWrongJsonStringToObject() {
		var converter = new Jackson3JsonHttpContentConverter<Person>();

		ObjectConverterException e = assertThrows(ObjectConverterException.class, () -> converter.from("x", null, Person.class));

		assertThat(e.getMessage(), equalTo("Error converting JSON response to " + Person.class.getName()));
	}

	@Test
	void shouldThrowExceptionOnConvertWrongConvertJsonStringToGenericObject() {
		var converter = new Jackson3JsonHttpContentConverter<List<Person>>();

		GenericClass<List<Person>> genericClass = new GenericClass<>() {
			// empty
		};
		ObjectConverterException e = assertThrows(ObjectConverterException.class, () -> converter.from("x", null, genericClass));

		assertThat(e.getMessage(), equalTo("Error converting JSON response to " + genericClass.getType().getTypeName()));
	}

	@Test
	void shouldReturnTrueIfObjectIsConvertible() {
		var converter = new Jackson3JsonHttpContentConverter<String>();
		ApiMimeType mimeType = ContentType.APPLICATION_JSON;

		boolean result = converter.isConvertible(mimeType, null, null);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseIfObjectIsNotConvertible() {
		var converter = new Jackson3JsonHttpContentConverter<String>();
		ApiMimeType mimeType = ContentType.TEXT_PLAIN;

		boolean result = converter.isConvertible(mimeType, null, null);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseIfMimeTypeIsNull() {
		var converter = new Jackson3JsonHttpContentConverter<String>();

		boolean result = converter.isConvertible(null, null, null);

		assertFalse(result);
	}

	@Test
	void shouldConvertObjectToJsonString() {
		var converter = new Jackson3JsonHttpContentConverter<Person>();

		String result = converter.toString(JSON_STRING, null);

		assertThat(result, equalTo(JsonBuilder.toJson(JSON_STRING)));
	}

	@Test
	void shouldThrowWhenTheGivenJsonMapperIsNull() {
		assertThrows(NullPointerException.class, () -> new Jackson3JsonHttpContentConverter<String>(null));
	}

	@Test
	void shouldApplyApiphanyAnnotationsWhenWritingWithTheGivenMapper() {
		JsonMapper jsonMapper = JsonMapper.builder()
				.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.build();
		var converter = new Jackson3JsonHttpContentConverter<String>(jsonMapper);

		FieldNamedBean bean = new FieldNamedBean();
		bean.setDisplayName("Ada");

		assertThat(converter.toString(bean, ContentType.APPLICATION_JSON), equalTo("{\"custom_name\":\"Ada\"}"));
	}

	@Test
	void shouldWriteWithTheGivenMapperWithoutConfiguringIt() {
		JsonMapper jsonMapper = JsonMapper.builder()
				.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.build();
		var converter = new Jackson3JsonHttpContentConverter<String>(jsonMapper);

		NamedPerson person = new NamedPerson();
		person.setFirstName("Ada");

		String result = converter.toString(person, ContentType.APPLICATION_JSON);

		assertThat(result, equalTo(jsonMapper.writeValueAsString(person)));
	}

	@Test
	void shouldReadAStringBodyWithTheGivenMapper() {
		JsonMapper jsonMapper = JsonMapper.builder().build();
		var converter = new Jackson3JsonHttpContentConverter<String>(jsonMapper);

		String json = jsonMapper.writeValueAsString("Ada");

		assertThat(converter.from(json, ContentType.APPLICATION_JSON, String.class), equalTo("Ada"));
	}

	@Test
	void shouldReadAByteArrayBodyWithTheGivenMapper() {
		JsonMapper jsonMapper = JsonMapper.builder().build();
		var converter = new Jackson3JsonHttpContentConverter<String>(jsonMapper);

		byte[] json = jsonMapper.writeValueAsBytes("Ada");

		assertThat(converter.from(json, ContentType.APPLICATION_JSON, String.class), equalTo("Ada"));
	}

	@Test
	void shouldReadAnInputStreamBodyWithTheGivenMapper() {
		JsonMapper jsonMapper = JsonMapper.builder().build();
		var converter = new Jackson3JsonHttpContentConverter<String>(jsonMapper);

		InputStream json = new ByteArrayInputStream(jsonMapper.writeValueAsBytes("Ada"));

		assertThat(converter.from(json, ContentType.APPLICATION_JSON, String.class), equalTo("Ada"));
	}

	@Test
	void shouldThrowWhenReadingAnUnsupportedBodyTypeWithTheGivenMapper() {
		var converter = new Jackson3JsonHttpContentConverter<String>(JsonMapper.builder().build());

		ObjectConverterException e = assertThrows(ObjectConverterException.class,
				() -> converter.from(42, ContentType.APPLICATION_JSON, String.class));
		assertThat(e.getCause(), instanceOf(UnsupportedOperationException.class));
	}

	@Test
	void shouldWrapAJsonProcessingExceptionWhenReadingWithTheGivenMapper() {
		var converter = new Jackson3JsonHttpContentConverter<String>(JsonMapper.builder().build());

		assertThrows(ObjectConverterException.class, () -> converter.from("{", ContentType.APPLICATION_JSON, String.class));
	}

	@Test
	void shouldNotReferenceJackson2BuilderAsAClassDependency() throws IOException {
		String classResource = Jackson3JsonHttpContentConverter.class.getName().replace('.', '/') + ".class";
		try (InputStream inputStream = Jackson3JsonHttpContentConverter.class.getClassLoader().getResourceAsStream(classResource)) {
			String classBytes = new String(inputStream.readAllBytes(), StandardCharsets.ISO_8859_1);

			assertThat(classBytes.contains("org/apiphany/json/jackson2/"), equalTo(false));
		}
	}

	/**
	 * Bean whose property name changes under snake case, used to prove the given mapper is used as-is.
	 */
	static class FieldNamedBean {

		@FieldName("custom_name")
		private String displayName;

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(final String displayName) {
			this.displayName = displayName;
		}
	}

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
