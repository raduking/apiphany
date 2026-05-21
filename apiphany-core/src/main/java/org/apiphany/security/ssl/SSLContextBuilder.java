package org.apiphany.security.ssl;

import java.util.Objects;

import javax.net.ssl.SSLContext;

/**
 * Fluent builder for creating {@link SSLContext} instances from key store and trust store configuration.
 *
 * @author Radu Sebastian LAZIN
 */
public class SSLContextBuilder {

	/**
	 * The SSL properties to configure.
	 */
	private SSLProperties sslProperties = new SSLProperties();

	/**
	 * Hide constructor.
	 */
	private SSLContextBuilder() {
		// empty
	}

	/**
	 * Creates a new {@link SSLContextBuilder}.
	 *
	 * @return a new {@link SSLContextBuilder}
	 */
	public static SSLContextBuilder create() {
		return new SSLContextBuilder();
	}

	/**
	 * Sets the SSL properties to use for building the SSL context.
	 *
	 * @param sslProperties the SSL properties to configure
	 * @return this
	 */
	public SSLContextBuilder properties(final SSLProperties sslProperties) {
		this.sslProperties = Objects.requireNonNull(sslProperties, "SSL properties cannot be null");
		return this;
	}

	/**
	 * Sets the key store configuration.
	 *
	 * @param location the file system path or classpath resource to the key store
	 * @param password the password to access the key store
	 * @return this
	 */
	public SSLContextBuilder keystore(final String location, final char[] password) {
		return keystore(StoreInfo.of(location, password));
	}

	/**
	 * Sets the key store configuration.
	 *
	 * @param location the file system path or classpath resource to the key store
	 * @param password the password to access the key store
	 * @param type the key store type (e.g., "PKCS12", "JKS")
	 * @return this
	 */
	public SSLContextBuilder keystore(final String location, final char[] password, final String type) {
		return keystore(StoreInfo.of(location, password, type));
	}

	/**
	 * Sets the key store configuration.
	 *
	 * @param storeInfo the key store information
	 * @return this
	 */
	public SSLContextBuilder keystore(final StoreInfo storeInfo) {
		sslProperties.setKeystore(storeInfo);
		return this;
	}

	/**
	 * Sets the trust store configuration.
	 *
	 * @param location the file system path or classpath resource to the trust store
	 * @param password the password to access the trust store
	 * @return this
	 */
	public SSLContextBuilder truststore(final String location, final char[] password) {
		sslProperties.setTruststore(StoreInfo.of(location, password));
		return this;
	}

	/**
	 * Sets the trust store configuration.
	 *
	 * @param location the file system path or classpath resource to the trust store
	 * @param password the password to access the trust store
	 * @param type the trust store type (e.g., "JKS", "PKCS12")
	 * @return this
	 */
	public SSLContextBuilder truststore(final String location, final char[] password, final String type) {
		return truststore(StoreInfo.of(location, password, type));
	}

	/**
	 * Sets the trust store configuration.
	 *
	 * @param storeInfo the trust store information
	 * @return this
	 */
	public SSLContextBuilder truststore(final StoreInfo storeInfo) {
		sslProperties.setTruststore(storeInfo);
		return this;
	}

	/**
	 * Sets the SSL/TLS protocol version.
	 *
	 * @param protocol the {@link SSLProtocol} to use for secure connections
	 * @return this
	 */
	public SSLContextBuilder protocol(final SSLProtocol protocol) {
		sslProperties.setProtocol(protocol);
		return this;
	}

	/**
	 * Builds the {@link SSLContext} from the configured properties.
	 *
	 * @return a new {@link SSLContext}
	 */
	public SSLContext build() {
		return SSLContexts.create(sslProperties);
	}
}
