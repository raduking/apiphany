package org.apiphany.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.SpringRestExchangeClient;
import org.apiphany.http.HttpHeader;
import org.apiphany.io.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * Integration test proving {@link SpringRestExchangeClient} writes a JSON request body with the {@link JsonMapper} bean
 * configured in a Spring Boot application.
 *
 * @author Radu Sebastian LAZIN
 */
class SpringRestExchangeClientJsonMapperIT {

	private static final String PATH = "/person";

	@RegisterExtension
	static final WireMockExtension wiremock = WireMockExtension.newInstance()
			.options(options().dynamicPort())
			.build();

	private static final ConfigurableApplicationContext CONTEXT = new SpringApplicationBuilder(TestApplication.class)
			.web(WebApplicationType.NONE)
			.run();

	private static final JsonMapper JSON_MAPPER = CONTEXT.getBean(JsonMapper.class);

	@AfterAll
	static void closeContext() {
		CONTEXT.close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldWriteJsonRequestBodyWithTheJsonMapperBean() throws Exception {
		wiremock.stubFor(post(urlEqualTo(PATH))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/plain")
						.withBody("ok")));

		NamedPerson person = new NamedPerson();
		person.setFirstName("Ada");
		String json = JSON_MAPPER.writeValueAsString(person);
		assertThat(json, equalTo("{\"first_name\":\"Ada\"}"));

		TestApiClient apiClient = CONTEXT.getBean(TestApiClient.class);
		String response = apiClient.postPerson(wiremock.baseUrl() + PATH, person);

		assertThat(response, equalTo("ok"));
		wiremock.verify(postRequestedFor(urlEqualTo(PATH))
				.withHeader("Content-Type", WireMock.equalTo("application/json"))
				.withRequestBody(WireMock.equalTo(json)));
	}

	/**
	 * Bean whose property name changes under the application {@link JsonMapper}.
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

	static class TestApiClient extends ApiClient {

		TestApiClient(final SpringRestExchangeClient springRestExchangeClient) {
			super(springRestExchangeClient);
		}

		String postPerson(final String url, final NamedPerson person) {
			return client()
					.http()
					.post()
					.url(url)
					.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON)
					.body(person)
					.retrieve(String.class)
					.orRethrow();
		}
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	static class TestApplication {

		@Bean
		JsonMapper jsonMapper() {
			return JsonMapper.builder()
					.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
					.build();
		}

		@Bean
		ClientProperties clientProperties() {
			return ClientProperties.defaults();
		}

		@Bean
		RestClient.Builder restClientBuilder(final JsonMapper jsonMapper) {
			return RestClient.builder().configureMessageConverters(messageConverters -> messageConverters.disableDefaults()
					.withJsonConverter(new JacksonJsonHttpMessageConverter(jsonMapper)));
		}

		@Bean
		SpringRestExchangeClient springRestExchangeClient(final ClientProperties clientProperties,
				final RestClient.Builder restClientBuilder) {
			return new SpringRestExchangeClient(clientProperties, restClientBuilder);
		}

		@Bean
		TestApiClient testApiClient(final SpringRestExchangeClient springRestExchangeClient) {
			return new TestApiClient(springRestExchangeClient);
		}
	}
}
