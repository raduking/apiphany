package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apiphany.ApiClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.CountingHttpExchangeClient;
import org.apiphany.security.AuthenticationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jayway.jsonpath.JsonPath;

/**
 * Contract tests for authentication-related behavior of the API client.
 *
 * @author Radu Sebastian LAZIN
 */
public interface AuthenticationContract extends ApiphanyContract {

	@DisplayName("Authentication: The client should use different underlying clients for different authentication types")
	@Test
	default void shouldUseDifferentClientForDifferentAuthType() throws Exception {
		// stub two different auth types → e.g. TOKEN and SESSION
		wiremock().stubFor(get("/token")
				.willReturn(okJson("{\"auth\":\"token\"}")));
		wiremock().stubFor(get("/session")
				.willReturn(okJson("{\"auth\":\"session\"}")));

		ExchangeClient tokenClient = getClient(AuthenticationType.TOKEN);
		ExchangeClient sessionClient = getClient(AuthenticationType.SESSION);

		ApiClient api = ApiClient.of(baseUrl(),
				ApiClient.with(tokenClient)
						.properties(clientProperties())
						.decoratedWith(CountingHttpExchangeClient.class),
				ApiClient.with(sessionClient)
						.properties(clientProperties())
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

		wiremock().verify(1, getRequestedFor(urlEqualTo("/token")));
		wiremock().verify(1, getRequestedFor(urlEqualTo("/session")));
	}

	@DisplayName("Authentication: The client should not leak Authorization headers across authentication types")
	@Test
	default void shouldNotLeakAuthorizationHeadersAcrossAuthenticationTypes() throws Exception {
		wiremock().stubFor(get("/token-auth")
				.willReturn(okJson("{\"auth\":\"token\"}")));

		wiremock().stubFor(get("/session-auth")
				.willReturn(okJson("{\"auth\":\"session\"}")));

		ExchangeClient tokenClient = getClient(AuthenticationType.TOKEN);
		ExchangeClient sessionClient = getClient(AuthenticationType.SESSION);

		ApiClient api = ApiClient.of(baseUrl(),
				ApiClient.with(tokenClient)
						.properties(clientProperties()),
				ApiClient.with(sessionClient)
						.properties(clientProperties()));

		try (tokenClient; sessionClient; api) {
			String tokenResult = api.client(AuthenticationType.TOKEN)
					.http()
					.get()
					.path("token-auth")
					.header("Authorization", "Bearer token-123")
					.retrieve(String.class)
					.orNull();

			String sessionResult = api.client(AuthenticationType.SESSION)
					.http()
					.get()
					.path("session-auth")
					.retrieve(String.class)
					.orNull();

			assertEquals("token", JsonPath.parse(tokenResult).read("$.auth"));
			assertEquals("session", JsonPath.parse(sessionResult).read("$.auth"));
		}

		wiremock().verify(getRequestedFor(urlEqualTo("/token-auth"))
				.withHeader("Authorization", equalTo("Bearer token-123")));

		wiremock().verify(getRequestedFor(urlEqualTo("/session-auth"))
				.withoutHeader("Authorization"));
	}
}
