package org.apiphany.tests;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apiphany.ApiClient;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.tests.contract.ApiphanyContract;
import org.apiphany.tests.contract.RedirectsContract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.client.WireMock;
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

		@DisplayName("Redirects: With redirects enabled, client should follow 302")
		@Test
		@Override
		public void shouldNotFollowRedirectsByDefault() throws Exception {
			wiremock().stubFor(WireMock.get("/redirect")
					.willReturn(WireMock.aResponse()
							.withStatus(302)
							.withHeader("Location", "/target")));

			wiremock().stubFor(WireMock.get("/target")
					.willReturn(WireMock.aResponse()
							.withStatus(200)
							.withBody("OK")));

			ApiClient api = apiClient();
			try (api) {
				String result = api.client()
						.http()
						.get()
						.path("redirect")
						.retrieve(String.class)
						.orNull();

				assertEquals("OK", result);
			}
		}

		@DisplayName("Redirects: With redirects enabled, 307 should preserve method and body")
		@Test
		@Override
		public void shouldNotTransformPostToGetOn307() throws Exception {
			wiremock().stubFor(WireMock.post("/redirect307")
					.willReturn(WireMock.aResponse()
							.withStatus(307)
							.withHeader("Location", "/target")));

			wiremock().stubFor(WireMock.post("/target")
					.willReturn(WireMock.aResponse()
							.withStatus(200)
							.withBody("OK")));

			ApiClient api = apiClient();
			try (api) {
				String result = api.client()
						.http()
						.post()
						.path("redirect307")
						.body("test")
						.retrieve(String.class)
						.orNull();

				assertEquals("OK", result);
			}
		}

		@DisplayName("Redirects: The client should fail when redirects exceed the maximum limit")
		@Test
		@Override
		public void shouldFailOnRedirectLoop() throws Exception {
			wiremock().stubFor(WireMock.get("/loop")
					.willReturn(WireMock.aResponse()
							.withStatus(302)
							.withHeader("Location", "/loop")));

			ApiClient api = apiClient();
			try (api) {
				ApiResponse<String> response = api.client()
						.http()
						.get()
						.path("loop")
						.retrieve(String.class);

				assertNotEquals(200, response.getStatus().getCode());
			}
		}
	}
}
