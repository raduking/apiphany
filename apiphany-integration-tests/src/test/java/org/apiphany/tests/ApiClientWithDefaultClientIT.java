package org.apiphany.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.headRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.apiphany.ApiClient;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.CountingHttpExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.ContentEncoding;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.io.InputStreamSupplier;
import org.apiphany.io.deflate.Deflate;
import org.apiphany.io.gzip.GZip;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationType;
import org.apiphany.test.io.OneShotInputStream;
import org.apiphany.tests.contract.HeadersContract;
import org.apiphany.tests.contract.JsonContract;
import org.apiphany.tests.contract.RedirectsContract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.morphix.lang.retry.Retry;
import org.morphix.lang.retry.WaitCounter;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.jayway.jsonpath.JsonPath;

/**
 * Test class with WireMock for {@link ApiClient} using {@link JavaNetHttpExchangeClient} and serves as a base class for
 * all other ApiClient integration tests that use other http clients.
 * <p>
 * The main purpose of this class is to verify that the ApiClient works correctly with Java's built-in HttpClient as the
 * underlying HTTP client, since this is what the ApiClient uses by default if no other HTTP client is available on the
 * classpath. This ensures that the ApiClient can be used in environments where no third-party HTTP client libraries are
 * available, such as in a Java SE environment without additional dependencies.
 * <p>
 * For other clients, we can extend this class and override the {@link #exchangeClientClass()} to return the appropriate
 * client class, {@link #getClient(AuthenticationType)} method to return an instance of that client with the specified
 * authentication type, and then we can reuse all the tests defined in this class to verify that the ApiClient works
 * correctly with that client as well.
 * <p>
 * TODO: group tests into nested classes based on functionality (e.g. redirects, gzip handling, error handling, etc.) to
 * improve readability.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientWithDefaultClientIT implements HeadersContract, RedirectsContract, JsonContract {

	@RegisterExtension
	private static final WireMockExtension wiremock =
			WireMockExtension.newInstance()
					.options(options().dynamicPort())
					.build();

	@Override
	public WireMockExtension wiremock() {
		return wiremock;
	}

	@Test
	void shouldReturnBody() throws Exception {
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

	@Test
	void shouldSendHeaders() throws Exception {
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

	@Test
	void shouldSendQueryParams() throws Exception {
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

	@Test
	void shouldSendPostBody() throws Exception {
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

	@Test
	void shouldDecodeGzipAutomatically() throws Exception {
		byte[] gzipped = GZip.compress("hello");

		wiremock().stubFor(get("/gzip")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip")
						.withBody(gzipped)));

		ApiClient api = apiClient();
		try (api) {
			var result = api.client()
					.http()
					.get()
					.header(HttpHeader.ACCEPT_ENCODING, ContentEncoding.GZIP)
					.path("gzip")
					.retrieve(String.class)
					.orNull();

			assertEquals("hello", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/gzip"))
				.withHeader("Accept-Encoding", containing("gzip")));
	}

	@Test
	void shouldDecodeGzipThenDeflateAutomatically() throws Exception {
		byte[] gzipped = GZip.compress("hello".getBytes(StandardCharsets.UTF_8));
		byte[] deflateThenGzip = Deflate.compress(gzipped);

		wiremock().stubFor(get("/gzip-deflate")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip, deflate")
						.withBody(deflateThenGzip)));

		ApiClient api = apiClient();
		try (api) {
			var result = api.client()
					.http()
					.get()
					.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
					.path("gzip-deflate")
					.retrieve(String.class)
					.orNull();

			assertEquals("hello", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/gzip-deflate"))
				.withHeader("Accept-Encoding", containing("gzip"))
				.withHeader("Accept-Encoding", containing("deflate")));
	}

	@Test
	void shouldNotFailWhenBodyIsNotActuallyGzipped() throws Exception {
		wiremock().stubFor(get("/fake-gzip")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip")
						.withBody("hello")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.header(HttpHeader.ACCEPT_ENCODING, ContentEncoding.GZIP)
					.path("fake-gzip")
					.retrieve(String.class)
					.orNull();

			assertEquals("hello", result);
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/fake-gzip")));
	}

	@Test
	void shouldParseContentEncodingWithSpaces() throws Exception {
		byte[] gzipped = GZip.compress("hello".getBytes(StandardCharsets.UTF_8));
		byte[] body = Deflate.compress(gzipped);

		wiremock().stubFor(get("/encoding-spaces")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip ,  deflate")
						.withBody(body)));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
					.path("encoding-spaces")
					.retrieve(String.class)
					.orNull();

			assertEquals("hello", result);
		}
	}

	@Test
	void shouldIgnoreUnknownEncoding() throws Exception {
		byte[] body = GZip.compress("hello".getBytes(StandardCharsets.UTF_8));

		wiremock().stubFor(get("/unknown-encoding")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip, weird")
						.withBody(body)));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.header(HttpHeader.ACCEPT_ENCODING, "gzip")
					.path("unknown-encoding")
					.retrieve(String.class)
					.orNull();

			assertEquals("hello", result);
		}
	}

	@Test
	void shouldNotAutoDecompressDoubleGzip() throws Exception {
		byte[] onceGzipped = GZip.compress("hello");
		byte[] doubleGzipped = GZip.compress(onceGzipped);

		wiremock().stubFor(get("/double-gzip")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip")
						.withBody(doubleGzipped)));

		ApiClient api = apiClient();
		try (api) {
			byte[] result = api.client()
					.http()
					.get()
					.path("double-gzip")
					.header(HttpHeader.ACCEPT_ENCODING, ContentEncoding.GZIP)
					.retrieve(byte[].class)
					.orNull();

			// the content should still be a gzip-ped byte array
			assertNotNull(result);
			// must NOT auto-decode twice, so the result is not the original "hello"
			assertNotEquals("hello", new String(result, Strings.DEFAULT_CHARSET));

			byte[] decompressedTwice = GZip.decompress(result);
			assertEquals("hello", new String(decompressedTwice, Strings.DEFAULT_CHARSET));
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/double-gzip"))
				.withHeader("Accept-Encoding", containing("gzip")));
	}

	@Test
	void shouldHandleEmptyGzipResponse() throws Exception {
		wiremock().stubFor(get("/empty-gzip")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip")
						.withBody(new byte[0])));

		ApiClient api = apiClient();
		try (api) {
			byte[] result = api.client()
					.http()
					.get()
					.path("empty-gzip")
					.retrieve(byte[].class)
					.orNull();

			assertNotNull(result);
			assertEquals(0, result.length);
		}
	}

	@Test
	void shouldHandleClientError() throws Exception {
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

	@Test
	void shouldHandleClientErrorWithBody() throws Exception {
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

	@Test
	void shouldHandleServerError() throws Exception {
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

	@Test
	void shouldHandleServerErrorWithBody() throws Exception {
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

	@Test
	void shouldTimeout() throws Exception {
		wiremock().stubFor(get("/slow")
				.willReturn(aResponse()
						.withFixedDelay(3000)
						.withStatus(200)));

		ClientProperties properties = clientProperties();
		properties.getTimeout().setConnect(Duration.ofMillis(100));
		properties.getTimeout().setRequest(Duration.ofMillis(100));

		ApiClient api = ApiClient.of(baseUrl(),
				ApiClient.with(exchangeClientClass()).properties(properties));
		try (api) {
			HttpException e = assertThrows(HttpException.class, () -> api.client() // NOSONAR should not complain
					.http()
					.get()
					.path("slow")
					.retrieve()
					.orRethrow());

			assertNotNull(e.getCause());
			assertEquals(500, e.getStatusCode());
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/slow")));
	}


	@Test
	void shouldNotRetryByDefault() throws Exception {
		// Apache's HttpClient will automatically retry failed requests by default, but Java's HttpClient does not, so we want
		// to ensure that the ApiClient doesn't add retries either.
		wiremock().stubFor(get("/flaky")
				.willReturn(aResponse()
						.withStatus(500)
						.withBody("flaky error response")));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<String> response = api.client()
					.http()
					.get()
					.path("flaky")
					.retrieve(String.class);

			assertNull(response.orNull());
			assertEquals(500, response.getStatus().getCode());
			assertEquals("flaky error response", response.getBody());
		}

		wiremock().verify(1, getRequestedFor(urlEqualTo("/flaky")));
	}

	@Test
	void shouldNotTransformPostToGetOn307() throws Exception {
		wiremock().stubFor(post("/redirect307")
				.willReturn(aResponse()
						.withStatus(307)
						.withHeader("Location", "/target")));

		ApiClient api = apiClient();
		try (api) {
			ApiResponse<?> response = api.client()
					.http()
					.post()
					.path("redirect307")
					.body("test")
					.retrieve();

			assertNull(response.orNull());
			assertEquals(307, response.getStatus().getCode());
			assertEquals(List.of("/target"), response.getHeaders().get("Location"));
		}

		wiremock().verify(postRequestedFor(urlEqualTo("/redirect307")));
		wiremock().verify(0, getRequestedFor(urlEqualTo("/target")));
	}

	@Test
	void shouldNotAddTransferEncodingAutomatically() throws Exception {
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

	@Test
	void shouldHandleEmptyBody200() throws Exception {
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

	@Test
	void shouldHandle204NoContent() throws Exception {
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

	@Test
	void shouldSendContentLengthWhenBodyPresent() throws Exception {
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

	@Test
	void shouldReuseConnectionForMultipleRequests() throws Exception {
		wiremock().stubFor(get("/reuse")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = apiClient();
		try (api) {
			api.client().http().get().path("reuse").retrieve();
			api.client().http().get().path("reuse").retrieve();
		}

		wiremock().verify(2, getRequestedFor(urlEqualTo("/reuse")));
	}

	@Test
	void shouldRespectCharsetFromContentType() throws Exception {
		wiremock().stubFor(get("/charset")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/plain; charset=ISO-8859-1")
						.withBody(new byte[] { (byte) 0xE9 }))); // é in ISO-8859-1

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("charset")
					.retrieve(String.class)
					.orNull();

			assertEquals("é", result);
		}

		wiremock().verify(1, getRequestedFor(urlEqualTo("/charset")));
	}

	@Test
	void shouldSupportHeadMethod() throws Exception {
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

	@Test
	void shouldSendMultipleHeaderValues() throws Exception {
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

	@Test
	void shouldRetryAndEventuallySucceed() throws Exception {
		wiremock().stubFor(get("/retry-success")
				.inScenario("retry")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500)
						.withBody("first error response"))
				.willSetStateTo("second"));

		wiremock().stubFor(get("/retry-success")
				.inScenario("retry")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("OK")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("retry-success")
					.retry(Retry.of(WaitCounter.of(2, Duration.ofMillis(100))))
					.retrieve(String.class)
					.orNull();

			assertEquals("OK", result);
		}

		// 2 attempts total
		wiremock().verify(2, getRequestedFor(urlEqualTo("/retry-success")));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldFailRetryBeforeSendWhenBodyIsNonRepeatable() throws Exception {
		wiremock().stubFor(post("/retry-stream")
				.inScenario("retry-stream")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		wiremock().stubFor(post("/retry-stream")
				.inScenario("retry-stream")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = apiClient();
		try (api) {
			assertThrows(Exception.class, () -> api.client()
					.http()
					.post()
					.path("retry-stream")
					.body(new OneShotInputStream("hello"))
					.retry(Retry.of(WaitCounter.of(2, Duration.ofMillis(100))))
					.retrieve()
					.orRethrow());
		}

		// this will break most clients since the InputStream can only be read once, but we want to ensure that the ApiClient
		// properly retries even in this case by re-creating the stream for the retry attempt
		wiremock().verify(1, postRequestedFor(urlEqualTo("/retry-stream"))
				.withRequestBody(equalTo("hello")));
	}

	@Test
	void shouldNotFailRetryBeforeSendWhenBodyIsInputStreamSupplierRepeatable() throws Exception {
		wiremock().stubFor(post("/retry-stream-1")
				.inScenario("retry-stream-1")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		wiremock().stubFor(post("/retry-stream-1")
				.inScenario("retry-stream-1")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("hi")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.post()
					.path("retry-stream-1")
					.body(InputStreamSupplier.from(() -> new OneShotInputStream("hello")))
					.retry(Retry.of(WaitCounter.of(2, Duration.ofMillis(100))))
					.retrieve(String.class)
					.orRethrow();

			assertEquals("hi", result);
		}

		// this will break most clients since the InputStream can only be read once, but we want to ensure that the ApiClient
		// properly retries even in this case by re-creating the stream for the retry attempt
		wiremock().verify(2, postRequestedFor(urlEqualTo("/retry-stream-1"))
				.withRequestBody(equalTo("hello")));
	}

	@Test
	void shouldNotFailRetryBeforeSendWhenBodyIsRepeatableByRecreatingTheInputStream() throws Exception {
		wiremock().stubFor(post("/retry-stream-2")
				.inScenario("retry-stream-2")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		wiremock().stubFor(post("/retry-stream-2")
				.inScenario("retry-stream-2")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("hi")));

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.post()
					.path("retry-stream-2")
					.body(() -> new OneShotInputStream("hello"))
					.retry(Retry.of(WaitCounter.of(2, Duration.ofMillis(100))))
					.retrieve(String.class)
					.orRethrow();

			assertEquals("hi", result);
		}

		// this will break most clients since the InputStream can only be read once, but we want to ensure that the ApiClient
		// properly retries even in this case by re-creating the stream for the retry attempt
		wiremock().verify(2, postRequestedFor(urlEqualTo("/retry-stream-2"))
				.withRequestBody(equalTo("hello")));
	}

	@Test
	void shouldFailRetryWhenSupplierReturnsSameConsumedInputStream() throws Exception {
		final String expectedBody = "hello world";

		// first request fails
		wiremock().stubFor(post("/retry-stream-3")
				.inScenario("retry-stream-3")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		// retry succeeds
		wiremock().stubFor(post("/retry-stream-3")
				.inScenario("retry-stream-3")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("hi")));

		InputStream alreadyOpened =
				new ByteArrayInputStream(expectedBody.getBytes(StandardCharsets.UTF_8));

		Supplier<InputStream> brokenSupplier = () -> alreadyOpened;

		ApiClient api = apiClient();
		try (api) {
			String result = api.client()
					.http()
					.post()
					.path("retry-stream-3")
					.body(brokenSupplier)
					.charset(StandardCharsets.UTF_8)
					.retry(Retry.of(WaitCounter.of(2, Duration.ofMillis(100))))
					.retrieve(String.class)
					.orRethrow();

			assertEquals("hi", result);
		}

		// first attempt
		wiremock().verify(1, postRequestedFor(urlEqualTo("/retry-stream-3"))
				.withRequestBody(equalTo(expectedBody)));

		// retry attempt MUST be broken (stream already consumed)
		wiremock().verify(1, postRequestedFor(urlEqualTo("/retry-stream-3"))
				.withRequestBody(WireMock.notMatching(expectedBody)));

		List<ServeEvent> events =
				wiremock().getAllServeEvents().stream()
						.filter(e -> e.getRequest().getUrl().equals("/retry-stream-3"))
						.toList();

		// events are in reverse order (newest first), so the first attempt is events.get(1) and the retry is events.get(0)
		assertEquals(2, events.size());
		assertEquals("", events.get(0).getRequest().getBodyAsString());
		assertEquals(expectedBody, events.get(1).getRequest().getBodyAsString());
	}

	@Test
	void shouldUseDifferentClientForDifferentAuthType() throws Exception {
		// stub two different auth types → e.g. TOKEN and SESSION
		wiremock().stubFor(get("/token")
				.willReturn(okJson("{\"auth\":\"token\"}")));
		wiremock().stubFor(get("/session")
				.willReturn(okJson("{\"auth\":\"session\"}")));

		ExchangeClient tokenClient = getClient(AuthenticationType.TOKEN);
		ExchangeClient sessionClient = getClient(AuthenticationType.SESSION);

		ApiClient api = ApiClient.of(baseUrl(),
				ApiClient.with(tokenClient)
						.properties(clientProperties())
						.decoratedWith(CountingHttpExchangeClient.class),
				ApiClient.with(sessionClient)
						.properties(clientProperties())
						.decoratedWith(CountingHttpExchangeClient.class));
		try (tokenClient; sessionClient; api) {
			String tokenResult = api.client(AuthenticationType.TOKEN)
					.http()
					.get()
					.path("token")
					.retrieve(String.class)
					.orNull();
			String sessionResult = api.client(AuthenticationType.SESSION)
					.http()
					.get()
					.path("session")
					.retrieve(String.class)
					.orNull();

			assertEquals("token", JsonPath.parse(tokenResult).read("$.auth"));
			assertEquals("session", JsonPath.parse(sessionResult).read("$.auth"));

			@SuppressWarnings("resource")
			CountingHttpExchangeClient tokenCountingClient = api.client(AuthenticationType.TOKEN)
					.getExchangeClient(CountingHttpExchangeClient.class);
			@SuppressWarnings("resource")
			CountingHttpExchangeClient sessionCountingClient = api.client(AuthenticationType.SESSION)
					.getExchangeClient(CountingHttpExchangeClient.class);

			assertEquals(1, tokenCountingClient.getRequestCount());
			assertEquals(1, sessionCountingClient.getRequestCount());
		}

		wiremock().verify(1, getRequestedFor(urlEqualTo("/token")));
		wiremock().verify(1, getRequestedFor(urlEqualTo("/session")));
	}

	@Test
	void shouldHandleConcurrentRequests() throws Exception {
		wiremock().stubFor(get(urlMatching("/concurrent/.*"))
				.willReturn(ok()));

		int threadCount = 50;
		ExecutorService exe = Executors.newVirtualThreadPerTaskExecutor();

		ApiClient api = apiClient();
		try (exe; api) {
			List<CompletableFuture<String>> futures = IntStream.range(0, threadCount)
					.mapToObj(i -> CompletableFuture.supplyAsync(() -> api.client()
							.http()
							.get()
							.path("/concurrent/" + i)
							.retrieve(String.class)
							.orNull(),
							exe))
					.toList();

			CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
		}

		wiremock().verify(50, getRequestedFor(urlMatching("/concurrent/.*")));
	}
}
