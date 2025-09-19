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
	AEAD,

	/**
	 * Block cipher (AES-CBC).
	 */
	BLOCK,

	/**
	 * Stream cipher (RC4).
	 */
	STREAM,

	/**
	 * No encryption.
	 */
	NO_ENCRYPTION

}
