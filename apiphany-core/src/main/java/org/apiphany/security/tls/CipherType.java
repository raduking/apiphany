package org.apiphany.security.tls;

/**
 * Represents the category of a bulk (symmetric) cipher in TLS.
 *
 * <ul>
 * <li>{@link #AEAD} - Authenticated Encryption with Associated Data ciphers (e.g., AES-GCM, ChaCha20-Poly1305). These
 * integrate encryption and authentication into a single operation.</li>
 * <li>{@link #BLOCK} - Traditional block ciphers in CBC/CTR/CNT modes (e.g., AES-CBC, 3DES-CBC, GOST-CTR). These
 * require a separate MAC algorithm for authentication.</li>
 * <li>{@link #STREAM} - Stream ciphers (e.g., RC4). These also require a separate MAC algorithm.</li>
 * <li>{@link #NO_ENCRYPTION} - No encryption (plaintext only). Always requires a MAC to protect integrity.</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public enum CipherType {

	/**
	 * Authenticated encryption (AES-GCM, ChaCha20).
	 */
	AEAD(true, false, false),

	/**
	 * Block cipher (AES-CBC).
	 */
	BLOCK(false, true, true),

	/**
	 * Stream cipher (RC4).
	 */
	STREAM(false, true, false),

	/**
	 * No encryption.
	 */
	NO_ENCRYPTION(false, true, false);

	/**
	 * Indicates whether the cipher type provides authentication.
	 */
	private final boolean authenticated;

	/**
	 * Indicates whether the cipher type uses a MAC for authentication.
	 */
	private final boolean usesMac;

	/**
	 * Indicates whether the cipher type uses padding.
	 */
	private final boolean usesPadding;

	/**
	 * Constructs a {@link CipherType} with the specified properties.
	 *
	 * @param authenticated whether the cipher type provides authentication
	 * @param usesMac whether the cipher type uses a MAC for authentication
	 * @param usesPadding whether the cipher type uses padding
	 */
	CipherType(final boolean authenticated, final boolean usesMac, final boolean usesPadding) {
		this.authenticated = authenticated;
		this.usesMac = usesMac;
		this.usesPadding = usesPadding;
	}

	/**
	 * Returns whether the cipher type provides authentication.
	 *
	 * @return true if authenticated, false otherwise
	 */
	public boolean authenticated() {
		return authenticated;
	}

	/**
	 * Returns whether the cipher type uses a MAC for authentication.
	 *
	 * @return true if uses MAC, false otherwise
	 */
	public boolean usesMac() {
		return usesMac;
	}

	/**
	 * Returns whether the cipher type uses padding.
	 *
	 * @return true if uses padding, false otherwise
	 */
	public boolean usesPadding() {
		return usesPadding;
	}
}
