package org.apiphany.tests.security;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.ssl.KeyStoreType;
import org.apiphany.test.Tests;
import org.apiphany.tests.contract.ApiphanyContract;
import org.apiphany.tests.contract.TLSContract;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * Security integration test class for {@link ApiClient} using {@link JavaNetHttpExchangeClient} and serves as a base
 * class for all other ApiClient integration tests that use other http clients with TLS support.
 * <p>
 * For other clients, we can extend this class and override the {@link #exchangeClientClass()} to return the appropriate
 * client class, {@link #getClient(AuthenticationType)} method to return an instance of that client with the specified
 * authentication type, and then we can reuse all the tests defined in this class to verify that the ApiClient works
 * correctly with that client as well.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientWithDefaultClientSecurityIT implements ApiphanyContract {

	@RegisterExtension
	private static final WireMockExtension wiremock =
			WireMockExtension.newInstance()
					.options(options()
							.dynamicHttpsPort()
							.keystorePath(
									Tests.resourcePath(ApiClientWithDefaultClientSecurityIT.class, "/security/ssl/server-keystore.jks").toString())
							.keystorePassword("serverkeystorepass123")
							.keyManagerPassword("serverkeystorepass123")
							.keystoreType(KeyStoreType.JKS.value())
							.trustStorePath(
									Tests.resourcePath(ApiClientWithDefaultClientSecurityIT.class, "/security/ssl/server-truststore.jks").toString())
							.trustStorePassword("servertruststorepass123")
							.trustStoreType(KeyStoreType.JKS.value()))
					.build();

	@Override
	public String baseUrl() {
		return wiremock().getRuntimeInfo().getHttpsBaseUrl();
	}

	@Override
	public WireMockExtension wiremock() {
		return wiremock;
	}

	private abstract class NestedContract implements ApiphanyContract {

		@Override
		public WireMockExtension wiremock() {
			return ApiClientWithDefaultClientSecurityIT.this.wiremock();
		}

		@Override
		public Class<? extends ExchangeClient> exchangeClientClass() {
			return ApiClientWithDefaultClientSecurityIT.this.exchangeClientClass();
		}

		@Override
		public ExchangeClient getClient(final AuthenticationType authType) {
			return ApiClientWithDefaultClientSecurityIT.this.getClient(authType);
		}

		@Override
		public ClientProperties clientProperties() {
			return ApiClientWithDefaultClientSecurityIT.this.clientProperties();
		}

		@Override
		public boolean enableRedirects() {
			return ApiClientWithDefaultClientSecurityIT.this.enableRedirects();
		}

		@Override
		public ApiClient apiClient() {
			return ApiClientWithDefaultClientSecurityIT.this.apiClient();
		}

		@Override
		public ApiClient apiClient(final ClientProperties properties) {
			return ApiClientWithDefaultClientSecurityIT.this.apiClient(properties);
		}
	}

	@Nested
	class TLS extends NestedContract implements TLSContract {
		// empty - inherits all tests from BasicContract
	}
}
