package org.apiphany;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
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
}
