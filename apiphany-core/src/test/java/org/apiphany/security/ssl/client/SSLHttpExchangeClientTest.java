package org.apiphany.security.ssl.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.ssl.KeyStoreType;
import org.apiphany.security.ssl.SSLContextAware;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.utils.security.SSLValues;
import org.apiphany.utils.security.SSLValues.DummySSLExchangeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.lang.Messages;
import org.morphix.lang.resource.ScopedResource;

/**
 * Test class for {@link SSLHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class SSLHttpExchangeClientTest {

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
	void shouldReturnSslAuthenticationType() throws Exception {
		SSLProperties sslProperties = new SSLProperties();
		sslProperties.getKeystore().setLocation(SSLValues.KEYSTORE_PATH);
		sslProperties.getKeystore().setPassword(SSLValues.KEYSTORE_PASSWORD.toCharArray());
		sslProperties.getKeystore().setType(KeyStoreType.JKS);

		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setCustomProperties(sslProperties);

		try (ExchangeClient exchangeClient = new DummySSLExchangeClient(clientProperties)) {
			try (SSLHttpExchangeClient client = new SSLHttpExchangeClient(exchangeClient)) {
				assertThat(client.getAuthenticationType(), equalTo(AuthenticationType.SSL));
			}
		}
	}

	@Test
	void shouldReturnSslPropertiesAndSSLContextFromClientProperties() throws Exception {
		ClientProperties clientProperties = new ClientProperties();
		SSLProperties sslProperties = new SSLProperties();
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);
		clientProperties.setCustomProperties(sslProperties);

		try (ExchangeClient exchangeClient = new DummySSLExchangeClient(clientProperties)) {
			try (SSLHttpExchangeClient client = new SSLHttpExchangeClient(exchangeClient)) {
				SSLProperties result = client.getSslProperties();

				assertThat(result, notNullValue());
				assertThat(client.getSslContext(), notNullValue());
				assertThat(result.getProtocol(), equalTo(SSLProtocol.TLS_1_2));
			}
		}
	}

	@Test
	void shouldReturnSslPropertiesFromClientPropertiesWithKeystore() throws Exception {
		ClientProperties clientProperties = new ClientProperties();
		SSLProperties sslProperties = new SSLProperties();
		sslProperties.getKeystore().setLocation(SSLValues.KEYSTORE_PATH);
		sslProperties.getKeystore().setPassword(SSLValues.KEYSTORE_PASSWORD.toCharArray());
		sslProperties.getKeystore().setType(KeyStoreType.JKS);
		sslProperties.getTruststore().setLocation(SSLValues.TRUSTSTORE_PATH);
		sslProperties.getTruststore().setPassword(SSLValues.TRUSTSTORE_PASSWORD.toCharArray());
		sslProperties.getTruststore().setType(KeyStoreType.JKS);
		clientProperties.setCustomProperties(sslProperties);

		try (ExchangeClient exchangeClient = new DummySSLExchangeClient(clientProperties)) {
			try (SSLHttpExchangeClient client = new SSLHttpExchangeClient(exchangeClient)) {
				SSLProperties result = client.getSslProperties();

				assertThat(result.getKeystore().getLocation(), equalTo(SSLValues.KEYSTORE_PATH));
				assertThat(result.getTruststore().getLocation(), equalTo(SSLValues.TRUSTSTORE_PATH));
			}
		}
	}

	@Test
	void shouldDelegateClientProperties() throws Exception {
		SSLProperties sslProperties = new SSLProperties();
		sslProperties.getKeystore().setLocation(SSLValues.KEYSTORE_PATH);
		sslProperties.getKeystore().setPassword(SSLValues.KEYSTORE_PASSWORD.toCharArray());
		sslProperties.getKeystore().setType(KeyStoreType.JKS);

		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setCustomProperties(sslProperties);

		try (ExchangeClient exchangeClient = new DummySSLExchangeClient(clientProperties)) {
			try (SSLHttpExchangeClient client = new SSLHttpExchangeClient(exchangeClient)) {
				ClientProperties result = client.getClientProperties();

				assertThat(result, sameInstance(clientProperties));
			}
		}
	}

	@Test
	void shouldThrowExceptionIfUnderlyingClientHasNoSSLConfigured() {
		ClientProperties clientProperties = new ClientProperties();

		try (DummySSLExchangeClient exchangeClient = new DummySSLExchangeClient(clientProperties)) {
			IllegalStateException e = assertThrows(IllegalStateException.class, () -> new SSLHttpExchangeClient(exchangeClient));

			assertThat(e.getMessage(),
					equalTo(Messages.message("Underlying exchange client: {}, must have a non-null SSL context", DummySSLExchangeClient.class)));
			assertThat(exchangeClient.isClosed(), equalTo(false));
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCloseManagedUnderlyingClientIfConstructorThrows() {
		ClientProperties clientProperties = new ClientProperties();

		DummySSLExchangeClient exchangeClient = new DummySSLExchangeClient(clientProperties);
		ScopedResource<ExchangeClient> exchangeClientResource = ScopedResource.managed(exchangeClient);

		assertThrows(IllegalStateException.class, () -> new SSLHttpExchangeClient(exchangeClientResource));

		assertThat(exchangeClient.isClosed(), equalTo(true));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotCloseManagedUnderlyingClientIfConstructorThrowsAndCloseThrows() {
		ClientProperties clientProperties = new ClientProperties();

		DummySSLExchangeClient exchangeClient = new DummySSLExchangeClient(clientProperties);
		exchangeClient.throwOnClose();
		ScopedResource<ExchangeClient> exchangeClientResource = ScopedResource.managed(exchangeClient);

		assertThrows(IllegalStateException.class, () -> new SSLHttpExchangeClient(exchangeClientResource));

		assertThat(exchangeClient.isClosed(), equalTo(false));
	}

	@Test
	void shouldThrowExceptionIfUnderlyingClientIsNotSslContextAware() throws Exception {
		try (ExchangeClient exchangeClient = new DummyExchangeClient()) {
			IllegalStateException e = assertThrows(IllegalStateException.class, () -> new SSLHttpExchangeClient(exchangeClient));

			assertThat(e.getMessage(),
					equalTo(Messages.message("Underlying exchange client: {}, must be SSL-configured and must implement: {}",
							DummyExchangeClient.class, SSLContextAware.class)));
		}
	}
}
