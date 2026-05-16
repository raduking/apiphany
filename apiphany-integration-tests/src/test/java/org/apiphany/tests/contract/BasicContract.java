package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.headRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.apiphany.ApiClient;
import org.apiphany.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.matching.UrlPattern;

/**
 * This contract defines basic HTTP client functionality that should be supported by all ApiClient implementations.
 *
 * @author Radu Sebastian LAZIN
 */
public interface BasicContract extends ApiphanyContract {

	@DisplayName("Basic: The client should be able to make a simple GET request and return the response body")
	@Test
	default void shouldReturnBody() throws Exception {
		wiremock().stubFor(get("/hello")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("world")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("hello")
					.retrieve(String.class)
					.orNull();

			assertEquals("world", result);
		}

		wiremock().verify(1, getRequestedFor(urlEqualTo("/hello")));
	}

	@DisplayName("Basic: The client should handle empty response body correctly when Content-Length: 0 is set")
	@Test
	default void shouldHandleEmptyBody200() throws Exception {
		// some clients behave differently for Content-Length: 0 vs no body. We want to ensure that the ApiClient handles both
		// cases correctly.
		wiremock().stubFor(get("/empty")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Length", "0")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("empty")
					.retrieve(String.class)
					.orNull();

			assertNull(result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/empty")));
	}

	@DisplayName("Basic: The client should handle 204 No Content responses without trying to deserialize the body")
	@Test
	default void shouldHandle204NoContent() throws Exception {
		// 204 must not try to deserialize.
		wiremock().stubFor(get("/no-content")
				.willReturn(aResponse()
						.withStatus(204)));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<?> response = api.client()
					.http()
					.get()
					.path("no-content")
					.retrieve();

			assertNull(response.orNull());
			assertEquals(204, response.getStatus().getCode());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/no-content")));
	}

	@DisplayName("Basic: The client should be able to send query parameters")
	@Test
	default void shouldSendQueryParams() throws Exception {
		wiremock().stubFor(get(urlPathEqualTo("/query"))
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("Foo OK")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("query")
					.param("foo", "bar")
					.retrieve(String.class)
					.orNull();

			assertEquals("Foo OK", result);
		}

		wiremock().verify(getRequestedFor(urlPathEqualTo("/query"))
				.withQueryParam("foo", equalTo("bar")));
	}

	@DisplayName("Basic: The client should be able to send a request body")
	@Test
	default void shouldSendBodyWithPost() throws Exception {
		wiremock().stubFor(post("/body")
				.willReturn(aResponse().withStatus(200)));

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.post()
					.path("body")
					.body("hello")
					.retrieve()
					.orNull();
		}

		wiremock().verify(postRequestedFor(urlEqualTo("/body"))
				.withRequestBody(equalTo("hello")));
	}

	@DisplayName("Basic: The client should be able to make a HEAD request and return the response status and headers")
	@Test
	default void shouldSupportHeadMethod() throws Exception {
		wiremock().stubFor(head(UrlPattern.ANY)
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("X-Test", "42")));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<?> response = api.client()
					.http()
					.head()
					.path("head")
					.retrieve();

			assertNull(response.orNull());
			assertDoesNotThrow(() -> response.orRethrow()); // NOSONAR lambda to avoid ambiguity
			assertEquals(200, response.getStatus().getCode());
			assertEquals(List.of("42"), response.getHeaders().get("X-Test"));
		}

		wiremock().verify(headRequestedFor(urlEqualTo("/head")));
	}
}
