package org.apiphany.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.Api;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * Integration tests for {@link Api}. Each test is a minimal example of one {@link Api#http()} overload.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiIT {

	private static final String PATH = "/hello";

	private static final String BODY = "world";

	@RegisterExtension
	static final WireMockExtension wiremock = WireMockExtension.newInstance()
			.options(options().dynamicPort())
			.build();

	ExchangeClient client() {
		return new JavaNetHttpExchangeClient();
	}

	@Test
	void shouldRetrieveWithExistingExchangeClient() throws Exception {
		stubHello();

		ExchangeClient client = client();

		String result = Api.http(client)
				.get()
				.url(wiremock.baseUrl() + PATH)
				.retrieve(String.class)
				.orRethrow();

		client.close();

		assertThat(result, equalTo(BODY));
		wiremock.verify(getRequestedFor(urlEqualTo(PATH)));
	}

	private static void stubHello() {
		wiremock.stubFor(get(PATH)
				.willReturn(aResponse()
						.withStatus(200)
						.withBody(BODY)));
	}
}
