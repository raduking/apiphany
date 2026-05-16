package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import org.apiphany.ApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for connection management. These tests verify that the client reuses connections when possible, which
 * is important for performance.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ConnectionContract extends ApiphanyContract {

	@DisplayName("Connection: The client should reuse the same connection for multiple requests to the same host")
	@Test
	default void shouldReuseConnectionForMultipleRequests() throws Exception {
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
}
