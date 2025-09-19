package org.apiphany.security.tls;

import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apiphany.lang.Bytes;
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
	 * AES-128 in GCM (AEAD) mode. Tag length: 16. Nonce = 4-byte fixed IV + 8-byte explicit per-record IV.
	 */
	AES_128_GCM("AES", 16, 16, 4, 8, CipherType.AEAD, -1, 16),

	/**
	 * AES-256 in GCM (AEAD) mode. Tag length: 16. Nonce = 4-byte fixed IV + 8-byte explicit per-record IV.
	 */
	AES_256_GCM("AES", 32, 16, 4, 8, CipherType.AEAD, -1, 16),

	/**
	 * ChaCha20-Poly1305 AEAD. Nonce = 12-byte fixed IV, no explicit nonce. Tag length: 16.
	 */
	CHACHA20_POLY1305("ChaCha20-Poly1305", 32, 0, 12, 0, CipherType.AEAD, -1, 16),

	/**
	 * AES-128 in CCM (AEAD) mode. Tag length: 16. Nonce = 4-byte fixed IV + 8-byte explicit per-record IV.
	 */
	AES_128_CCM("AES", 16, 16, 4, 8, CipherType.AEAD, -1, 16),

	/**
	 * AES-256 in CCM (AEAD) mode. Tag length: 16.
	 */
	AES_256_CCM("AES", 32, 16, 4, 8, CipherType.AEAD, -1, 16),

	/**
	 * AES-128 in CCM with 8-byte tag (RFC 6655).
	 */
	AES_128_CCM_8("AES", 16, 16, 4, 8, CipherType.AEAD, -1, 8),

	/**
	 * AES-256 in CCM with 8-byte tag.
	 */
	AES_256_CCM_8("AES", 32, 16, 4, 8, CipherType.AEAD, -1, 8),

	/**
	 * AES-128 in CBC mode (non-AEAD). IV length: 16.
	 */
	AES_128_CBC("AES", 16, 16, 16, 0, CipherType.BLOCK, -1, 0),

	/**
	 * AES-256 in CBC mode (non-AEAD). IV length: 16.
	 */
	AES_256_CBC("AES", 32, 16, 16, 0, CipherType.BLOCK, -1, 0),

	/**
	 * Triple-DES (3DES) in CBC mode (legacy). IV length: 8.
	 */
	_3DES_EDE_CBC("DESede", 24, 8, 8, 0, CipherType.BLOCK, -1, 0),

	/**
	 * GOST R 34.12-2015 (Kuznyechik) in CTR mode. IV length: 16.
	 */
	KUZNYECHIK_CTR("Kuznyechik", 32, 16, 16, 0, CipherType.BLOCK, -1, 0),

	/**
	 * GOST 28147-89 in CTR mode. IV length: 8.
	 */
	MAGMA_CTR("GOST28147", 32, 8, 8, 0, CipherType.BLOCK, -1, 0),

	/**
	 * GOST 28147 CNT variant. IV length: 8.
	 */
	GOST_28147_CNT("GOST28147", 32, 8, 8, 0, CipherType.BLOCK, -1, 0),

	/**
	 * RC4 with 128-bit key (legacy, insecure). No IV.
	 */
	RC4_128("RC4", 16, 0, 0, 0, CipherType.STREAM, -1, 0),

	/**
	 * RC4 with 56-bit key (export legacy). No IV.
	 */
	RC4_56("RC4", 7, 0, 0, 0, CipherType.STREAM, -1, 0),

	/**
	 * Un-encrypted cipher (NULL cipher). No key, no IV, no tag.
	 */
	UNENCRYPTED("NONE", 0, 0, 0, 0, CipherType.NO_ENCRYPTION, 0, 0);

	/**
	 * The algorithm.
	 */
	private final String algorithm;

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
	 * The explicit per-record nonce length in bytes (sent with each record).
	 */
	private final int explicitNonceLength;

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
	 * The AEAD authentication tag length in bytes (0 for non-AEAD ciphers).
	 */
	private final int tagLength;

	/**
	 * Constructor.
	 *
	 * @param algorithm the algorithm
	 * @param keyLength the symmetric key length in bytes
	 * @param blockSize the block size in bytes, or 0 for stream ciphers
	 * @param fixedIvLength the fixed IV length in bytes (0 if not applicable)
	 * @param explicitNonceLength the explicit per-record nonce length in bytes (sent with each record)
	 * @param cipherType the cipher type
	 * @param macKeyLength the MAC key length
	 * @param tagLength AEAD authentication tag length in bytes (0 for non-AEAD ciphers)
	 */
	BulkCipher(
			final String algorithm,
			final int keyLength,
			final int blockSize,
			final int fixedIvLength,
			final int explicitNonceLength,
			final CipherType cipherType,
			final int macKeyLength,
			final int tagLength) {
		this.algorithm = algorithm;
		this.keyLength = keyLength;
		this.blockSize = blockSize;
		this.fixedIvLength = fixedIvLength;
		this.explicitNonceLength = explicitNonceLength;
		this.type = cipherType;
		this.macKeyLength = macKeyLength;
		this.tagLength = tagLength;
	}

	/**
	 * Returns the algorithm.
	 *
	 * @return the algorithm
	 */
	public String algorithm() {
		return algorithm;
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
	 * Returns the explicit per-record nonce length in bytes (sent with each record).
	 *
	 * @return the explicit per-record nonce length in bytes (sent with each record)
	 */
	public int explicitNonceLength() {
		return explicitNonceLength;
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
	 * Returns the MAC key length in bytes. If this cipher uses a MAC whose size depends on the negotiated hash (HMAC-SHA256
	 * -> 32 bytes), this method should be called with the negotiated {@link MessageDigestAlgorithm}.
	 *
	 * @param mac negotiated message digest algorithm
	 * @return MAC key length in bytes
	 */
	public int macKeyLength(final MessageDigestAlgorithm mac) {
		return macKeyLength == -1 ? mac.digestLength() : macKeyLength;
	}

	/**
	 * Returns the tag length.
	 *
	 * @return the tag length
	 */
	public int tagLength() {
		return tagLength;
	}

	/**
	 * Builds the appropriate {@link AlgorithmParameterSpec} for this bulk cipher, based on its cipher type and provided
	 * IV/nonce.
	 *
	 * @param fullIV the IV or nonce to use (may be null for stream ciphers)
	 * @return the AlgorithmParameterSpec, or null if not required
	 */
	public AlgorithmParameterSpec spec(final byte[] fullIV) {
		return switch (type) {
			case AEAD -> new GCMParameterSpec(tagLength() * 8, fullIV);
			case BLOCK -> new IvParameterSpec(fullIV);
			case STREAM, NO_ENCRYPTION -> null;
		};
	}

	/**
	 * Returns a new cipher.
	 *
	 * @param mode the cipher mode {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE}
	 * @param key the cipher key
	 * @param spec the algorithm parameter specification
	 * @return a new cipher
	 */
	public Cipher cipher(final int mode, final byte[] key, final AlgorithmParameterSpec spec) {
		try {
			SecretKeySpec secret = new SecretKeySpec(key, algorithm());
			Cipher cipher = Cipher.getInstance(type().transformation());
			if (null != spec) {
				cipher.init(mode, secret, spec);
			} else {
				cipher.init(mode, secret);
			}
			return cipher;
		} catch (GeneralSecurityException e) {
			throw new SecurityException("Error building cipher from: " + this, e);
		}
	}

	/**
	 * Returns the total IV length for AEAD ciphers (fixed IV + explicit nonce).
	 *
	 * @return total IV length in bytes
	 */
	public int fullIVLength() {
		return fixedIvLength + explicitNonceLength;
	}

	/**
	 * Returns the full IV for AEAD, or the IV for BLOCK/STREAM.
	 *
	 * @param keyIV fixed IV from ExchangeKeys (or null if not used)
	 * @param explicitNonce explicit per-record nonce (or null if not applicable)
	 * @return full IV to pass to Cipher
	 */
	public byte[] fullIV(final byte[] keyIV, final byte[] explicitNonce) {
		switch (type()) {
			case AEAD:
				if (explicitNonceLength <= 0) {
					return Bytes.isNotEmpty(keyIV) ? keyIV.clone() : new byte[fixedIvLength()];
				}
				byte[] iv = new byte[fixedIvLength() + explicitNonceLength()];
				System.arraycopy(keyIV, 0, iv, 0, fixedIvLength());
				System.arraycopy(explicitNonce, 0, iv, fixedIvLength(), explicitNonceLength());
				return iv;
			case BLOCK, STREAM, NO_ENCRYPTION:
			default:
				return Bytes.isNotEmpty(keyIV) ? keyIV.clone() : Bytes.EMPTY;
		}
	}
}
