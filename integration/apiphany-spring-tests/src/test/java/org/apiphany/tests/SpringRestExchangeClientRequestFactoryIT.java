package org.apiphany.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.SpringRestExchangeClient;
import org.apiphany.http.HttpException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.morphix.reflection.Fields;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * Integration tests for keeping a caller-configured request factory on {@link SpringRestExchangeClient}, including the
 * case where a logging interceptor consumes the response body.
 *
 * @author Radu Sebastian LAZIN
 */
class SpringRestExchangeClientRequestFactoryIT {

	private static final String PATH = "/buffered-body";
	private static final String BODY = "{\"property_id\":\"1234\",\"available\":true}";

	@RegisterExtension
	static final WireMockExtension wiremock = WireMockExtension.newInstance()
			.options(options().dynamicPort())
			.build();

	@Test
	void shouldReadResponseBodyAfterInterceptorConsumedItWithRestClient() throws Exception {
		stubJsonResponse();
		BodyLoggingInterceptor interceptor = new BodyLoggingInterceptor();
		RestClient restClient = RestClient.builder()
				.requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
				.requestInterceptor(interceptor)
				.build();

		try (SpringRestExchangeClient exchangeClient = new SpringRestExchangeClient(ClientProperties.defaults(), restClient);
				TestApiClient apiClient = new TestApiClient(exchangeClient)) {
			String response = apiClient.get(wiremock.baseUrl() + PATH);

			assertThat(response, equalTo(BODY));
		}
		assertThat(interceptor.getLoggedBodies(), equalTo(List.of(BODY)));
	}

	@Test
	void shouldReadResponseBodyAfterInterceptorConsumedItWithRestClientBuilder() throws Exception {
		stubJsonResponse();
		BodyLoggingInterceptor interceptor = new BodyLoggingInterceptor();
		RestClient.Builder restClientBuilder = RestClient.builder()
				.requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
				.requestInterceptor(interceptor);

		try (SpringRestExchangeClient exchangeClient = new SpringRestExchangeClient(ClientProperties.defaults(), restClientBuilder);
				TestApiClient apiClient = new TestApiClient(exchangeClient)) {
			String response = apiClient.get(wiremock.baseUrl() + PATH);

			assertThat(response, equalTo(BODY));
		}
		assertThat(interceptor.getLoggedBodies(), equalTo(List.of(BODY)));
	}

	@Test
	void shouldFailWhenInterceptorConsumesBodyWithDefaultRequestFactoryOnRestClient() throws Exception {
		stubJsonResponse();
		BodyLoggingInterceptor interceptor = new BodyLoggingInterceptor();
		RestClient restClient = RestClient.builder()
				.requestInterceptor(interceptor)
				.build();

		try (SpringRestExchangeClient exchangeClient = new SpringRestExchangeClient(ClientProperties.defaults(), restClient);
				TestApiClient apiClient = new TestApiClient(exchangeClient)) {
			String url = wiremock.baseUrl() + PATH;
			HttpException exception = assertThrows(HttpException.class, () -> apiClient.get(url));

			assertThat(exception.getMessage(), containsString("Error while extracting response"));
			assertThat(causeMessages(exception), containsString("Stream already closed"));
		}
	}

	@Test
	void shouldFailWhenInterceptorConsumesBodyWithDefaultRequestFactoryOnRestClientBuilder() throws Exception {
		stubJsonResponse();
		BodyLoggingInterceptor interceptor = new BodyLoggingInterceptor();
		RestClient.Builder restClientBuilder = RestClient.builder()
				.requestInterceptor(interceptor);

		try (SpringRestExchangeClient exchangeClient = new SpringRestExchangeClient(ClientProperties.defaults(), restClientBuilder);
				TestApiClient apiClient = new TestApiClient(exchangeClient)) {
			String url = wiremock.baseUrl() + PATH;
			HttpException exception = assertThrows(HttpException.class, () -> apiClient.get(url));

			assertThat(exception.getMessage(), containsString("Error while extracting response"));
			assertThat(causeMessages(exception), containsString("Stream already closed"));
		}
	}

	@Test
	void shouldUseConfiguredRequestFactoryForStreamingDownloads() throws Exception {
		stubJsonResponse();
		BufferingClientHttpRequestFactory requestFactory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
		RestClient restClient = RestClient.builder()
				.requestFactory(requestFactory)
				.build();

		try (SpringRestExchangeClient exchangeClient = new SpringRestExchangeClient(ClientProperties.defaults(), restClient)) {
			Object streamingRequestFactory = Fields.IgnoreAccess.get(
					Fields.IgnoreAccess.get(exchangeClient, "requestFactory"), "delegate");

			assertThat(streamingRequestFactory, sameInstance(requestFactory));
		}
	}

	private static void stubJsonResponse() {
		wiremock.stubFor(get(urlEqualTo(PATH))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody(BODY)));
	}

	private static String causeMessages(final Throwable throwable) {
		StringBuilder messages = new StringBuilder();
		Throwable current = throwable;
		while (null != current) {
			if (null != current.getMessage()) {
				messages.append(current.getMessage()).append('\n');
			}
			current = current.getCause();
		}
		return messages.toString();
	}

	/**
	 * Interceptor which consumes the whole response body, like a logging interceptor would.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class BodyLoggingInterceptor implements ClientHttpRequestInterceptor {

		private final List<String> loggedBodies = new ArrayList<>();

		@Override
		@SuppressWarnings("resource")
		public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
				throws IOException {
			ClientHttpResponse response = execution.execute(request, body);
			loggedBodies.add(new String(StreamUtils.copyToByteArray(response.getBody()), StandardCharsets.UTF_8));
			return response;
		}

		List<String> getLoggedBodies() {
			return loggedBodies;
		}
	}

	static class TestApiClient extends ApiClient {

		TestApiClient(final SpringRestExchangeClient springRestExchangeClient) {
			super(springRestExchangeClient);
		}

		String get(final String url) {
			return client()
					.http()
					.get()
					.url(url)
					.retrieve(String.class)
					.orRethrow();
		}
	}
}
