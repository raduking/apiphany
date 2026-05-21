package org.apiphany.utils.security;

import javax.net.ssl.SSLContext;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.ssl.SSLContextAware;
import org.apiphany.security.ssl.SSLContextBuilder;
import org.apiphany.security.ssl.SSLContexts;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.ssl.StoreInfo;

/**
 * Utility class for SSL/TLS related tests. Because SSL context initialization is relatively slow and can be a
 * bottleneck in tests that require SSL contexts, this class provides predefined constants for keystore and truststore
 * paths and passwords used in tests, as well as dummy implementations of {@link ExchangeClient} that are SSL context
 * aware for testing SSL client behavior without the overhead of a real exchange client implementation.
 * <p>
 * Additionally, this class provides methods to create default and fully configured SSL contexts for testing SSL context
 * creation and configuration with specific SSL properties.
 * <p>
 * This class provides:
 * <ul>
 * <li>predefined constants for keystore and truststore paths and passwords used in tests</li>
 * <li>dummy implementations of {@link ExchangeClient} that are SSL context aware for testing SSL client behavior</li>
 * <li>methods to create default and fully configured SSL contexts for testing SSL context creation and
 * configuration</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class SSLValues {

	public static final String KEYSTORE_PATH = "security/ssl/keystore.jks";
	public static final String KEYSTORE_PASSWORD = "keystorepassword123";
	public static final String TRUSTSTORE_PATH = "security/ssl/truststore.jks";
	public static final String TRUSTSTORE_PASSWORD = "truststorepassword123";

	/**
	 * Dummy exchange client that is SSL context aware for testing SSL client behavior without the overhead of a real
	 * exchange client implementation.
	 * <p>
	 * This dummy client can be configured with {@link SSLProperties} through the constructor to create an SSL context for
	 * testing SSL context creation and configuration.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class DummySSLExchangeClient implements ExchangeClient, SSLContextAware {

		private ClientProperties clientProperties;
		private SSLContext sslContext;
		private boolean closed = false;
		private boolean throwOnClose = false;

		public DummySSLExchangeClient() {
			// empty
		}

		public DummySSLExchangeClient(final ClientProperties clientProperties) {
			this.clientProperties = clientProperties;
			SSLProperties sslProperties = clientProperties.getCustomProperties(SSLProperties.class);
			if (null == sslProperties) {
				return;
			}
			if (SSLProperties.isEmpty(sslProperties)) {
				// do not create a new SSL context if SSL properties are empty to avoid unnecessary overhead and potential issues with
				// default SSL context creation when no SSL configuration is provided
				this.sslContext = SSLValues.defaultSSLContext();
				return;
			}
			if (SSLValues.KEYSTORE_PATH.equals(sslProperties.getKeystore().getLocation())
					&& SSLValues.TRUSTSTORE_PATH.equals(sslProperties.getTruststore().getLocation())
					&& SSLProtocol.TLS_1_3.equals(sslProperties.getProtocol())) {
				// return a pre-built SSL context for the specific SSL properties used in the tests to avoid overhead of building an SSL
				// context from the provided properties
				this.sslContext = SSLValues.fullSSLContext();
				return;
			}
			this.sslContext = SSLContextBuilder.create().properties(sslProperties).build();
		}

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
			return null;
		}

		@Override
		public void close() {
			if (throwOnClose) {
				throw new RuntimeException("Simulated close exception");
			}
			this.closed = true;
		}

		@Override
		@SuppressWarnings("unchecked")
		public ClientProperties getClientProperties() {
			return clientProperties;
		}

		@Override
		public AuthenticationType getAuthenticationType() {
			return AuthenticationType.NONE;
		}

		@Override
		public SSLContext getSslContext() {
			return sslContext;
		}

		public boolean isClosed() {
			return closed;
		}

		public void throwOnClose() {
			this.throwOnClose = true;
		}
	}

	/**
	 * Dummy exchange client that is not SSL context aware for testing SSL client behavior when the underlying client does
	 * not support SSL contexts. This client intentionally does not implement SSL context awareness to test the behavior of
	 * SSL clients when the underlying client is not properly SSL-configured.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class BadSSLExchangeClient extends DummySSLExchangeClient {

		@SuppressWarnings("unused")
		public BadSSLExchangeClient(final ClientProperties clientProperties) {
			// do not call super to avoid SSL context initialization
		}

		public BadSSLExchangeClient() {
			// empty
		}

		@Override
		public SSLContext getSslContext() {
			return null;
		}
	}

	/**
	 * Returns a default SSL context for testing SSL context creation and configuration when no specific SSL properties are
	 * provided. This default SSL context is created with default SSL properties to ensure consistent behavior across tests
	 * that require an SSL context without specific configuration.
	 *
	 * @return a default SSL context
	 */
	public static SSLContext defaultSSLContext() {
		return InstanceHolder.DEFAULT_SSL_CONTEXT;
	}

	/**
	 * Returns a fully configured SSL context for testing SSL context creation and configuration with specific SSL
	 * properties. This fully configured SSL context is created with the specific SSL properties used in the tests to ensure
	 * consistent behavior across tests that require an SSL context with specific configuration.
	 *
	 * @return a fully configured SSL context
	 */
	public static SSLContext fullSSLContext() {
		return InstanceHolder.FULL_SSL_CONTEXT;
	}

	/**
	 * Holder class for lazily initialized SSL contexts used in tests. This class ensures that the SSL contexts are only
	 * created when needed and that they are shared across tests to avoid unnecessary overhead of creating multiple SSL
	 * contexts with the same configuration.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class InstanceHolder {

		private static final SSLContext DEFAULT_SSL_CONTEXT = SSLContexts.create(new SSLProperties());

		private static final SSLContext FULL_SSL_CONTEXT = SSLContextBuilder.create()
				.keystore(StoreInfo.of(KEYSTORE_PATH, KEYSTORE_PASSWORD.toCharArray()))
				.truststore(StoreInfo.of(TRUSTSTORE_PATH, TRUSTSTORE_PASSWORD.toCharArray()))
				.protocol(SSLProtocol.TLS_1_3)
				.build();
	}

	/**
	 * Hide constructor.
	 */
	private SSLValues() {
		// empty
	}
}
