package org.apiphany.security.ssl;

/**
 * Configuration properties for SSL/TLS connections. This class encapsulates SSL protocol settings and certificate store
 * information for both keystore (client certificates) and truststore (trusted certificates).
 *
 * <p>
 * The default protocol is set to TLS 1.3 for maximum security.
 * </p>
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
	private CertificateStoreInfo keystore = new CertificateStoreInfo();

	/**
	 * Configuration for the trust store containing trusted CA certificates.
	 */
	private CertificateStoreInfo truststore = new CertificateStoreInfo();

	/**
	 * Default constructor.
	 */
	public SSLProperties() {
		// empty
	}

	/**
	 * Gets the key store configuration.
	 *
	 * @return the {@link CertificateStoreInfo} containing key store settings
	 */
	public CertificateStoreInfo getKeystore() {
		return keystore;
	}

	/**
	 * Sets the key store configuration.
	 *
	 * @param keystore the {@link CertificateStoreInfo} containing key store settings
	 */
	public void setKeystore(final CertificateStoreInfo keystore) {
		this.keystore = keystore;
	}

	/**
	 * Gets the trust store configuration.
	 *
	 * @return the {@link CertificateStoreInfo} containing trust store settings
	 */
	public CertificateStoreInfo getTruststore() {
		return truststore;
	}

	/**
	 * Sets the trust store configuration.
	 *
	 * @param truststore the {@link CertificateStoreInfo} containing trust store settings
	 */
	public void setTruststore(final CertificateStoreInfo truststore) {
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
