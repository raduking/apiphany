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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.apiphany.ApiClient;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.DecoratingExchangeClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.ContentEncoding;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.io.InputStreamSupplier;
import org.apiphany.io.gzip.GZip;
import org.apiphany.lang.ScopedResource;
import org.apiphany.lang.retry.Retry;
import org.apiphany.lang.retry.WaitCounter;
import org.apiphany.security.AuthenticationType;
import org.apiphany.test.io.OneShotInputStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.jayway.jsonpath.JsonPath;

/**
 * Test class for {@link ApiClient} using {@link JavaNetHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientWithJavaNetHttpIT {

	@RegisterExtension
	private static final WireMockExtension wiremock =
			WireMockExtension.newInstance()
					.options(options().dynamicPort())
					.build();

	protected String baseUrl() {
		return wiremock.getRuntimeInfo().getHttpBaseUrl();
	}

	protected Class<? extends ExchangeClient> exchangeClientClass() {
		return JavaNetHttpExchangeClient.class;
	}

	public ExchangeClient getClient(final AuthenticationType authType) {
		return new JavaNetHttpExchangeClient() {
			@Override
			public AuthenticationType getAuthenticationType() {
				return authType;
			}
		};
	}

	static class CountingHttpExchangeClient extends DecoratingExchangeClient implements HttpExchangeClient {

		private final AtomicInteger requestCount = new AtomicInteger(0);

		public CountingHttpExchangeClient(final ScopedResource<ExchangeClient> delegate) {
			super(delegate);
		}

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
			requestCount.incrementAndGet();
			return super.exchange(apiRequest);
		}

		public int getRequestCount() {
			return requestCount.get();
		}
	}

	@Test
	void shouldReturnBody() throws Exception {
		wiremock.stubFor(get("/hello")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("world")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("hello")
					.retrieve(String.class)
					.orNull();

			assertEquals("world", result);
		}

		wiremock.verify(1, getRequestedFor(urlEqualTo("/hello")));
	}

	@Test
	void shouldNotFollowRedirectsByDefault() throws Exception {
		wiremock.stubFor(get("/redirect")
				.willReturn(aResponse()
						.withStatus(302)
						.withHeader("Location", "/target")));

		wiremock.stubFor(get("/target")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("OK")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			var result = api.client()
					.http()
					.get()
					.path("redirect")
					.retrieve(String.class)
					.orNull();
			assertNull(result);
		}

		// should have called redirect exactly once
		wiremock.verify(1, getRequestedFor(urlEqualTo("/redirect")));

		// MUST NOT have followed redirect
		wiremock.verify(0, getRequestedFor(urlEqualTo("/target")));
	}

	@Disabled("Redirects are not yet supported by JavaNetHttpExchangeClient")
	@Test
	void shouldFollowRedirectsWhenEnabled() throws Exception {
		wiremock.stubFor(get("/redirect")
				.willReturn(aResponse()
						.withStatus(302)
						.withHeader("Location", "/target")));

		wiremock.stubFor(get("/target")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("OK")));

		ClientProperties properties = new ClientProperties();
		ApiClient api = ApiClient.of(baseUrl(),
				ApiClient.with(exchangeClientClass()).properties(properties));
		try (api) {
			var result = api
					.client()
					.http()
					.get()
					.path("redirect")
					.retrieve(String.class)
					.orNull();

			assertEquals("OK", result);
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/redirect")));
		wiremock.verify(getRequestedFor(urlEqualTo("/target")));
	}

	@Test
	void shouldSendHeaders() throws Exception {
		wiremock.stubFor(get("/headers")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			api.client()
					.http()
					.get()
					.path("headers")
					.header("X-Test", "42")
					.retrieve();
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/headers"))
				.withHeader("X-Test", equalTo("42")));
	}

	@Test
	void shouldSendQueryParams() throws Exception {
		wiremock.stubFor(get(urlPathEqualTo("/query"))
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("Foo OK")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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

		wiremock.verify(getRequestedFor(urlPathEqualTo("/query"))
				.withQueryParam("foo", equalTo("bar")));
	}

	record MyDto(String name) {
		// no additional code needed
	}

	@Test
	void shouldDeserializeJson() throws Exception {
		wiremock.stubFor(get("/json")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody("""
								    {"name":"john"}
								""")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			var result = api.client()
					.http()
					.get()
					.path("json")
					.retrieve(MyDto.class)
					.orNull();

			assertEquals("john", result.name());
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/json")));
	}

	@Test
	void shouldSendPostBody() throws Exception {
		wiremock.stubFor(post("/body")
				.willReturn(aResponse().withStatus(200)));

		ApiClient client = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (client) {
			client.client()
					.http()
					.post()
					.path("body")
					.body("hello")
					.retrieve()
					.orNull();
		}

		wiremock.verify(postRequestedFor(urlEqualTo("/body"))
				.withRequestBody(equalTo("hello")));
	}

	@Test
	void shouldDecodeGzipAutomatically() throws Exception {
		byte[] gzipped = GZip.compress("hello");

		wiremock.stubFor(get("/gzip")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Encoding", "gzip")
						.withBody(gzipped)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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

		wiremock.verify(getRequestedFor(urlEqualTo("/gzip"))
				.withHeader("Accept-Encoding", containing("gzip")));
	}

	@Test
	void shouldHandleClientError() throws Exception {
		wiremock.stubFor(get("/404")
				.willReturn(aResponse()
						.withStatus(404)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			var result = api.client()
					.http()
					.get()
					.path("404")
					.retrieve(String.class)
					.orNull();

			assertNull(result);
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/404")));
	}

	@Test
	void shouldHandleClientErrorWithBody() throws Exception {
		wiremock.stubFor(get("/404")
				.willReturn(aResponse()
						.withStatus(404)
						.withBody("This is a not found body")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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

		wiremock.verify(getRequestedFor(urlEqualTo("/404")));
	}

	@Test
	void shouldHandleServerError() throws Exception {
		wiremock.stubFor(get("/500")
				.willReturn(aResponse()
						.withStatus(500)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			var result = api.client()
					.http()
					.get()
					.path("500")
					.retrieve(String.class)
					.orNull();

			assertNull(result);
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/500")));
	}

	@Test
	void shouldHandleServerErrorWithBody() throws Exception {
		wiremock.stubFor(get("/500")
				.willReturn(aResponse()
						.withStatus(500)
						.withBody("This is a server error body")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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

		wiremock.verify(getRequestedFor(urlEqualTo("/500")));
	}

	@Test
	void shouldTimeout() throws Exception {
		wiremock.stubFor(get("/slow")
				.willReturn(aResponse()
						.withFixedDelay(3000)
						.withStatus(200)));

		ClientProperties properties = new ClientProperties();
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

		wiremock.verify(getRequestedFor(urlEqualTo("/slow")));
	}

	@Test
	void shouldNotSendContentTypeWithoutBody() throws Exception {
		// Some clients will add a "Content-Type: text/plain" header by default even for empty bodies, but Java's HttpClient
		// does not, so we want to ensure that the ApiClient doesn't add it either.
		wiremock.stubFor(post("/no-body")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("post no body")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			String result = api.client()
					.http()
					.post()
					.path("no-body")
					.retrieve(String.class)
					.orNull();

			assertEquals("post no body", result);
		}

		wiremock.verify(postRequestedFor(urlEqualTo("/no-body"))
				.withoutHeader("Content-Type"));
	}

	@Test
	void shouldNotSendAcceptHeaderByDefault() throws Exception {
		// Many clients will send an "Accept: */*" header by default, but Java's HttpClient does not, so we want to ensure that
		// the ApiClient doesn't add it either.
		wiremock.stubFor(get("/accept")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("no accept header")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("accept")
					.retrieve(String.class)
					.orNull();

			assertEquals("no accept header", result);
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/accept"))
				.withoutHeader("Accept"));
	}

	@Test
	void shouldNotSendAcceptEncodingByDefault() throws Exception {
		// Many clients will send an "Accept-Encoding: gzip, deflate" header by default, but Java's HttpClient does not, so we
		// want to ensure that the ApiClient doesn't add it either.
		wiremock.stubFor(get("/encoding")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("no accept-encoding header")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("encoding")
					.retrieve(String.class)
					.orNull();

			assertEquals("no accept-encoding header", result);
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/encoding"))
				.withoutHeader("Accept-Encoding"));
	}

	@Test
	void shouldConvertTextPlainToStringByDefault() throws Exception {
		wiremock.stubFor(get("/string")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/plain")
						.withBody("the string body")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			var result = api.client()
					.http()
					.get()
					.path("string")
					.retrieve()
					.orNull();

			assertEquals("the string body", result);
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/string"))
				.withoutHeader("Accept")
				.withoutHeader("Accept-Encoding")
				.withoutHeader("Content-Type"));
	}

	@Test
	void shouldPreserveCustomHeader() throws Exception {
		// OkHttp loves normalizing header names to lower case, so we want to ensure that the ApiClient preserves the case of
		// custom headers even if the underlying client doesn't.
		wiremock.stubFor(get("/case")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("case sensitive header")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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

		wiremock.verify(getRequestedFor(urlEqualTo("/case"))
				.withHeader("X-CuStOm-HeAdEr", equalTo("42")));
	}

	@Test
	void shouldNotRetryByDefault() throws Exception {
		// Apache's HttpClient will automatically retry failed requests by default, but Java's HttpClient does not, so we want
		// to ensure that the ApiClient doesn't add retries either.
		wiremock.stubFor(get("/flaky")
				.willReturn(aResponse()
						.withStatus(500)
						.withBody("flaky error response")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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

		wiremock.verify(1, getRequestedFor(urlEqualTo("/flaky")));
	}

	@Test
	void shouldNotTransformPostToGetOn307() throws Exception {
		wiremock.stubFor(post("/redirect307")
				.willReturn(aResponse()
						.withStatus(307)
						.withHeader("Location", "/target")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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

		wiremock.verify(postRequestedFor(urlEqualTo("/redirect307")));
		wiremock.verify(0, getRequestedFor(urlEqualTo("/target")));
	}

	@Test
	void shouldNotAddTransferEncodingAutomatically() throws Exception {
		wiremock.stubFor(post("/chunk")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("chunked body")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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

		wiremock.verify(postRequestedFor(urlEqualTo("/chunk"))
				.withoutHeader("Transfer-Encoding"));
	}

	@Test
	void shouldHandleEmptyBody200() throws Exception {
		// some clients behave differently for Content-Length: 0 vs no body.
		wiremock.stubFor(get("/empty")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Length", "0")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("empty")
					.retrieve(String.class)
					.orNull();

			assertNull(result);
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/empty")));
	}

	@Test
	void shouldHandle204NoContent() throws Exception {
		// 204 must not try to deserialize.
		wiremock.stubFor(get("/no-content")
				.willReturn(aResponse()
						.withStatus(204)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			ApiResponse<?> response = api.client()
					.http()
					.get()
					.path("no-content")
					.retrieve();

			assertNull(response.orNull());
			assertEquals(204, response.getStatus().getCode());
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/no-content")));
	}

	@Test
	void shouldSendContentLengthWhenBodyPresent() throws Exception {
		wiremock.stubFor(post("/length")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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

		wiremock.verify(postRequestedFor(urlEqualTo("/length"))
				.withHeader("Content-Length", equalTo("5")));
	}

	@Test
	void shouldReuseConnectionForMultipleRequests() throws Exception {
		wiremock.stubFor(get("/reuse")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			api.client().http().get().path("reuse").retrieve();
			api.client().http().get().path("reuse").retrieve();
		}

		wiremock.verify(2, getRequestedFor(urlEqualTo("/reuse")));
	}

	@Test
	void shouldRespectCharsetFromContentType() throws Exception {
		wiremock.stubFor(get("/charset")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/plain; charset=ISO-8859-1")
						.withBody(new byte[] { (byte) 0xE9 }))); // é in ISO-8859-1

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("charset")
					.retrieve(String.class)
					.orNull();

			assertEquals("é", result);
		}

		wiremock.verify(1, getRequestedFor(urlEqualTo("/charset")));
	}

	@Test
	void shouldSupportHeadMethod() throws Exception {
		wiremock.stubFor(head(UrlPattern.ANY)
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("X-Test", "42")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			ApiResponse<?> response = api.client()
					.http()
					.head()
					.path("head")
					.retrieve();

			assertNull(response.orNull());
			assertDoesNotThrow(() -> response.orRethrow());
			assertEquals(200, response.getStatus().getCode());
			assertEquals(List.of("42"), response.getHeaders().get("X-Test"));
		}

		wiremock.verify(headRequestedFor(urlEqualTo("/head")));
	}

	@Test
	void shouldSendMultipleHeaderValues() throws Exception {
		wiremock.stubFor(get("/multi")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("multiple headers")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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

		wiremock.verify(getRequestedFor(urlEqualTo("/multi"))
				.withHeader("X-Test", containing("A"))
				.withHeader("X-Test", containing("B")));
	}

	@Test
	void shouldFailOnInvalidJson() throws Exception {
		wiremock.stubFor(get("/bad-json")
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/json")
						.withBody("{ invalid json")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			assertThrows(Exception.class, () -> api.client()
					.http()
					.get()
					.path("bad-json")
					.retrieve(MyDto.class)
					.orRethrow());
		}

		wiremock.verify(1, getRequestedFor(urlEqualTo("/bad-json")));
	}

	@Test
	void shouldRetryAndEventuallySucceed() throws Exception {
		wiremock.stubFor(get("/retry-success")
				.inScenario("retry")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500)
						.withBody("first error response"))
				.willSetStateTo("second"));

		wiremock.stubFor(get("/retry-success")
				.inScenario("retry")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("OK")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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
		wiremock.verify(2, getRequestedFor(urlEqualTo("/retry-success")));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldFailRetryBeforeSendWhenBodyIsNonRepeatable() throws Exception {
		wiremock.stubFor(post("/retry-stream")
				.inScenario("retry-stream")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		wiremock.stubFor(post("/retry-stream")
				.inScenario("retry-stream")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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
		wiremock.verify(1, postRequestedFor(urlEqualTo("/retry-stream"))
				.withRequestBody(equalTo("hello")));
	}

	@Test
	void shouldNotFailRetryBeforeSendWhenBodyIsInputStreamSupplierRepeatable() throws Exception {
		wiremock.stubFor(post("/retry-stream-1")
				.inScenario("retry-stream-1")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		wiremock.stubFor(post("/retry-stream-1")
				.inScenario("retry-stream-1")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("hi")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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
		wiremock.verify(2, postRequestedFor(urlEqualTo("/retry-stream-1"))
				.withRequestBody(equalTo("hello")));
	}

	@Test
	void shouldNotFailRetryBeforeSendWhenBodyIsRepeatableByRecreatingTheInputStream() throws Exception {
		wiremock.stubFor(post("/retry-stream-2")
				.inScenario("retry-stream-2")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		wiremock.stubFor(post("/retry-stream-2")
				.inScenario("retry-stream-2")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("hi")));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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
		wiremock.verify(2, postRequestedFor(urlEqualTo("/retry-stream-2"))
				.withRequestBody(equalTo("hello")));
	}

	@Test
	void shouldFailRetryWhenSupplierReturnsSameConsumedInputStream() throws Exception {
		final String expectedBody = "hello world";

		// first request fails
		wiremock.stubFor(post("/retry-stream-3")
				.inScenario("retry-stream-3")
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse()
						.withStatus(500))
				.willSetStateTo("second"));

		// retry succeeds
		wiremock.stubFor(post("/retry-stream-3")
				.inScenario("retry-stream-3")
				.whenScenarioStateIs("second")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("hi")));

		InputStream alreadyOpened =
				new ByteArrayInputStream(expectedBody.getBytes(StandardCharsets.UTF_8));

		Supplier<InputStream> brokenSupplier = () -> alreadyOpened;

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
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
		wiremock.verify(1, postRequestedFor(urlEqualTo("/retry-stream-3"))
				.withRequestBody(equalTo(expectedBody)));

		// retry attempt MUST be broken (stream already consumed)
		wiremock.verify(1, postRequestedFor(urlEqualTo("/retry-stream-3"))
				.withRequestBody(WireMock.notMatching(expectedBody)));

		List<ServeEvent> events =
				wiremock.getAllServeEvents().stream()
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
		wiremock.stubFor(get("/token")
				.willReturn(okJson("{\"auth\":\"token\"}")));
		wiremock.stubFor(get("/session")
				.willReturn(okJson("{\"auth\":\"session\"}")));

		ExchangeClient tokenClient = getClient(AuthenticationType.TOKEN);
		ExchangeClient sessionClient = getClient(AuthenticationType.SESSION);

		ApiClient api = ApiClient.of(baseUrl(),
				ApiClient.with(tokenClient)
						.decoratedWith(CountingHttpExchangeClient.class),
				ApiClient.with(sessionClient)
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

		wiremock.verify(1, getRequestedFor(urlEqualTo("/token")));
		wiremock.verify(1, getRequestedFor(urlEqualTo("/session")));
	}

	@Test
	void shouldHandleConcurrentRequests() throws Exception {
		wiremock.stubFor(get(urlMatching("/concurrent.*"))
				.willReturn(ok()));

		int threadCount = 50;
		ExecutorService exe = Executors.newVirtualThreadPerTaskExecutor();

		ApiClient api = ApiClient.of(baseUrl(),
				ApiClient.with(exchangeClientClass()));
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

		wiremock.verify(50, getRequestedFor(urlMatching("/concurrent/.*")));
	}
}
