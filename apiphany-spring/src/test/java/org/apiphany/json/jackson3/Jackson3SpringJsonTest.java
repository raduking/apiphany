package org.apiphany.json.jackson3;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apiphany.client.ContentConverter;
import org.apiphany.io.ContentType;
import org.junit.jupiter.api.Test;
import org.morphix.convert.ObjectConverterException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tests for {@link Jackson3SpringJson}.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson3SpringJsonTest {

	@Test
	void shouldReturnNullWhenTheConverterIsNull() {
		assertThat(Jackson3SpringJson.converter(null), nullValue());
	}

	@Test
	void shouldReturnNullWhenTheConverterIsNotJackson3() {
		assertThat(Jackson3SpringJson.converter(new StringHttpMessageConverter()), nullValue());
	}

	@Test
	void shouldResolveAConverterThatWritesWithTheMapper() throws Exception {
		JsonMapper jsonMapper = JsonMapper.builder()
				.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
				.build();
		JacksonJsonHttpMessageConverter jackson3Converter = new JacksonJsonHttpMessageConverter(jsonMapper);

		NamedPerson person = new NamedPerson();
		person.setFirstName("Ada");

		ContentConverter<String> converter = Jackson3SpringJson.converter(jackson3Converter);

		assertThat(converter.toString(person, ContentType.APPLICATION_JSON),
				equalTo(jsonMapper.writeValueAsString(person)));
	}

	@Test
	void shouldConvertOnlyApplicationJson() {
		ContentConverter<String> converter = Jackson3SpringJson.converter(new JacksonJsonHttpMessageConverter());

		assertThat(converter.isConvertible(ContentType.APPLICATION_JSON, null, null), equalTo(true));
		assertThat(converter.isConvertible(ContentType.TEXT_PLAIN, null, null), equalTo(false));
		assertThat(converter.isConvertible(null, null, null), equalTo(false));
	}

	@Test
	void shouldReadAStringBodyWithTheMapper() throws Exception {
		JsonMapper jsonMapper = JsonMapper.builder().build();
		JacksonJsonHttpMessageConverter jackson3Converter = new JacksonJsonHttpMessageConverter(jsonMapper);
		ContentConverter<String> converter = Jackson3SpringJson.converter(jackson3Converter);

		String json = jsonMapper.writeValueAsString("Ada");

		assertThat(converter.from(json, ContentType.APPLICATION_JSON, String.class), equalTo("Ada"));
	}

	@Test
	void shouldReadAByteArrayBodyWithTheMapper() throws Exception {
		JsonMapper jsonMapper = JsonMapper.builder().build();
		JacksonJsonHttpMessageConverter jackson3Converter = new JacksonJsonHttpMessageConverter(jsonMapper);
		ContentConverter<String> converter = Jackson3SpringJson.converter(jackson3Converter);

		byte[] json = jsonMapper.writeValueAsBytes("Ada");

		assertThat(converter.from(json, ContentType.APPLICATION_JSON, String.class), equalTo("Ada"));
	}

	@Test
	void shouldReadAnInputStreamBodyWithTheMapper() throws Exception {
		JsonMapper jsonMapper = JsonMapper.builder().build();
		JacksonJsonHttpMessageConverter jackson3Converter = new JacksonJsonHttpMessageConverter(jsonMapper);
		ContentConverter<String> converter = Jackson3SpringJson.converter(jackson3Converter);

		InputStream json = new ByteArrayInputStream(jsonMapper.writeValueAsBytes("Ada"));

		assertThat(converter.from(json, ContentType.APPLICATION_JSON, String.class), equalTo("Ada"));
	}

	@Test
	void shouldThrowWhenReadingAnUnsupportedBodyType() {
		ContentConverter<String> converter = Jackson3SpringJson.converter(new JacksonJsonHttpMessageConverter());

		ObjectConverterException e = assertThrows(ObjectConverterException.class,
				() -> converter.from(42, ContentType.APPLICATION_JSON, String.class));
		assertThat(e.getCause(), instanceOf(UnsupportedOperationException.class));
	}

	@Test
	void shouldWrapAJsonProcessingExceptionWhenReading() {
		ContentConverter<String> converter = Jackson3SpringJson.converter(new JacksonJsonHttpMessageConverter());

		assertThrows(ObjectConverterException.class, () -> converter.from("{", ContentType.APPLICATION_JSON, String.class));
	}

	@Test
	void shouldPreventInstantiation() {
		assertDefaultConstructorThrows(Jackson3SpringJson.class);
	}

	/**
	 * Bean whose property name changes under snake case.
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
