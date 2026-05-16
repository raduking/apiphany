package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.apiphany.ApiClient;
import org.junit.jupiter.api.Test;

public interface RedirectsContract extends ApiClientITContract {

	@Test
	default void shouldNotFollowRedirectsByDefault() throws Exception {
		wiremock().stubFor(get("/redirect")
				.willReturn(aResponse()
						.withStatus(302)
						.withHeader("Location", "/target")));

		wiremock().stubFor(get("/target")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("OK")));

		ApiClient api = apiClient();
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
		wiremock().verify(1, getRequestedFor(urlEqualTo("/redirect")));

		// MUST NOT have followed redirect
		wiremock().verify(0, getRequestedFor(urlEqualTo("/target")));
	}

	@Test
	default void shouldFollowRedirectsWhenEnabled() throws Exception {
		assumeTrue(enableRedirects(), "This client does not support redirects");

		wiremock().stubFor(get("/redirect")
				.willReturn(aResponse()
						.withStatus(302)
						.withHeader("Location", "/target")));

		wiremock().stubFor(get("/target")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("OK")));

		ApiClient api = apiClient();
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

		wiremock().verify(getRequestedFor(urlEqualTo("/redirect")));
		wiremock().verify(getRequestedFor(urlEqualTo("/target")));
	}
}
