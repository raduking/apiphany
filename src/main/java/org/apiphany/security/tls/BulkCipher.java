package org.apiphany.security.tls;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
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
	AES_128_GCM(BulkCipherInfo.of("AES", 16, 16, 4, 8, CipherType.AEAD, -1, 16)),

	/**
	 * AES-256 in GCM (AEAD) mode. Tag length: 16. Nonce = 4-byte fixed IV + 8-byte explicit per-record IV.
	 */
	AES_256_GCM(BulkCipherInfo.of("AES", 32, 16, 4, 8, CipherType.AEAD, -1, 16)),

	/**
	 * ChaCha20-Poly1305 AEAD. Nonce = 12-byte fixed IV, no explicit nonce. Tag length: 16.
	 */
	CHACHA20_POLY1305(BulkCipherInfo.of("ChaCha20-Poly1305", 32, 0, 12, 0, CipherType.AEAD, -1, 16)),

	/**
	 * AES-128 in CCM (AEAD) mode. Tag length: 16. Nonce = 4-byte fixed IV + 8-byte explicit per-record IV.
	 */
	AES_128_CCM(BulkCipherInfo.of("AES", 16, 16, 4, 8, CipherType.AEAD, -1, 16)),

	/**
	 * AES-256 in CCM (AEAD) mode. Tag length: 16.
	 */
	AES_256_CCM(BulkCipherInfo.of("AES", 32, 16, 4, 8, CipherType.AEAD, -1, 16)),

	/**
	 * AES-128 in CCM with 8-byte tag (RFC 6655).
	 */
	AES_128_CCM_8(BulkCipherInfo.of("AES", 16, 16, 4, 8, CipherType.AEAD, -1, 8)),

	/**
	 * AES-256 in CCM with 8-byte tag.
	 */
	AES_256_CCM_8(BulkCipherInfo.of("AES", 32, 16, 4, 8, CipherType.AEAD, -1, 8)),

	/**
	 * AES-128 in CBC mode (non-AEAD). IV length: 16.
	 */
	AES_128_CBC(BulkCipherInfo.of("AES", 16, 16, 16, 0, CipherType.BLOCK, -1, 0)),

	/**
	 * AES-256 in CBC mode (non-AEAD). IV length: 16.
	 */
	AES_256_CBC(BulkCipherInfo.of("AES", 32, 16, 16, 0, CipherType.BLOCK, -1, 0)),

	/**
	 * Triple-DES (3DES) in CBC mode (legacy). IV length: 8.
	 */
	_3DES_EDE_CBC(BulkCipherInfo.of("DESede", 24, 8, 8, 0, CipherType.BLOCK, -1, 0)),

	/**
	 * GOST R 34.12-2015 (Kuznyechik) in CTR mode. IV length: 16.
	 */
	KUZNYECHIK_CTR(BulkCipherInfo.of("Kuznyechik", 32, 16, 16, 0, CipherType.BLOCK, -1, 0)),

	/**
	 * GOST 28147-89 in CTR mode. IV length: 8.
	 */
	MAGMA_CTR(BulkCipherInfo.of("GOST28147", 32, 8, 8, 0, CipherType.BLOCK, -1, 0)),

	/**
	 * GOST 28147 CNT variant. IV length: 8.
	 */
	GOST_28147_CNT(BulkCipherInfo.of("GOST28147", 32, 8, 8, 0, CipherType.BLOCK, -1, 0)),

	/**
	 * RC4 with 128-bit key (legacy, insecure). No IV.
	 */
	RC4_128(BulkCipherInfo.of("RC4", 16, 0, 0, 0, CipherType.STREAM, -1, 0)),

	/**
	 * RC4 with 56-bit key (export legacy). No IV.
	 */
	RC4_56(BulkCipherInfo.of("RC4", 7, 0, 0, 0, CipherType.STREAM, -1, 0)),

	/**
	 * Un-encrypted cipher (NULL cipher). No key, no IV, no tag.
	 */
	UNENCRYPTED(BulkCipherInfo.of("NONE", 0, 0, 0, 0, CipherType.NO_ENCRYPTION, 0, 0));

	/**
	 * Secure random number generator for IV generation.
	 */
	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * The information about this bulk cipher.
	 */
	private final BulkCipherInfo info;

	/**
	 * Constructor.
	 *
	 * @param info the information
	 */
	BulkCipher(final BulkCipherInfo info) {
		this.info = info;
	}

	/**
	 * Returns the algorithm.
	 *
	 * @return the algorithm
	 */
	public String algorithm() {
		return info.algorithm();
	}

	/**
	 * Returns the symmetric key length in bytes.
	 *
	 * @return the symmetric key length in bytes
	 */
	public int keyLength() {
		return info.keyLength();
	}

	/**
	 * Returns the block size in bytes, or 0 for stream ciphers.
	 *
	 * @return the block size in bytes
	 */
	public int blockSize() {
		return info.blockSize();
	}

	/**
	 * Returns the fixed IV length in bytes (0 if not applicable).
	 *
	 * @return the fixed IV length in bytes
	 */
	public int fixedIvLength() {
		return info.fixedIvLength();
	}

	/**
	 * Returns the explicit per-record nonce length in bytes (sent with each record).
	 *
	 * @return the explicit per-record nonce length in bytes (sent with each record)
	 */
	public int explicitNonceLength() {
		return info.explicitNonceLength();
	}

	/**
	 * Returns the cipher type (AEAD, BLOCK, STREAM, NULL).
	 *
	 * @return the cipher type
	 */
	public CipherType type() {
		return info.type();
	}

	/**
	 * Returns the MAC key length in bytes. If this cipher uses a MAC whose size depends on the negotiated hash (HMAC-SHA256
	 * -> 32 bytes), this method should be called with the negotiated {@link MessageDigestAlgorithm}.
	 *
	 * @param mac negotiated message digest algorithm
	 * @return MAC key length in bytes
	 */
	public int macKeyLength(final MessageDigestAlgorithm mac) {
		return info.macKeyLength() == -1 ? mac.digestLength() : info.macKeyLength();
	}

	/**
	 * Returns the tag length.
	 *
	 * @return the tag length
	 */
	public int tagLength() {
		return info.tagLength();
	}

	/**
	 * Builds the appropriate {@link AlgorithmParameterSpec} for this bulk cipher, based on its cipher type and provided
	 * IV/nonce.
	 *
	 * @param fullIV the IV or nonce to use (may be null for stream ciphers)
	 * @return the AlgorithmParameterSpec, or null if not required
	 */
	public AlgorithmParameterSpec spec(final byte[] fullIV) {
		return switch (type()) {
			case AEAD -> new GCMParameterSpec(tagLength() * 8, fullIV);
			case BLOCK -> new IvParameterSpec(fullIV);
			case STREAM, NO_ENCRYPTION -> null;
		};
	}

	/**
	 * Returns a new cipher with the given parameters.
	 *
	 * @param mode the cipher mode {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE}
	 * @param key the cipher key
	 * @param fullIV the iv used for algorithm parameter specification
	 * @return a new cipher
	 */
	public Cipher cipher(final int mode, final byte[] key, final byte[] fullIV) {
		return cipher(mode, key, spec(fullIV));
	}

	/**
	 * Returns a new cipher with the given parameters it delegates to {@link #cipher(int, byte[], byte[])} with the fullIV
	 * as {@code null}.
	 *
	 * @param mode the cipher mode {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE}
	 * @param key the cipher key
	 * @return a new cipher
	 */
	public Cipher cipher(final int mode, final byte[] key) {
		return cipher(mode, key, (byte[]) null);
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
	 * Returns the full IV for AEAD, or the IV for BLOCK/STREAM.
	 *
	 * @param keyIV fixed IV from ExchangeKeys (or null if not used)
	 * @param explicitNonce explicit per-record nonce (or null if not applicable)
	 * @return full IV to pass to Cipher
	 */
	public byte[] fullIV(final byte[] keyIV, final byte[] explicitNonce) {
		return switch (type()) {
			case AEAD -> {
				byte[] iv = new byte[fixedIvLength() + explicitNonceLength()];
				System.arraycopy(keyIV, 0, iv, 0, fixedIvLength());
				if (explicitNonceLength() > 0) {
					System.arraycopy(explicitNonce, 0, iv, fixedIvLength(), explicitNonceLength());
				}
				yield iv;
			}
			case BLOCK -> {
				byte[] iv = new byte[blockSize()];
				RANDOM.nextBytes(iv);
				yield iv;
			}
			case STREAM, NO_ENCRYPTION -> Bytes.isNotEmpty(keyIV) ? keyIV.clone() : Bytes.EMPTY;
		};
	}
}
