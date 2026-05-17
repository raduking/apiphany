package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.apiphany.ApiClient;
import org.apiphany.ApiResponse;
import org.apiphany.Status;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpException;
import org.apiphany.test.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.morphix.lang.function.ThrowingRunnable;
import org.morphix.lang.thread.Threads;

/**
 * Contract tests for error handling. These tests verify that the client correctly handles server errors (5xx) and
 * client errors (4xx).
 *
 * @author Radu Sebastian LAZIN
 */
public interface ErrorsContract extends ApiphanyContract {

	@DisplayName("Error Handling: The client should return null when the server returns a server error 5xx without throwing an exception when using orNull()")
	@Test
	default void shouldHandleServerError() throws Exception {
		wiremock().stubFor(get("/500")
				.willReturn(aResponse()
						.withStatus(500)));

		ApiClient api = apiClient();
		try (api) {
			var result = api.client()
					.http()
					.get()
					.path("500")
					.retrieve(String.class)
					.orNull();

			assertNull(result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/500")));
	}

	@DisplayName("Error Handling: The client should return the error body when the server returns a server error 5xx without throwing an exception when using orNull()")
	@Test
	default void shouldHandleServerErrorWithBody() throws Exception {
		wiremock().stubFor(get("/500")
				.willReturn(aResponse()
						.withStatus(500)
						.withBody("This is a server error body")));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<String> result = api.client()
					.http()
					.get()
					.path("500")
					.retrieve(String.class);

			assertNull(result.orNull());
			assertEquals(500, result.getStatus().getCode());
			assertEquals("This is a server error body", result.getBody());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/500")));
	}

	@DisplayName("Error Handling: The client should return null when the server returns a client error 4xx without throwing an exception when using orNull()")
	@Test
	default void shouldHandleClientError() throws Exception {
		wiremock().stubFor(get("/404")
				.willReturn(aResponse()
						.withStatus(404)));

		ApiClient api = apiClient();
		try (api) {
			var result = api.client()
					.http()
					.get()
					.path("404")
					.retrieve(String.class)
					.orNull();

			assertNull(result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/404")));
	}

	@DisplayName("Error Handling: The client should return the error body when the server returns a client error 4xx without throwing an exception when using orNull()")
	@Test
	default void shouldHandleClientErrorWithBody() throws Exception {
		wiremock().stubFor(get("/404")
				.willReturn(aResponse()
						.withStatus(404)
						.withBody("This is a not found body")));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<String> result = api.client()
					.http()
					.get()
					.path("404")
					.retrieve(String.class);

			assertNull(result.orNull());
			assertEquals(404, result.getStatus().getCode());
			assertEquals("This is a not found body", result.getBody());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/404")));
	}

	@DisplayName("Error Handling: The client should throw an HttpException when the request times out")
	@Test
	default void shouldTimeoutAndThrowHttpException() throws Exception {
		wiremock().stubFor(get("/slow")
				.willReturn(aResponse()
						.withFixedDelay(3000)
						.withStatus(200)));

		ClientProperties properties = new ClientProperties();
		properties.getTimeout().setConnect(Duration.ofMillis(100));
		properties.getTimeout().setRequest(Duration.ofMillis(100));

		ApiClient api = apiClient(properties);
		try (api) {
			HttpException e = assertThrows(HttpException.class, () -> api.client() // NOSONAR should not complain
					.http()
					.get()
					.path("slow")
					.retrieve()
					.orRethrow());

			assertNotNull(e.getCause());
			assertEquals(null, e.getStatus());
			assertEquals(Status.UNKNOWN, e.getStatusCode());
		}

		ThrowingRunnable assertion = () -> wiremock().verify(getRequestedFor(urlEqualTo("/slow")));

		boolean requestReachedServer = Threads.waitUntil(Assertions.asserted(assertion), Duration.ofSeconds(5));
		assertTrue(requestReachedServer, "Expected the request to be received by the server within 5 seconds");
	}
}
