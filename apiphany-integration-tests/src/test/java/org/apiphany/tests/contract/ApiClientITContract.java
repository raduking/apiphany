package org.apiphany.tests.contract;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.security.AuthenticationType;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * Integration test interface for testing {@link ApiClient}. By default, the tests will use the
 * {@link JavaNetHttpExchangeClient} as the exchange client implementation, but this can be overridden by implementing
 * classes to test different exchange client implementations.
 * <p>
 * The tests will also use a WireMock server to mock the API responses and verify the behavior of the API client under
 * different scenarios.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ApiClientITContract {

	WireMockExtension wiremock();

	default String baseUrl() {
		return wiremock().getRuntimeInfo().getHttpBaseUrl();
	}

	default Class<? extends ExchangeClient> exchangeClientClass() {
		return JavaNetHttpExchangeClient.class;
	}

	default ExchangeClient getClient(final AuthenticationType authType) {
		return new JavaNetHttpExchangeClient() {
			@Override
			public AuthenticationType getAuthenticationType() {
				return authType;
			}
		};
	}

	default boolean enableRedirects() {
		return false;
	}

	default ClientProperties clientProperties() {
		return new ClientProperties();
	}

	default ApiClient apiClient() {
		return ApiClient.of(baseUrl(), ApiClient
				.with(exchangeClientClass())
				.properties(clientProperties()));
	}
}
