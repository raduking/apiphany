package org.apiphany.http;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.apiphany.io.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * Tests for {@link SpringJson}.
 *
 * @author Radu Sebastian LAZIN
 */
class SpringJsonTest {

	@Test
	void shouldReturnNullWhenRestClientBuilderHasNoMessageConverters() {
		assertThat(SpringJson.converter(RestClient.builder()), nullValue());
	}

	@Test
	void shouldReturnNullWhenRestTemplateBuilderHasNoMessageConverters() {
		assertThat(SpringJson.converter(new RestTemplateBuilder()), nullValue());
	}

	@Test
	void shouldReturnNullWhenTheBuilderIsNull() {
		assertThat(SpringJson.converter((RestClient.Builder) null), nullValue());
		assertThat(SpringJson.converter((RestTemplateBuilder) null), nullValue());
	}

	@Test
	void shouldReturnNullWhenTheConvertersAreNotJackson() {
		assertThat(SpringJson.converter(List.of(new StringHttpMessageConverter())), nullValue());
	}

	@Test
	void shouldReturnNullWhenNoConverterWritesJson() {
		MappingJackson2HttpMessageConverter text = new MappingJackson2HttpMessageConverter(new ObjectMapper());
		text.setSupportedMediaTypes(List.of(MediaType.TEXT_PLAIN));

		assertThat(SpringJson.converter(List.of(new StringHttpMessageConverter(), text)), nullValue());
	}

	@Test
	void shouldUseTheObjectMapperOfTheFirstJsonConverter() throws Exception {
		ObjectMapper textMapper = new ObjectMapper();
		MappingJackson2HttpMessageConverter text = new MappingJackson2HttpMessageConverter(textMapper);
		text.setSupportedMediaTypes(List.of(MediaType.TEXT_PLAIN));

		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		MappingJackson2HttpMessageConverter json = new MappingJackson2HttpMessageConverter(jsonMapper);

		NamedPerson person = new NamedPerson();
		person.setFirstName("Ada");

		assertThat(SpringJson.converter(List.of(text, json)).to(person, ContentType.APPLICATION_JSON),
				equalTo(jsonMapper.writeValueAsString(person)));
	}

	@Test
	void shouldReadTheObjectMapperFromARestClientBuilder() throws Exception {
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		RestClient.Builder builder = RestClient.builder()
				.messageConverters(List.of(new MappingJackson2HttpMessageConverter(jsonMapper)));

		NamedPerson person = new NamedPerson();
		person.setFirstName("Ada");

		assertThat(SpringJson.converter(builder).to(person, ContentType.APPLICATION_JSON),
				equalTo(jsonMapper.writeValueAsString(person)));
	}

	@Test
	void shouldReadTheObjectMapperFromARestTemplateBuilder() throws Exception {
		ObjectMapper jsonMapper = new ObjectMapper();
		jsonMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		RestTemplateBuilder builder = new RestTemplateBuilder().messageConverters(new MappingJackson2HttpMessageConverter(jsonMapper));

		NamedPerson person = new NamedPerson();
		person.setFirstName("Ada");

		assertThat(SpringJson.converter(builder).to(person, ContentType.APPLICATION_JSON),
				equalTo(jsonMapper.writeValueAsString(person)));
	}

	@Test
	void shouldPreventInstantiation() {
		assertDefaultConstructorThrows(SpringJson.class);
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
