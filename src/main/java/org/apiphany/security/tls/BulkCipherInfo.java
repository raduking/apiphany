package org.apiphany.security.tls;

/**
 * Immutable record holding algorithm parameters for a TLS bulk cipher.
 * <p>
 * This class is a simple data carrier for cipher characteristics such as key lengths, IV sizes, block sizes, and
 * authentication tag sizes. It is used by {@link BulkCipher} to encapsulate the low-level details of each supported
 * cipher.
 * <p>
 * Unlike {@code BulkCipher}, this record contains no behavior; it is purely descriptive.
 *
 * @param algorithm the base algorithm name (e.g., "AES", "CHACHA20")
 * @param keyLength the length of the encryption key in bytes
 * @param blockSize the block size in bytes (for block ciphers, otherwise -1)
 * @param fixedIvLength the length of the fixed IV portion in bytes (used in AEAD modes)
 * @param explicitNonceLength the length of the explicit nonce in bytes (used in AEAD modes)
 * @param type the cipher type (AEAD, BLOCK, STREAM, or NO_ENCRYPTION)
 * @param macKeyLength the length of the MAC key in bytes (or 0 / -1 if not applicable)
 * @param tagLength the authentication tag length in bytes (for AEAD, otherwise -1)
 *
 * @author Radu Sebastian LAZIN
 */
public record BulkCipherInfo(
		String algorithm,
		int keyLength,
		int blockSize,
		int fixedIvLength,
		int explicitNonceLength,
		CipherType type,
		int macKeyLength,
		int tagLength) {

	/**
	 * Factory method to construct the information object.
	 *
	 * @param algorithm the base algorithm name (e.g., "AES", "CHACHA20")
	 * @param keyLength the length of the encryption key in bytes
	 * @param blockSize the block size in bytes (for block ciphers, otherwise -1)
	 * @param fixedIvLength the length of the fixed IV portion in bytes (used in AEAD modes)
	 * @param explicitNonceLength the length of the explicit nonce in bytes (used in AEAD modes)
	 * @param type the cipher type (AEAD, BLOCK, STREAM, or NO_ENCRYPTION)
	 * @param macKeyLength the length of the MAC key in bytes (or 0 / -1 if not applicable)
	 * @param tagLength the authentication tag length in bytes (for AEAD, otherwise -1)
	 * @return a new bulk cipher info object
	 */
	public static BulkCipherInfo of(
			final String algorithm,
			final int keyLength,
			final int blockSize,
			final int fixedIvLength,
			final int explicitNonceLength,
			final CipherType type,
			final int macKeyLength,
			final int tagLength) {
		return new BulkCipherInfo(
				algorithm,
				keyLength,
				blockSize,
				fixedIvLength,
				explicitNonceLength,
				type,
				macKeyLength,
				tagLength);
	}
}
