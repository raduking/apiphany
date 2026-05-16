package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apiphany.ApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for content handling. These tests verify that the client correctly handles different content types and
 * charsets.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ContentContract extends ApiphanyContract {

	@DisplayName("Charset: The client should respect the charset specified in the Content-Type header when decoding the response body")
	@Test
	default void shouldRespectCharsetFromContentType() throws Exception {
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
}
