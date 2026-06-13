package org.apiphany.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.charset.StandardCharsets;

import org.apiphany.Api;
import org.apiphany.ApiClient;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.SpringRestExchangeClient;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * Integration test proving {@link SpringRestExchangeClient} preserves and uses customizations applied on
 * {@link RestClient.Builder} coming from a Spring Boot application context.
 *
 * @author Radu Sebastian LAZIN
 */
class SpringRestExchangeClientRestClientBuilderIT {

	private static final String BUILDER_HEADER = "X-RestClient-Builder";
	private static final String BUILDER_HEADER_VALUE = "configured-by-spring-boot";
	private static final String INTERCEPTOR_HEADER = "X-RestClient-Interceptor";
	private static final String INTERCEPTOR_HEADER_VALUE = "configured-by-interceptor";

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
	void shouldUseConfiguredRestClientBuilderDefaultHeaderWithApiClient() {
		wiremock.stubFor(get(urlEqualTo("/builder-api-client"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/octet-stream")
						.withBody("ok")));

		TestApiClient apiClient = CONTEXT.getBean(TestApiClient.class);

		String url = wiremock.baseUrl() + "/builder-api-client";

		String response = apiClient.getWithApiClient(url);

		assertThat(response, equalTo("ok"));
		wiremock.verify(getRequestedFor(urlEqualTo("/builder-api-client"))
				.withHeader(BUILDER_HEADER, WireMock.equalTo(BUILDER_HEADER_VALUE))
				.withHeader(INTERCEPTOR_HEADER, WireMock.equalTo(INTERCEPTOR_HEADER_VALUE)));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldUseConfiguredRestClientBuilderDefaultHeaderWithSimpleGetRequest() {
		wiremock.stubFor(get(urlEqualTo("/builder-exchange-client"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/octet-stream")
						.withBody("ok")));

		SpringRestExchangeClient exchangeClient = CONTEXT.getBean(SpringRestExchangeClient.class);
		String url = wiremock.baseUrl() + "/builder-exchange-client";
		SimpleGetRequest request = new SimpleGetRequest(url);

		ApiResponse<byte[]> response = exchangeClient.exchange(request);

		assertThat(response.getStatus(), equalTo(HttpStatus.OK));
		assertThat(new String(response.getBody(), StandardCharsets.UTF_8), equalTo("ok"));
		wiremock.verify(getRequestedFor(urlEqualTo("/builder-exchange-client"))
				.withHeader(BUILDER_HEADER, WireMock.equalTo(BUILDER_HEADER_VALUE))
				.withHeader(INTERCEPTOR_HEADER, WireMock.equalTo(INTERCEPTOR_HEADER_VALUE)));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldUseConfiguredRestClientBuilderDefaultHeaderWithApiClass() {
		wiremock.stubFor(get(urlEqualTo("/builder-api"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/octet-stream")
						.withBody("ok")));

		SpringRestExchangeClient exchangeClient = CONTEXT.getBean(SpringRestExchangeClient.class);

		String url = wiremock.baseUrl() + "/builder-api";

		ApiResponse<byte[]> response = Api
				.http(exchangeClient)
				.get()
				.url(url)
				.retrieve(byte[].class);

		assertThat(response.getStatus(), equalTo(HttpStatus.OK));
		assertThat(new String(response.getBody(), StandardCharsets.UTF_8), equalTo("ok"));
		wiremock.verify(getRequestedFor(urlEqualTo("/builder-api"))
				.withHeader(BUILDER_HEADER, WireMock.equalTo(BUILDER_HEADER_VALUE))
				.withHeader(INTERCEPTOR_HEADER, WireMock.equalTo(INTERCEPTOR_HEADER_VALUE)));
	}

	public static class SimpleGetRequest extends ApiRequest<Void> {

		SimpleGetRequest(final String url) {
			this.method = HttpMethod.GET;
			this.url = url;
			this.classResponseType = byte[].class;
		}
	}

	static class TestApiClient extends ApiClient {

		TestApiClient(final SpringRestExchangeClient springRestExchangeClient) {
			super(springRestExchangeClient);
		}

		public String getWithApiClient(final String url) {
			return client()
					.http()
					.get()
					.url(url)
					.retrieve(String.class)
					.orNull();
		}
	}

	@SpringBootConfiguration
	static class TestApplication {

		@Bean
		ClientProperties clientProperties() {
			return ClientProperties.defaults();
		}

		@Bean
		RestClient.Builder restClientBuilder() {
			return RestClient.builder()
					.defaultHeader(BUILDER_HEADER, BUILDER_HEADER_VALUE)
					.requestInterceptor((request, body, execution) -> {
						request.getHeaders().add(INTERCEPTOR_HEADER, INTERCEPTOR_HEADER_VALUE);
						return execution.execute(request, body);
					});
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
