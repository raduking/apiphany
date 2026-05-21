package org.apiphany.security.ssl.client;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.logging.Slf4jLoggerAdapter;
import org.apiphany.security.ssl.KeyStoreType;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.SSLProtocol;
import org.apiphany.security.ssl.StoreInfo;
import org.morphix.lang.Nullables;
import org.morphix.lang.resource.ScopedResource;

/**
 * Builder for creating {@link SSLHttpExchangeClient} instances with a fluent API for configuring SSL/TLS settings.
 * <p>
 * This builder injects {@link SSLProperties} into the client properties before building the underlying exchange client
 * so that the SSL context is properly configured at the transport level.
 *
 * @author Radu Sebastian LAZIN
 */
public class SSLHttpExchangeClientBuilder extends ExchangeClientBuilder {

	/**
	 * Class logger.
	 */
	private static final Slf4jLoggerAdapter LOGGER = Slf4jLoggerAdapter.of(SSLHttpExchangeClient.class);

	/**
	 * The SSL properties to configure.
	 */
	private final SSLProperties sslProperties = new SSLProperties();

	/**
	 * Flag to track if any SSL properties were set on this builder. This is used to determine if SSL configuration should
	 * be validated on the underlying client.
	 */
	private boolean sslLocalConfigured = false;

	/**
	 * Hide constructor.
	 */
	private SSLHttpExchangeClientBuilder() {
		// empty
	}

	/**
	 * Creates a new SSL exchange client builder.
	 *
	 * @return a new SSL exchange client builder
	 */
	public static SSLHttpExchangeClientBuilder create() {
		return new SSLHttpExchangeClientBuilder();
	}

	/**
	 * Sets the key store configuration with the default store type ({@link KeyStoreType#PKCS12}).
	 *
	 * @param location the file system path or classpath resource to the key store
	 * @param password the password to access the key store
	 * @return this
	 */
	public SSLHttpExchangeClientBuilder keystore(final String location, final String password) {
		return keystore(location, password, KeyStoreType.PKCS12);
	}

	/**
	 * Sets the key store configuration.
	 *
	 * @param location the file system path or classpath resource to the key store
	 * @param password the password to access the key store
	 * @param type the key store type (e.g., "JKS", "PKCS12")
	 * @return this
	 */
	public SSLHttpExchangeClientBuilder keystore(final String location, final String password, final KeyStoreType type) {
		char[] passwordChars = Nullables.whenNotNull(password, String::toCharArray);
		sslProperties.setKeystore(StoreInfo.of(location, passwordChars, type.value()));
		return sslConfigured();
	}

	/**
	 * Sets the trust store configuration with the default store type ({@link KeyStoreType#PKCS12}).
	 *
	 * @param location the file system path or classpath resource to the trust store
	 * @param password the password to access the trust store
	 * @return this
	 */
	public SSLHttpExchangeClientBuilder truststore(final String location, final String password) {
		return truststore(location, password, KeyStoreType.PKCS12);
	}

	/**
	 * Sets the trust store configuration.
	 *
	 * @param location the file system path or classpath resource to the trust store
	 * @param password the password to access the trust store
	 * @param type the trust store type (e.g., "JKS", "PKCS12")
	 * @return this
	 */
	public SSLHttpExchangeClientBuilder truststore(final String location, final String password, final KeyStoreType type) {
		char[] passwordChars = Nullables.whenNotNull(password, String::toCharArray);
		sslProperties.setTruststore(StoreInfo.of(location, passwordChars, type.value()));
		return sslConfigured();
	}

	/**
	 * Sets the SSL/TLS protocol version.
	 *
	 * @param protocol the {@link SSLProtocol} to use for secure connections
	 * @return this
	 */
	public SSLHttpExchangeClientBuilder protocol(final SSLProtocol protocol) {
		sslProperties.setProtocol(protocol);
		return SSLProtocol.DEFAULT != protocol ? sslConfigured() : this;
	}

	/**
	 * Marks this builder as having SSL properties configured. This is used to trigger validation of the underlying client
	 * SSL configuration during build. This method can be used when SSL properties are set directly on the builder's
	 * {@link SSLProperties} instance instead of using the provided fluent methods.
	 *
	 * @return this
	 */
	public SSLHttpExchangeClientBuilder sslConfigured() {
		this.sslLocalConfigured = true;
		return this;
	}

	/**
	 * Builds the SSL exchange client based on the builder members.
	 * <p>
	 * If {@link SSLProperties} are already configured in the client properties they take priority. Otherwise, the
	 * properties configured on this builder are used. After building, validates that the underlying client is properly
	 * SSL-configured when explicit keystore/truststore settings were provided.
	 *
	 * @return a new exchange client resource with life cycle management information
	 * @throws IllegalStateException if explicit SSL properties were set but the underlying client was not built with SSL
	 *     support
	 */
	@SuppressWarnings("resource")
	@Override
	public ScopedResource<ExchangeClient> build() {
		if (null == clientProperties) {
			// use default client properties if none were provided to ensure the SSL properties are properly injected
			properties(ClientProperties.defaults());
		}
		SSLProperties sslPropertiesClient = clientProperties.getCustomProperties(SSLProperties.class);
		if (sslLocalConfigured) {
			requireThat(SSLProperties.isEmpty(sslPropertiesClient),
					"Conflicting SSL configuration: SSL properties were configured both on the builder and in the client properties."
							+ " Please ensure SSL properties are only set in one place to avoid ambiguity.");
			return buildLocalConfigured();
		}
		if (null == sslPropertiesClient) {
			clientProperties.setCustomProperties(sslProperties);
		}
		ScopedResource<ExchangeClient> clientResource = super.build();
		return buildDecorator(clientResource);
	}

	/**
	 * Builds the SSL exchange client when SSL properties were configured on this builder. Validates that there are no
	 * conflicting SSL properties in the client properties and that the underlying client is properly SSL-configured.
	 *
	 * @return a new exchange client resource with life cycle management information
	 * @throws IllegalStateException if there are conflicting SSL properties or if the underlying client was not built with
	 *     SSL support
	 */
	@SuppressWarnings("resource")
	protected ScopedResource<ExchangeClient> buildLocalConfigured() {
		clientProperties.setCustomProperties(sslProperties);
		ScopedResource<ExchangeClient> clientResource = super.build();
		try {
			requireThat(!isBuiltClient(),
					"Cannot build SSL exchange client: the underlying client was built without the builder SSL properties configured,"
							+ " but this builder has SSL properties set. Please ensure the underlying client is built with SSL properties"
							+ " or remove SSL configuration from this builder.");
			return buildDecorator(clientResource);
		} catch (RuntimeException ex) {
			clientResource.closeIfManaged(e -> LOGGER.error("Error closing managed underlying exchange client: {}",
					clientResource.unwrap().getClass(), e));
			throw ex;
		}
	}

	/**
	 * Builds the SSL exchange client by decorating the provided client resource. The provided client resource is expected
	 * to be properly SSL-configured if SSL properties were set on this builder.
	 *
	 * @param clientResource the underlying exchange client resource to decorate with SSL semantics
	 * @return a new exchange client resource with life cycle management information
	 */
	@SuppressWarnings("resource")
	protected ScopedResource<ExchangeClient> buildDecorator(final ScopedResource<ExchangeClient> clientResource) {
		SSLHttpExchangeClient sslClient = new SSLHttpExchangeClient(clientResource);
		return ScopedResource.managed(sslClient);
	}

	/**
	 * Validates that a key store is configured. This consumer is used by {@code mtls()} to ensure mutual TLS requirements
	 * are met.
	 *
	 * @param builder the SSL exchange client builder to validate
	 * @throws IllegalStateException if no key store is configured
	 */
	public static void requireKeystore(final SSLHttpExchangeClientBuilder builder) {
		StoreInfo keystore = builder.sslProperties.getKeystore();
		requireThat(Strings.isNotEmpty(keystore.getLocation()),
				"Keystore must be configured for mutual TLS (mTLS). Use keystore(path, password) to configure a client certificate.");
	}
}
