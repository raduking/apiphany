package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apiphany.ApiClient;
import org.apiphany.ApiResponse;
import org.apiphany.http.HttpStatus;
import org.apiphany.io.IOStreams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for response streaming behavior.
 *
 * @author Radu Sebastian LAZIN
 */
public interface StreamContract extends ApiphanyContract {

	@DisplayName("Stream: The client should retrieve stream responses as InputStream")
	@Test
	default void shouldRetrieveStreamResponseAsInputStream() throws Exception {
		wiremock().stubFor(get(urlEqualTo("/stream"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/plain")
						.withBody("stream-body")));

		ApiClient apiClient = apiClient();
		try (apiClient) {
			ApiResponse<InputStream> response = apiClient.client()
					.http()
					.get()
					.path("stream")
					.stream()
					.retrieve(InputStream.class);

			assertThat(response.getStatus(), equalTo(HttpStatus.OK));
			InputStream body = response.orNull();
			assertNotNull(body);
			try (body) {
				assertThat(new String(IOStreams.toByteArray(body), StandardCharsets.UTF_8), equalTo("stream-body"));
			}
		}
	}
}
