package org.apiphany.tests;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.tests.contract.ApiphanyContract;
import org.apiphany.tests.contract.RedirectsContract;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * Redirect-focused integration tests for {@link ApiClient} using the default exchange client with redirects enabled.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientWithDefaultClientRedirectsIT implements ApiphanyContract {

	@RegisterExtension
	private static final WireMockExtension wiremock =
			WireMockExtension.newInstance()
					.options(options()
							.dynamicPort())
					.build();

	@Override
	public WireMockExtension wiremock() {
		return wiremock;
	}

	@Override
	public boolean enableRedirects() {
		return true;
	}

	@Override
	public ClientProperties clientProperties() {
		ClientProperties properties = ApiphanyContract.super.clientProperties();
		properties.getConnection().setFollowRedirects(true);
		return properties;
	}

	private abstract class NestedContract implements ApiphanyContract {

		@Override
		public WireMockExtension wiremock() {
			return ApiClientWithDefaultClientRedirectsIT.this.wiremock();
		}

		@Override
		public boolean enableRedirects() {
			return ApiClientWithDefaultClientRedirectsIT.this.enableRedirects();
		}

		@Override
		public ClientProperties clientProperties() {
			return ApiClientWithDefaultClientRedirectsIT.this.clientProperties();
		}

		@Override
		public ApiClient apiClient() {
			return ApiClientWithDefaultClientRedirectsIT.this.apiClient();
		}

		@Override
		public ApiClient apiClient(final ClientProperties properties) {
			return ApiClientWithDefaultClientRedirectsIT.this.apiClient(properties);
		}
	}

	@Nested
	class Redirects extends NestedContract implements RedirectsContract {
		// empty - inherits all tests from RedirectsContract
	}
}
