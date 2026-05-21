package org.apiphany.security.ssl.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.ssl.KeyStoreType;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.ssl.StoreInfo;
import org.apiphany.utils.security.SSLValues;
import org.apiphany.utils.security.SSLValues.DummySSLExchangeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.lang.resource.ScopedResource;

/**
 * Test class for {@link SSLHttpExchangeClientBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class SSLHttpExchangeClientBuilderTest {

	private static final String P12_KEYSTORE_PATH = "security/ssl/keystore.p12";
	private static final String P12_KEYSTORE_PASSWORD = "p12password123";

	@Test
	void shouldCreateBuilder() {
		SSLHttpExchangeClientBuilder builder = SSLHttpExchangeClientBuilder.create();

		assertNotNull(builder);
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildSslClientWithKeystoreAndTruststore() throws Exception {
		ClientProperties properties = new ClientProperties();

		SSLHttpExchangeClientBuilder builder = SSLHttpExchangeClientBuilder.create();
		builder.client(DummySSLExchangeClient.class);
		builder.properties(properties);
		builder.keystore(SSLValues.KEYSTORE_PATH, SSLValues.KEYSTORE_PASSWORD);
		builder.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD);
		builder.protocol(SSLProtocol.TLS_1_3);

		try (ScopedResource<ExchangeClient> resource = builder.build()) {
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) resource.unwrap();
			DummySSLExchangeClient delegate = (DummySSLExchangeClient) sslClient.getDelegate().unwrap();

			assertNotNull(sslClient);
			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
			assertNotNull(sslClient.getSslProperties());
			assertThat(delegate.getAuthenticationType(), equalTo(AuthenticationType.NONE));
			assertThat(delegate.getSslContext(), equalTo(sslClient.getSslContext()));
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildSslClientWithKeystoreAndTruststoreWithType() throws Exception {
		ClientProperties properties = new ClientProperties();

		SSLHttpExchangeClientBuilder builder = SSLHttpExchangeClientBuilder.create();
		builder.client(DummySSLExchangeClient.class);
		builder.properties(properties);
		builder.keystore(SSLValues.KEYSTORE_PATH, SSLValues.KEYSTORE_PASSWORD, KeyStoreType.JKS.value());
		builder.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD, KeyStoreType.JKS.value());
		builder.protocol(SSLProtocol.TLS_1_3);

		try (ScopedResource<ExchangeClient> resource = builder.build()) {
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) resource.unwrap();
			DummySSLExchangeClient delegate = (DummySSLExchangeClient) sslClient.getDelegate().unwrap();

			assertNotNull(sslClient);
			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
			assertNotNull(sslClient.getSslProperties());
			assertThat(delegate.getAuthenticationType(), equalTo(AuthenticationType.NONE));
			assertThat(delegate.getSslContext(), equalTo(sslClient.getSslContext()));
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildSslClientWithOnlyTruststore() throws Exception {
		ClientProperties properties = new ClientProperties();

		SSLHttpExchangeClientBuilder builder = SSLHttpExchangeClientBuilder.create();
		builder.client(DummySSLExchangeClient.class);
		builder.properties(properties);
		builder.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD);

		try (ScopedResource<ExchangeClient> resource = builder.build()) {
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) resource.unwrap();
			DummySSLExchangeClient delegate = (DummySSLExchangeClient) sslClient.getDelegate().unwrap();

			assertNotNull(sslClient);
			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
			assertNotNull(sslClient.getSslProperties());
			assertThat(delegate.getAuthenticationType(), equalTo(AuthenticationType.NONE));
			assertThat(delegate.getSslContext(), equalTo(sslClient.getSslContext()));
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotBuildSslClientWithKeystoreAndTruststoreWhenClientIsAlreadyBuilt() {
		ClientProperties properties = new ClientProperties();
		ExchangeClient exchangeClient = mock(ExchangeClient.class);
		doReturn(properties).when(exchangeClient).getClientProperties();

		SSLHttpExchangeClientBuilder builder = SSLHttpExchangeClientBuilder.create();
		builder.client(exchangeClient);
		builder.keystore(SSLValues.KEYSTORE_PATH, SSLValues.KEYSTORE_PASSWORD);
		builder.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD);
		builder.protocol(SSLProtocol.TLS_1_3);

		IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

		assertThat(exception.getMessage(),
				equalTo("Cannot build SSL exchange client: the underlying client was built without the builder SSL properties configured,"
						+ " but this builder has SSL properties set. Please ensure the underlying client is built with SSL properties"
						+ " or remove SSL configuration from this builder."));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildSslClientWithClientPropertiesSSLConfiguration() throws Exception {
		ClientProperties properties = new ClientProperties();
		SSLProperties sslProperties = new SSLProperties();
		sslProperties.setKeystore(StoreInfo.of(SSLValues.KEYSTORE_PATH, SSLValues.KEYSTORE_PASSWORD.toCharArray()));
		sslProperties.setTruststore(StoreInfo.of(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD.toCharArray()));
		sslProperties.setProtocol(SSLProtocol.TLS_1_2);
		properties.setCustomProperties(sslProperties);

		SSLHttpExchangeClientBuilder builder = SSLHttpExchangeClientBuilder.create();
		builder.client(DummySSLExchangeClient.class);
		builder.properties(properties);

		try (ScopedResource<ExchangeClient> resource = builder.build()) {
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) resource.unwrap();
			DummySSLExchangeClient delegate = (DummySSLExchangeClient) sslClient.getDelegate().unwrap();

			assertNotNull(sslClient);
			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
			assertNotNull(sslClient.getSslProperties());
			assertThat(delegate.getAuthenticationType(), equalTo(AuthenticationType.NONE));
			assertThat(delegate.getSslContext(), equalTo(sslClient.getSslContext()));
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildSslClientWithNoSSLPropertiesConfiguredAndUseDefaultJavaSSLContext() throws Exception {
		ClientProperties properties = new ClientProperties();

		SSLHttpExchangeClientBuilder builder = SSLHttpExchangeClientBuilder.create();
		builder.client(DummySSLExchangeClient.class);
		builder.properties(properties);

		ScopedResource<ExchangeClient> resource = builder.build();

		try (resource) {
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) resource.unwrap();
			DummySSLExchangeClient delegate = (DummySSLExchangeClient) sslClient.getDelegate().unwrap();

			assertNotNull(sslClient);
			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
			assertNotNull(sslClient.getSslContext());
			assertThat(delegate.getAuthenticationType(), equalTo(AuthenticationType.NONE));
		}
	}

	@Test
	void shouldRequireKeystoreThrowsWhenNoKeystoreConfigured() {
		SSLHttpExchangeClientBuilder builder = SSLHttpExchangeClientBuilder.create();

		IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> SSLHttpExchangeClientBuilder.requireKeystore(builder));

		assertThat(exception.getMessage(), equalTo("Keystore must be configured for mutual TLS (mTLS). "
				+ "Use keystore(path, password) to configure a client certificate."));
	}

	@Test
	void shouldNotRequireKeystoreThrowsWhenKeystoreConfigured() {
		SSLHttpExchangeClientBuilder builder = SSLHttpExchangeClientBuilder.create()
				.keystore(SSLValues.KEYSTORE_PATH, SSLValues.KEYSTORE_PASSWORD);

		assertDoesNotThrow(() -> SSLHttpExchangeClientBuilder.requireKeystore(builder));
	}

	@Test
	void shouldBuildSslClientThroughExchangeClientBuilderChain() throws Exception {
		ClientProperties properties = new ClientProperties();

		ScopedResource<ExchangeClient> resource = ExchangeClientBuilder.create()
				.client(DummySSLExchangeClient.class)
				.properties(properties)
				.securedWith()
				.ssl(ssl -> ssl
						.keystore(SSLValues.KEYSTORE_PATH, SSLValues.KEYSTORE_PASSWORD)
						.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD))
				.build();

		try (resource) {
			@SuppressWarnings("resource")
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) resource.unwrap();

			assertNotNull(sslClient);
			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
		} finally {
			resource.close();
		}
	}

	@Test
	void shouldBuildSslClientThroughExchangeClientBuilderChainWithoutClientPropertiesSet() throws Exception {
		ScopedResource<ExchangeClient> resource = ExchangeClientBuilder.create()
				.client(DummySSLExchangeClient.class)
				.securedWith()
				.ssl(ssl -> ssl
						.keystore(SSLValues.KEYSTORE_PATH, SSLValues.KEYSTORE_PASSWORD)
						.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD))
				.build();

		try (resource) {
			@SuppressWarnings("resource")
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) resource.unwrap();

			assertNotNull(sslClient);
			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
		} finally {
			resource.close();
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildAndNotChangeDelegateAuthenticationType() throws Exception {
		ClientProperties properties = new ClientProperties();

		ScopedResource<ExchangeClient> resource = ExchangeClientBuilder.create()
				.client(DummySSLExchangeClient.class)
				.properties(properties)
				.securedWith()
				.ssl(ssl -> ssl
						.keystore(SSLValues.KEYSTORE_PATH, SSLValues.KEYSTORE_PASSWORD)
						.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD))
				.build();

		try (resource) {
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) resource.unwrap();
			DummySSLExchangeClient delegate = (DummySSLExchangeClient) sslClient.getDelegate().unwrap();

			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
			assertThat(delegate.getAuthenticationType(), equalTo(AuthenticationType.NONE));
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldBuildWithPkcs12Keystore() throws Exception {
		ClientProperties properties = new ClientProperties();

		SSLHttpExchangeClientBuilder builder = SSLHttpExchangeClientBuilder.create();
		builder.client(DummySSLExchangeClient.class);
		builder.properties(properties);
		builder.keystore(P12_KEYSTORE_PATH, P12_KEYSTORE_PASSWORD);
		builder.truststore(SSLValues.TRUSTSTORE_PATH, SSLValues.TRUSTSTORE_PASSWORD);

		try (ScopedResource<ExchangeClient> resource = builder.build()) {
			SSLHttpExchangeClient sslClient = (SSLHttpExchangeClient) resource.unwrap();
			DummySSLExchangeClient delegate = (DummySSLExchangeClient) sslClient.getDelegate().unwrap();

			assertNotNull(sslClient);
			assertThat(sslClient.getAuthenticationType(), equalTo(AuthenticationType.SSL));
			assertNotNull(sslClient.getSslProperties());
			assertThat(delegate.getAuthenticationType(), equalTo(AuthenticationType.NONE));
			assertThat(delegate.getSslContext(), equalTo(sslClient.getSslContext()));
		}
	}
}
