package org.apiphany.security.ssl;

import org.apiphany.client.ClientProperties;
import org.apiphany.json.JsonBuilder;

/**
 * Configuration properties for SSL/TLS connections. This class encapsulates SSL protocol settings and certificate store
 * information for both keystore (client certificates) and truststore (trusted certificates).
 *
 * <p>
 * The default protocol is set to TLS 1.3 for maximum security.
 *
 * <p>
 * To configure these properties in the {@link ClientProperties} under the {@code custom} root, use the prefix
 * {@code ssl} as defined in {@link #ROOT}. For example:
 *
 * <pre>
 * my-client-properties.custom.ssl.protocol=TLSv1.2
 * </pre>
 *
 * or in YAML:
 *
 * <pre>
 * my-client-properties:
 *   custom:
 *     ssl:
 *       protocol: TLSv1.2
 * </pre>
 *
 * This would set the SSL protocol to TLS 1.2 and similarly for key store and trust store configurations.
 *
 * @author Radu Sebastian LAZIN
 */
public class SSLProperties {

	/**
	 * The root configuration path for SSL properties in configuration files.
	 */
	public static final String ROOT = "ssl";

	/**
	 * The SSL/TLS protocol version to use for secure connections. Defaults to {@link SSLProtocol#TLS_1_3}.
	 */
	private SSLProtocol protocol = SSLProtocol.TLS_1_3;

	/**
	 * Configuration for the key store containing client certificates and private keys.
	 */
	private StoreInfo keystore = new StoreInfo();

	/**
	 * Configuration for the trust store containing trusted CA certificates.
	 */
	private StoreInfo truststore = new StoreInfo();

	/**
	 * Default constructor.
	 */
	public SSLProperties() {
		// empty
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Gets the key store configuration.
	 *
	 * @return the {@link StoreInfo} containing key store settings
	 */
	public StoreInfo getKeystore() {
		return keystore;
	}

	/**
	 * Sets the key store configuration.
	 *
	 * @param keystore the {@link StoreInfo} containing key store settings
	 */
	public void setKeystore(final StoreInfo keystore) {
		this.keystore = keystore;
	}

	/**
	 * Gets the trust store configuration.
	 *
	 * @return the {@link StoreInfo} containing trust store settings
	 */
	public StoreInfo getTruststore() {
		return truststore;
	}

	/**
	 * Sets the trust store configuration.
	 *
	 * @param truststore the {@link StoreInfo} containing trust store settings
	 */
	public void setTruststore(final StoreInfo truststore) {
		this.truststore = truststore;
	}

	/**
	 * Gets the SSL/TLS protocol version.
	 *
	 * @return the configured {@link SSLProtocol}
	 */
	public SSLProtocol getProtocol() {
		return protocol;
	}

	/**
	 * Sets the SSL/TLS protocol version.
	 *
	 * @param protocol the {@link SSLProtocol} to use for secure connections
	 */
	public void setProtocol(final SSLProtocol protocol) {
		this.protocol = protocol;
	}
}
