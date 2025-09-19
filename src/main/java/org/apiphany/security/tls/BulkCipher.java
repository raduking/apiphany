package org.apiphany.security.tls;

import org.apiphany.security.MessageDigestAlgorithm;

/**
 * Represents the bulk (symmetric) encryption algorithm used in a TLS cipher suite.
 * <p>
 * Each bulk cipher defines:
 * <ul>
 * <li>The type of encryption mode (AEAD, block, or stream)</li>
 * <li>The key length in bytes</li>
 * <li>The block size in bytes (if applicable)</li>
 * <li>The fixed IV length in bytes (for AEAD modes such as AES-GCM or CCM)</li>
 * </ul>
 *
 * <p>
 * In TLS 1.2 and below, the bulk cipher determines how the record payload is encrypted:
 * <ul>
 * <li><b>AEAD ciphers</b> (e.g., AES-GCM, ChaCha20-Poly1305) integrate encryption and authentication, requiring only
 * encryption keys and fixed IVs.</li>
 * <li><b>Block ciphers</b> (e.g., AES-CBC, 3DES-CBC) require separate MAC keys and IVs in the key block.</li>
 * <li><b>Stream ciphers</b> (e.g., RC4) require separate MAC keys but do not use IVs.</li>
 * </ul>
 *
 * <p>
 * This abstraction provides the necessary metadata for correct key block derivation during the TLS handshake and for
 * selecting the appropriate record protection mechanism.
 *
 * @author Radu Sebastian LAZIN
 */
public enum BulkCipher {

	/**
	 * AES-128 in GCM (AEAD) mode. Key length: 16 bytes, block size: 16, fixed IV length: 4 (typical TLS fixed IV for
	 * AES-GCM).
	 */
	AES_128_GCM(16, 16, 4, CipherType.AEAD, -1),

	/**
	 * AES-256 in GCM (AEAD) mode. Key length: 32 bytes, block size: 16, fixed IV length: 4.
	 */
	AES_256_GCM(32, 16, 4, CipherType.AEAD, -1),

	/**
	 * ChaCha20-Poly1305 AEAD. Key length: 32 bytes, stream-like block size: 0, fixed IV length: 12 (RFC 7905 style / TLS
	 * 1.2 variant).
	 */
	CHACHA20_POLY1305(32, 0, 12, CipherType.AEAD, -1),

	/**
	 * AES-128 in CCM (AEAD) mode. Key length: 16 bytes, block size: 16, fixed IV length: 4 (TLS uses a small fixed part
	 * plus explicit nonce).
	 */
	AES_128_CCM(16, 16, 4, CipherType.AEAD, -1),

	/**
	 * AES-256 in CCM (AEAD) mode. Key length: 32 bytes, block size: 16, fixed IV length: 4.
	 */
	AES_256_CCM(32, 16, 4, CipherType.AEAD, -1),

	/**
	 * AES-128 in CBC mode (non-AEAD). Key length: 16 bytes, block size: 16, IV length: 16.
	 */
	AES_128_CBC(16, 16, 16, CipherType.BLOCK, -1),

	/**
	 * AES-256 in CBC mode (non-AEAD). Key length: 32 bytes, block size: 16, IV length: 16.
	 */
	AES_256_CBC(32, 16, 16, CipherType.BLOCK, -1),

	/**
	 * Triple-DES (3DES) in CBC mode (legacy). Key length: 24 bytes, block size: 8, IV length: 8.
	 */
	_3DES_EDE_CBC(24, 8, 8, CipherType.BLOCK, -1),

	/**
	 * GOST R 34.12-2015.
	 */
	KUZNYECHIK_CTR(32, 16, 16, CipherType.BLOCK, -1),

	/**
	 * GOST 28147-89.
	 */
	MAGMA_CTR(32, 8, 8, CipherType.BLOCK, -1),

	/**
	 * CNT == CTR variant
	 */
	GOST_28147_CNT(32, 8, 8, CipherType.BLOCK, -1),

	/**
	 * RC4 with 128-bit key (legacy, insecure). Key length: 16 bytes, no IV.
	 */
	RC4_128(16, 0, 0, CipherType.STREAM, -1),

	/**
	 * RC4 with 56-bit key (export legacy). Key length: 7 bytes (56 bits), no IV.
	 */
	RC4_56(7, 0, 0, CipherType.STREAM, -1),

	/**
	 * Un-encryped cipher (used for special/reserved values or when no encryption is negotiated).
	 */
	UNENCRYPTED(0, 0, 0, CipherType.NO_ENCRYPTION, 0);

	/**
	 * The symmetric key length in bytes.
	 */
	private final int keyLength;

	/**
	 * The block size in bytes, or 0 for stream ciphers.
	 */
	private final int blockSize;

	/**
	 * The fixed IV length in bytes (0 if not applicable).
	 */
	private final int fixedIvLength;

	/**
	 * The bulk cipher type.
	 */
	private final CipherType type;

	/**
	 * If the MAC key length is -1 it means "depends on negotiated hash"; use {@link #macKeyLength(MessageDigestAlgorithm)}
	 * to obtain the final value.
	 */
	private final int macKeyLength;

	/**
	 * Constructor.
	 *
	 * @param keyLength the symmetric key length in bytes
	 * @param blockSize the block size in bytes, or 0 for stream ciphers
	 * @param fixedIvLength the fixed IV length in bytes (0 if not applicable)
	 * @param cipherType the cipher type
	 * @param macKeyLength the MAC key length
	 */
	BulkCipher(final int keyLength, final int blockSize, final int fixedIvLength, final CipherType cipherType, final int macKeyLength) {
		this.keyLength = keyLength;
		this.blockSize = blockSize;
		this.fixedIvLength = fixedIvLength;
		this.type = cipherType;
		this.macKeyLength = macKeyLength;
	}

	/**
	 * Returns the symmetric key length in bytes.
	 *
	 * @return the symmetric key length in bytes
	 */
	public int keyLength() {
		return keyLength;
	}

	/**
	 * Returns the block size in bytes, or 0 for stream ciphers.
	 *
	 * @return the block size in bytes
	 */
	public int blockSize() {
		return blockSize;
	}

	/**
	 * Returns the fixed IV length in bytes (0 if not applicable).
	 *
	 * @return the fixed IV length in bytes
	 */
	public int fixedIvLength() {
		return fixedIvLength;
	}

	/**
	 * Returns the cipher type (AEAD, BLOCK, STREAM, NULL).
	 *
	 * @return the cipher type
	 */
	public CipherType type() {
		return type;
	}

	/**
	 * Returns true for AEAD ciphers (AES-GCM, ChaCha20-Poly1305, CCM).
	 *
	 * @return true for AEAD ciphers, false otherwise
	 */
	public boolean isAEAD() {
		return CipherType.AEAD == type();
	}

	/**
	 * Returns the MAC key length in bytes. If this cipher uses a MAC whose size depends on the negotiated hash (HMAC-SHA256
	 * -> 32 bytes), this method should be called with the negotiated {@link MessageDigestAlgorithm}.
	 *
	 * @param mac negotiated message digest algorithm
	 * @return MAC key length in bytes
	 */
	public int macKeyLength(final MessageDigestAlgorithm mac) {
		return macKeyLength == -1 ? mac.digestLength() : macKeyLength;
	}
}
