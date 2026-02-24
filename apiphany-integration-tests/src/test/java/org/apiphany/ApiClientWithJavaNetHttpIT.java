package org.apiphany;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.http.ContentEncoding;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.io.gzip.GZip;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * Test class for {@link ApiClient} using {@link JavaNetHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientWithJavaNetHttpIT {

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
		// properties.setFollowRedirects(true);
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
				.willReturn(aResponse().withStatus(200)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			api.client()
					.http()
					.get()
					.path("query")
					.param("foo", "bar")
					.retrieve()
					.orNull();
		}

		wiremock.verify(getRequestedFor(urlPathEqualTo("/query"))
				.withQueryParam("foo", equalTo("bar")));
	}

	static record MyDto(String name) {
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
			assertThrows(HttpException.class, () -> api.client()
					.http()
					.get()
					.path("slow")
					.retrieve()
					.orRethrow());
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/slow")));
	}

	@Test
	void shouldNotSendContentTypeWithoutBody() throws Exception {
		// Some clients will add a "Content-Type: text/plain" header by default even for empty bodies, but Java's HttpClient
		// does not, so we want to ensure that the ApiClient doesn't add it either.
		wiremock.stubFor(post("/no-body")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			api.client()
					.http()
					.post()
					.path("no-body")
					.retrieve()
					.orNull();
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
						.withStatus(200)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			api.client()
					.http()
					.get()
					.path("accept")
					.retrieve()
					.orNull();
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
						.withStatus(200)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			api.client()
					.http()
					.get()
					.path("encoding")
					.retrieve()
					.orNull();
		}

		wiremock.verify(getRequestedFor(urlEqualTo("/encoding"))
				.withoutHeader("Accept-Encoding"));
	}

	@Test
	void shouldPreserveCustomHeader() throws Exception {
		// OkHttp loves normalizing header names to lower case, so we want to ensure that the ApiClient preserves the case of
		// custom headers even if the underlying client doesn't.
		wiremock.stubFor(get("/case")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			api.client()
					.http()
					.get()
					.path("case")
					.header("X-CuStOm-HeAdEr", "42")
					.retrieve();
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
						.withStatus(500)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			api.client()
					.http()
					.get()
					.path("flaky")
					.retrieve()
					.orNull();
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
			api.client()
					.http()
					.post()
					.path("redirect307")
					.body("test")
					.retrieve()
					.orNull();
		}

		wiremock.verify(postRequestedFor(urlEqualTo("/redirect307")));
		wiremock.verify(0, getRequestedFor(urlEqualTo("/target")));
	}

	@Test
	void shouldNotAddTransferEncodingAutomatically() throws Exception {
		wiremock.stubFor(post("/chunk")
				.willReturn(aResponse()
						.withStatus(200)));

		ApiClient api = ApiClient.of(baseUrl(), ApiClient.with(exchangeClientClass()));
		try (api) {
			api.client()
					.http()
					.post()
					.path("chunk")
					.body("hello")
					.retrieve();
		}

		wiremock.verify(postRequestedFor(urlEqualTo("/chunk"))
				.withoutHeader("Transfer-Encoding"));
	}
}
