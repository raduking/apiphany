package org.apiphany.security.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.ssl.SSLContextAware;
import org.apiphany.security.ssl.client.SSLHttpExchangeClient;
import org.apiphany.utils.security.SSLValues;
import org.apiphany.utils.security.SSLValues.BadSSLExchangeClient;
import org.apiphany.utils.security.SSLValues.DummySSLExchangeClient;
import org.junit.jupiter.api.Test;
import org.morphix.lang.Messages;
import org.morphix.lang.resource.ScopedResource;

/**
 * Test class for {@link SecuredExchangeClientBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class SecuredExchangeClientBuilderTest {

	public static class DummyExchangeClient implements ExchangeClient {

		public DummyExchangeClient() {
			// empty
		}

		@SuppressWarnings("unused")
		public DummyExchangeClient(final ClientProperties clientProperties) {
			// empty
		}

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

	@Test
	@SuppressWarnings("resource")
	void shouldBuildExchangeClientWithSslSecurity() throws Exception {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummySSLExchangeClient.class)
				.properties(new ClientProperties())
				.securedWith()
				.ssl();

		try (ScopedResource<ExchangeClient> exchangeClient = builder.build()) {
			assertNotNull(exchangeClient);
			assertThat(exchangeClient.unwrap().getAuthenticationType(), equalTo(AuthenticationType.SSL));
		}
	}

	@Test
	void shouldNotBuildExchangeClientWithSslSecurityIfTheUnderlyingExchangeClientDoesNotSupportIt() {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.securedWith()
				.ssl();

		IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);

		assertThat(e.getMessage(), equalTo(
				Messages.message("Underlying exchange client: {}, must be SSL-configured and must implement: {}",
						DummyExchangeClient.class, SSLContextAware.class)));
	}

	@Test
	void shouldNotBuildExchangeClientWithSslSecurityIfTheUnderlyingExchangeClientDoesNotConfigureIt() {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(BadSSLExchangeClient.class)
				.securedWith()
				.ssl();

		IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);

		assertThat(e.getMessage(),
				equalTo(Messages.message("Underlying exchange client: {}, must have a non-null SSL context", BadSSLExchangeClient.class)));
	}

	@Test
	void shouldBuildExchangeClientWithSslCustomizer() throws Exception {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummySSLExchangeClient.class)
				.securedWith()
				.ssl(ssl -> ssl
						.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD));

		try (ScopedResource<ExchangeClient> exchangeClient = builder.build()) {
			@SuppressWarnings("resource")
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) exchangeClient.unwrap();

			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
		}
	}

	@Test
	void shouldBuildExchangeClientWithMtlsCustomizer() throws Exception {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummySSLExchangeClient.class)
				.securedWith()
				.mtls(mtls -> mtls
						.keystore(SSLValues.KEYSTORE_PATH, SSLValues.KEYSTORE_PASSWORD)
						.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD));

		try (ScopedResource<ExchangeClient> exchangeClient = builder.build()) {
			@SuppressWarnings("resource")
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) exchangeClient.unwrap();

			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
		}
	}

	@Test
	void shouldNotBuildExchangeClientWithMtlsCustomizerWhenTheUnderlyingClientDoesNotSupportIt() throws Exception {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.securedWith()
				.mtls(mtls -> mtls
						.keystore(SSLValues.KEYSTORE_PATH, SSLValues.KEYSTORE_PASSWORD)
						.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD));

		IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);

		assertThat(e.getMessage(), equalTo(
				Messages.message("Underlying exchange client: {}, must be SSL-configured and must implement: {}",
						DummyExchangeClient.class, SSLContextAware.class)));
	}

	@Test
	void shouldThrowExceptionWhenMtlsWithoutKeystore() {
		SecuredExchangeClientBuilder builder = SecuredExchangeClientBuilder.create();
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> builder.mtls(mtls -> mtls.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD)));

		assertThat(e.getMessage(), equalTo("Keystore must be configured for mutual TLS (mTLS). "
				+ "Use keystore(path, password) to configure a client certificate."));
	}
}
