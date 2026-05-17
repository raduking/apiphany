package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;

import org.apiphany.ApiClient;
import org.apiphany.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for HTTP redirects. These tests verify that the client correctly handles HTTP redirects (3xx)
 * according to the HTTP specification.
 *
 * @author Radu Sebastian LAZIN
 */
public interface RedirectsContract extends ApiphanyContract {

	@DisplayName("Redirects: By default, the client should not follow redirects")
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

	@DisplayName("Redirects: The client should follow redirects when enabled")
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

	@DisplayName("Redirects: The client should not transform POST to GET on 307 redirect")
	@Test
	default void shouldNotTransformPostToGetOn307() throws Exception {
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
}
