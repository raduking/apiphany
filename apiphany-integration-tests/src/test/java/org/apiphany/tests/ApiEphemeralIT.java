package org.apiphany.tests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.Api;
import org.apiphany.client.ExchangeClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * Integration tests for {@link Api}. Each test is a minimal example of one {@link Api#http()} overload.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiEphemeralIT {

	private static final String PATH = "/hello";

	private static final String BODY = "world";

	@RegisterExtension
	static final WireMockExtension wiremock = WireMockExtension.newInstance()
			.options(options().dynamicPort())
			.build();

	@Test
	void shouldRetrieveWithDefaultHttpEntryPoint() {
		stubHello();

		String result = Api.http()
				.get()
				.url(wiremock.baseUrl() + PATH)
				.retrieve(String.class)
				.orRethrow();

		assertThat(result, equalTo(BODY));
		wiremock.verify(getRequestedFor(urlEqualTo(PATH)));
	}

	@Test
	void shouldRetrieveWithExchangeClientBuilder() {
		stubHello();

		String result = Api.http(ExchangeClientBuilder.create().withDefaultClient())
				.get()
				.url(wiremock.baseUrl() + PATH)
				.retrieve(String.class)
				.orRethrow();

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
