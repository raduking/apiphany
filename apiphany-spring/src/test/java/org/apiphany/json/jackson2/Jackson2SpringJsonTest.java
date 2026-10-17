package org.apiphany.json.jackson2;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.client.ContentConverter;
import org.apiphany.io.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * Tests for {@link Jackson2SpringJson}.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson2SpringJsonTest {

	@Test
	void shouldReturnNullWhenTheConverterIsNull() {
		assertThat(Jackson2SpringJson.converter(null), nullValue());
	}

	@Test
	void shouldReturnNullWhenTheConverterIsNotJackson2() {
		assertThat(Jackson2SpringJson.converter(new StringHttpMessageConverter()), nullValue());
	}

	@Test
	void shouldResolveAConverterThatWritesWithTheMapper() throws Exception {
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		MappingJackson2HttpMessageConverter jackson2Converter = new MappingJackson2HttpMessageConverter(jsonMapper);

		NamedPerson person = new NamedPerson();
		person.setFirstName("Ada");

		ContentConverter<String> converter = Jackson2SpringJson.converter(jackson2Converter);

		assertThat(converter.toString(person, ContentType.APPLICATION_JSON), equalTo(jsonMapper.writeValueAsString(person)));
	}

	@Test
	void shouldConvertOnlyApplicationJson() {
		ContentConverter<String> converter = Jackson2SpringJson.converter(new MappingJackson2HttpMessageConverter());

		assertThat(converter.isConvertible(ContentType.APPLICATION_JSON, null, null), equalTo(true));
		assertThat(converter.isConvertible(ContentType.TEXT_PLAIN, null, null), equalTo(false));
		assertThat(converter.isConvertible(null, null, null), equalTo(false));
	}

	@Test
	void shouldThrowOnReadingAResponse() {
		ContentConverter<String> converter = Jackson2SpringJson.converter(new MappingJackson2HttpMessageConverter());

		assertThat(converter, notNullValue());
		assertThrows(UnsupportedOperationException.class, () -> converter.from("{}", ContentType.APPLICATION_JSON, String.class));
	}

	@Test
	void shouldPreventInstantiation() {
		assertDefaultConstructorThrows(Jackson2SpringJson.class);
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
