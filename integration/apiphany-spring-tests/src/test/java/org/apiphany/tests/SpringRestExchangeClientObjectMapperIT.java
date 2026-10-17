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
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * Integration test proving {@link SpringRestExchangeClient} writes a JSON request body with the {@link ObjectMapper}
 * bean configured in a Spring Boot application.
 *
 * @author Radu Sebastian LAZIN
 */
class SpringRestExchangeClientObjectMapperIT {

	private static final String PATH = "/person";

	@RegisterExtension
	static final WireMockExtension wiremock = WireMockExtension.newInstance()
			.options(options().dynamicPort())
			.build();

	private static final ConfigurableApplicationContext CONTEXT = new SpringApplicationBuilder(TestApplication.class)
			.web(WebApplicationType.NONE)
			.run();

	@AfterAll
	static void closeContext() {
		CONTEXT.close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldWriteJsonRequestBodyWithTheObjectMapperBean() throws Exception {
		wiremock.stubFor(post(urlEqualTo(PATH))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/plain")
						.withBody("ok")));

		NamedPerson person = new NamedPerson();
		person.setFirstName("Ada");
		ObjectMapper objectMapper = CONTEXT.getBean(ObjectMapper.class);
		String json = objectMapper.writeValueAsString(person);
		assertThat(json, equalTo("{\"first_name\":\"Ada\"}"));

		TestApiClient apiClient = CONTEXT.getBean(TestApiClient.class);
		String response = apiClient.postPerson(wiremock.baseUrl() + PATH, person);

		assertThat(response, equalTo("ok"));
		wiremock.verify(postRequestedFor(urlEqualTo(PATH))
				.withHeader("Content-Type", WireMock.equalTo("application/json"))
				.withRequestBody(WireMock.equalTo(json)));
	}

	/**
	 * Bean whose property name changes under the application {@link ObjectMapper}.
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
		ObjectMapper objectMapper() {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
			return objectMapper;
		}

		@Bean
		ClientProperties clientProperties() {
			return ClientProperties.defaults();
		}

		@Bean
		SpringRestExchangeClient springRestExchangeClient(final ClientProperties clientProperties, final RestClient.Builder restClientBuilder) {
			return new SpringRestExchangeClient(clientProperties, restClientBuilder);
		}

		@Bean
		TestApiClient testApiClient(final SpringRestExchangeClient springRestExchangeClient) {
			return new TestApiClient(springRestExchangeClient);
		}
	}
}
