package org.apiphany.tests;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.security.AuthenticationType;
import org.apiphany.tests.contract.ApiphanyContract;
import org.apiphany.tests.contract.AuthenticationContract;
import org.apiphany.tests.contract.BasicContract;
import org.apiphany.tests.contract.CompressionContract;
import org.apiphany.tests.contract.ConcurrencyContract;
import org.apiphany.tests.contract.ConnectionContract;
import org.apiphany.tests.contract.ContentContract;
import org.apiphany.tests.contract.ErrorsContract;
import org.apiphany.tests.contract.HeadersContract;
import org.apiphany.tests.contract.JsonContract;
import org.apiphany.tests.contract.RedirectsContract;
import org.apiphany.tests.contract.RetryContract;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * Test class with WireMock for {@link ApiClient} using {@link JavaNetHttpExchangeClient} and serves as a base class for
 * all other ApiClient integration tests that use other http clients.
 * <p>
 * The main purpose of this class is to verify that the ApiClient works correctly with Java's built-in HttpClient as the
 * underlying HTTP client, since this is what the ApiClient uses by default if no other HTTP client is available on the
 * classpath. This ensures that the ApiClient can be used in environments where no third-party HTTP client libraries are
 * available, such as in a Java SE environment without additional dependencies.
 * <p>
 * For other clients, we can extend this class and override the {@link #exchangeClientClass()} to return the appropriate
 * client class, {@link #getClient(AuthenticationType)} method to return an instance of that client with the specified
 * authentication type, and then we can reuse all the tests defined in this class to verify that the ApiClient works
 * correctly with that client as well.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientWithDefaultClientIT implements ApiphanyContract {

	@RegisterExtension
	private static final WireMockExtension wiremock =
			WireMockExtension.newInstance()
					.options(options().dynamicPort())
					.build();

	@Override
	public WireMockExtension wiremock() {
		return wiremock;
	}

	abstract class NestedContract implements ApiphanyContract {

		@Override
		public WireMockExtension wiremock() {
			return ApiClientWithDefaultClientIT.this.wiremock();
		}

		@Override
		public Class<? extends ExchangeClient> exchangeClientClass() {
			return ApiClientWithDefaultClientIT.this.exchangeClientClass();
		}

		@Override
		public ExchangeClient getClient(final AuthenticationType authType) {
			return ApiClientWithDefaultClientIT.this.getClient(authType);
		}

		@Override
		public ClientProperties clientProperties() {
			return ApiClientWithDefaultClientIT.this.clientProperties();
		}

		@Override
		public boolean enableRedirects() {
			return ApiClientWithDefaultClientIT.this.enableRedirects();
		}

		@Override
		public ApiClient apiClient() {
			return ApiClientWithDefaultClientIT.this.apiClient();
		}
	}

	@Nested
	class Basic extends NestedContract implements BasicContract {
		// empty - inherits all tests from BasicContract
	}

	@Nested
	class Headers extends NestedContract implements HeadersContract {
		// empty - inherits all tests from HeadersContract
	}

	@Nested
	class Redirects extends NestedContract implements RedirectsContract {
		// empty - inherits all tests from RedirectsContract
	}

	@Nested
	class Retries extends NestedContract implements RetryContract {
		// empty - inherits all tests from RetryContract
	}

	@Nested
	class Errors extends NestedContract implements ErrorsContract {
		// empty - inherits all tests from ErrorsContract
	}

	@Nested
	class Authentication extends NestedContract implements AuthenticationContract {
		// empty - inherits all tests from AuthenticationContract
	}

	@Nested
	class Content extends NestedContract implements ContentContract {
		// empty - inherits all tests from ContentContract
	}

	@Nested
	class Json extends NestedContract implements JsonContract {
		// empty - inherits all tests from JsonContract
	}

	@Nested
	class Compression extends NestedContract implements CompressionContract {
		// empty - inherits all tests from CompressionContract
	}

	@Nested
	class Concurrency extends NestedContract implements ConcurrencyContract {
		// empty - inherits all tests from ConcurrencyContract
	}

	@Nested
	class Connection extends NestedContract implements ConnectionContract {
		// empty - inherits all tests from ConnectionContract
	}
}
