package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apiphany.ApiClient;
import org.apiphany.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for headers. These tests verify that the client correctly sends and handles HTTP headers, including
 * custom headers, Content-Type, Accept, and others.
 *
 * @author Radu Sebastian LAZIN
 */
public interface HeadersContract extends ApiphanyContract {

	@DisplayName("Headers: The client should send custom headers when set explicitly")
	@Test
	default void shouldSendCustomHeaders() throws Exception {
		wiremock().stubFor(get("/headers")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = apiClient();
		try (api) {
			api.client()
					.http()
					.get()
					.path("headers")
					.header("X-Test", "42")
					.retrieve();
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/headers"))
				.withHeader("X-Test", equalTo("42")));
	}

	@DisplayName("Headers: The client should not send a Content-Type header for requests without a body when not set explicitly")
	@Test
	default void shouldNotSendContentTypeWithoutBody() throws Exception {
		// Some clients will add a "Content-Type: text/plain" header by default even for empty bodies, but Java's HttpClient
		// does not, so we want to ensure that the ApiClient doesn't add it either.
		wiremock().stubFor(post("/no-body")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("post no body")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.post()
					.path("no-body")
					.retrieve(String.class)
					.orNull();

			assertEquals("post no body", result);
		}

		wiremock().verify(postRequestedFor(urlEqualTo("/no-body"))
				.withoutHeader("Content-Type"));
	}

	@DisplayName("Headers: The client should send a Content-Length header when a body is present even if Content-Type is not set explicitly")
	@Test
	default void shouldSendContentLengthWhenBodySizeIsKnown() throws Exception {
		// according RFC-9112 §6 - we always send Content-Length when the body size is known, even if Content-Type is not set
		// explicitly, and we should not add a Transfer-Encoding: chunked header in this case.
		wiremock().stubFor(post("/length")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<?> response = api.client()
					.http()
					.post()
					.path("length")
					.body("hello")
					.retrieve();

			assertNull(response.orNull());
			assertEquals(200, response.getStatus().getCode());
		}

		wiremock().verify(postRequestedFor(urlEqualTo("/length"))
				.withHeader("Content-Length", equalTo("5")));
	}

	@DisplayName("Headers: The client should not send an Accept header by default when not set explicitly")
	@Test
	default void shouldNotSendAcceptHeaderByDefault() throws Exception {
		// Many clients will send an "Accept: */*" header by default, but Java's HttpClient does not, so we want to ensure that
		// the ApiClient doesn't add it either.
		wiremock().stubFor(get("/accept")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("no accept header")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("accept")
					.retrieve(String.class)
					.orNull();

			assertEquals("no accept header", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/accept"))
				.withoutHeader("Accept"));
	}

	@DisplayName("Headers: The client should not send an Accept-Encoding header by default when not set explicitly")
	@Test
	default void shouldNotSendAcceptEncodingByDefault() throws Exception {
		// Many clients will send an "Accept-Encoding: gzip, deflate" header by default, but apiphany should not
		// set any default headers, the user has full control over what headers to send, so if the user doesn't set an
		// Accept-Encoding header, then none should be sent.
		wiremock().stubFor(get("/encoding")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("no accept-encoding header")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("encoding")
					.retrieve(String.class)
					.orNull();

			assertEquals("no accept-encoding header", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/encoding"))
				.withoutHeader("Accept-Encoding"));
	}

	@DisplayName("Headers: The client should not add a Transfer-Encoding header automatically when sending a body without an explicit Content-Type")
	@Test
	default void shouldNotAddTransferEncodingAutomatically() throws Exception {
		wiremock().stubFor(post("/chunk")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("chunked body")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.post()
					.path("chunk")
					.body("hello")
					.retrieve(String.class)
					.orNull();

			assertEquals("chunked body", result);
		}

		wiremock().verify(postRequestedFor(urlEqualTo("/chunk"))
				.withoutHeader("Transfer-Encoding"));
	}

	@DisplayName("Headers: The client should convert text/plain responses to String by default even without an Accept header")
	@Test
	default void shouldConvertTextPlainToStringByDefault() throws Exception {
		wiremock().stubFor(get("/string")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/plain")
						.withBody("the string body")));

		ApiClient api = apiClient();
		try (api) {
			var result = api.client()
					.http()
					.get()
					.path("string")
					.retrieve()
					.orNull();

			assertEquals("the string body", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/string"))
				.withoutHeader("Accept")
				.withoutHeader("Accept-Encoding")
				.withoutHeader("Content-Type"));
	}

	@DisplayName("Headers: The client should preserve the case of custom headers even if the underlying client normalizes them")
	@Test
	default void shouldPreserveCustomHeader() throws Exception {
		// OkHttp loves normalizing header names to lower case, so we want to ensure that the ApiClient preserves the case of
		// custom headers even if the underlying client doesn't.
		wiremock().stubFor(get("/case")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("case sensitive header")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("case")
					.header("X-CuStOm-HeAdEr", "42")
					.retrieve(String.class)
					.orNull();

			assertEquals("case sensitive header", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/case"))
				.withHeader("X-CuStOm-HeAdEr", equalTo("42")));
	}

	@DisplayName("Headers: The client should allow sending multiple values for the same header")
	@Test
	default void shouldSendMultipleHeaderValues() throws Exception {
		wiremock().stubFor(get("/multi")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("multiple headers")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("multi")
					.header("X-Test", "A")
					.header("X-Test", "B")
					.retrieve(String.class)
					.orRethrow();

			assertEquals("multiple headers", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/multi"))
				.withHeader("X-Test", containing("A"))
				.withHeader("X-Test", containing("B")));
	}

	@DisplayName("Headers: The client should not override User-Agent when explicitly set")
	@Test
	default void shouldRespectUserAgentWhenSet() throws Exception {
		wiremock().stubFor(get("/ua")
				.willReturn(aResponse().withStatus(200)));

		ApiClient api = apiClient();

		try (api) {
			api.client()
					.http()
					.get()
					.path("ua")
					.header("User-Agent", "apiphany-test-agent")
					.retrieve();
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/ua"))
				.withHeader("User-Agent", equalTo("apiphany-test-agent")));
	}

	@DisplayName("Headers: The client should not compress request bodies unless explicitly configured")
	@Test
	default void shouldNotCompressRequestBodyImplicitly() throws Exception {
		wiremock().stubFor(post("/compress")
				.willReturn(aResponse().withStatus(200)));

		ApiClient api = apiClient();

		try (api) {
			api.client()
					.http()
					.post()
					.path("compress")
					.body("hello")
					.retrieve();
		}

		wiremock().verify(postRequestedFor(urlEqualTo("/compress"))
				.withoutHeader("Content-Encoding"));
	}
}
