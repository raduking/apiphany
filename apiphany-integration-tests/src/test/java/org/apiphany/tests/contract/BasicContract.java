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
import org.apiphany.Status;
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

			assertEquals("", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/empty")));
	}

	@DisplayName("Basic: The client should handle empty response body correctly when Content-Length: 0 is set and no response type is provided")
	@Test
	default void shouldHandleEmptyBody200WithNoResponseType() throws Exception {
		// some clients behave differently for Content-Length: 0 vs no body. We want to ensure that the ApiClient handles both
		// cases correctly.
		wiremock().stubFor(get("/empty")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Length", "0")));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<?> response = api.client()
					.http()
					.get()
					.path("empty")
					.retrieve();
			Object result = response.orNull();

			assertNull(response.getBody());
			assertEquals(200, response.getStatus().getCode());
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
			Object result = response.orNull();

			assertNull(result);
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

	@DisplayName("Basic: The client should join base URL and request path correctly")
	@Test
	default void shouldJoinBaseUrlAndPathCorrectly() throws Exception {
		wiremock().stubFor(get("/join")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("joined")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("/join")
					.retrieve(String.class)
					.orNull();

			assertEquals("joined", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/join")));
	}

	@DisplayName("Basic: The client should URL-encode query parameter values when encoding is explicitly enabled")
	@Test
	default void shouldEncodeQueryParams() throws Exception {
		wiremock().stubFor(get(urlPathEqualTo("/encoding"))
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("ok")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("encoding")
					.urlEncoded()
					.param("q", "hello world")
					.retrieve(String.class)
					.orNull();

			assertEquals("ok", result);
		}

		wiremock().verify(getRequestedFor(urlPathEqualTo("/encoding"))
				.withQueryParam("q", equalTo("hello world")));
	}

	@DisplayName("Basic: The client should not URL-encode query parameters implicitly")
	@Test
	default void shouldNotEncodeQueryParamsImplicitly() throws Exception {
		wiremock().stubFor(get(urlPathEqualTo("/raw"))
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("ok")));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<String> response = api.client()
					.http()
					.get()
					.path("raw")
					.param("q", "hello world")
					.retrieve(String.class);

			assertEquals(Status.UNKNOWN, response.getStatusCode());
			assertNull(response.getStatus());
			assertNull(response.orNull());
		}

		wiremock().verify(0, getRequestedFor(urlEqualTo("/raw?q=hello%20world")));
		wiremock().verify(0, getRequestedFor(urlEqualTo("/raw?q=hello")));
	}

	@DisplayName("Basic: The client should support repeated query parameters")
	@Test
	default void shouldSupportRepeatedQueryParams() throws Exception {
		wiremock().stubFor(get(urlPathEqualTo("/repeat"))
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.get()
					.path("repeat")
					.param("tag", "a")
					.param("tag", "b")
					.retrieve();
		}

		wiremock().verify(getRequestedFor(urlPathEqualTo("/repeat"))
				.withQueryParam("tag", equalTo("a"))
				.withQueryParam("tag", equalTo("b")));
	}

	@DisplayName("Basic: Response headers should be accessible case-insensitively")
	@Test
	default void shouldAccessHeadersCaseInsensitively() throws Exception {
		wiremock().stubFor(get("/headers")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("content-type", "text/plain")
						.withBody("ok")));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<String> response = api.client()
					.http()
					.get()
					.path("headers")
					.retrieve(String.class);

			assertEquals(List.of("text/plain"),
					response.getHeaders().get("Content-Type"));
		}
	}

	@DisplayName("Basic: HEAD responses should not attempt body deserialization")
	@Test
	default void shouldIgnoreHeadResponseBody() throws Exception {
		wiremock().stubFor(head(urlEqualTo("/head-body"))
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("illegal-body")));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<?> response = api.client()
					.http()
					.head()
					.path("head-body")
					.retrieve();

			assertNull(response.orNull());
			assertEquals(200, response.getStatus().getCode());
		}
	}
}
