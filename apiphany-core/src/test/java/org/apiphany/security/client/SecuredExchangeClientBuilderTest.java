package org.apiphany.security.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.ssl.client.SSLHttpExchangeClient;
import org.junit.jupiter.api.Test;
import org.morphix.lang.resource.ScopedResource;

/**
 * Test class for {@link SecuredExchangeClientBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class SecuredExchangeClientBuilderTest {

	private static final String KEYSTORE_PATH = "security/ssl/keystore.jks";
	private static final String KEYSTORE_PASSWORD = "keystorepassword123";
	private static final String TRUSTSTORE_PATH = "security/ssl/truststore.jks";
	private static final String TRUSTSTORE_PASSWORD = "truststorepassword123";

	public static class DummyExchangeClient implements ExchangeClient {

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
			return null;
		}

		@Override
		public void close() {
			// empty
		}
	}

	@Test
	void shouldBuildExchangeClientBuilder() {
		SecuredExchangeClientBuilder builder = SecuredExchangeClientBuilder.create();

		assertNotNull(builder);
	}

	@Test
	void shouldThrowExceptionIfClientIsNotSecuredWithAnyMechanism() {
		SecuredExchangeClientBuilder builder = SecuredExchangeClientBuilder.create();
		IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);

		assertThat(e.getMessage(), equalTo("Client not secured with any mechanism"));
	}

	@Test
	void shouldBuildExchangeClientWithOAuth2Security() throws Exception {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.securedWith()
				.oAuth2();

		try (ScopedResource<ExchangeClient> exchangeClient = builder.build()) {
			assertNotNull(exchangeClient);
		}
	}

	@Test
	void shouldThrowExceptionIfClientClassIsSetWithoutSecurityMechanism() {
		SecuredExchangeClientBuilder builder = SecuredExchangeClientBuilder.create();
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> builder.client(DummyExchangeClient.class));

		assertThat(e.getMessage(), equalTo("Cannot set exchange client class when securing an existing client"));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldThrowExceptionIfClientIsSetWithoutSecurityMechanism() {
		SecuredExchangeClientBuilder builder = SecuredExchangeClientBuilder.create();
		DummyExchangeClient exchangeClient = new DummyExchangeClient();
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> builder.client(exchangeClient));

		assertThat(e.getMessage(), equalTo("Cannot set exchange client when securing an existing client"));
	}

	@SuppressWarnings("resource")
	@Test
	void shouldBuildExchangeClientWithSslSecurity() throws Exception {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.securedWith()
				.ssl();

		try (ScopedResource<ExchangeClient> exchangeClient = builder.build()) {
			assertNotNull(exchangeClient);
			assertThat(exchangeClient.unwrap().getAuthenticationType(), equalTo(AuthenticationType.SSL));
		}
	}

	@Test
	void shouldBuildExchangeClientWithSslCustomizer() throws Exception {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.securedWith()
				.ssl(ssl -> ssl
						.truststore(TRUSTSTORE_PATH, TRUSTSTORE_PASSWORD));

		try (ScopedResource<ExchangeClient> exchangeClient = builder.build()) {
			@SuppressWarnings("resource")
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) exchangeClient.unwrap();

			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
		}
	}

	@Test
	void shouldBuildExchangeClientWithMtlsCustomizer() throws Exception {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.securedWith()
				.mtls(mtls -> mtls
						.keystore(KEYSTORE_PATH, KEYSTORE_PASSWORD)
						.truststore(TRUSTSTORE_PATH, TRUSTSTORE_PASSWORD));

		try (ScopedResource<ExchangeClient> exchangeClient = builder.build()) {
			@SuppressWarnings("resource")
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) exchangeClient.unwrap();

			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
		}
	}

	@Test
	void shouldThrowExceptionWhenMtlsWithoutKeystore() {
		SecuredExchangeClientBuilder builder = SecuredExchangeClientBuilder.create();
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> builder.mtls(mtls -> mtls.truststore(TRUSTSTORE_PATH, TRUSTSTORE_PASSWORD)));

		assertThat(e.getMessage(), equalTo("Keystore must be configured for mutual TLS (mTLS). "
				+ "Use keystore(path, password) to configure a client certificate."));
	}
}
