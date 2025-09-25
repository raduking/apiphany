package org.apiphany.security.tls;

import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the set of keys derived from the TLS key block for a given {@link CipherSuite}.
 * <p>
 * Depending on the {@link BulkCipher} type, this may include:
 * <ul>
 * <li>MAC keys for client and server (for BLOCK or STREAM ciphers)</li>
 * <li>Encryption keys for client and server</li>
 * <li>IVs for client and server (for AEAD or BLOCK ciphers)</li>
 * </ul>
 * <p>
 * Use {@link #from(byte[], CipherSuite)} to parse a TLS key block dynamically according to the cipher suite.
 * <p>
 * This class supports AEAD, block, stream, and no-encryption cipher types.
 *
 * @author Radu Sebastian LAZIN
 */
public class ExchangeKeys {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeKeys.class);

	/**
	 * Client MAC key (BLOCK or STREAM ciphers).
	 */
	private byte[] clientMacKey;

	/**
	 * Server MAC key (BLOCK or STREAM ciphers).
	 */
	private byte[] serverMacKey;

	/**
	 * Client write key.
	 */
	private byte[] clientWriteKey;

	/**
	 * Server write key.
	 */
	private byte[] serverWriteKey;

	/**
	 * Client IV (AEAD or BLOCK ciphers).
	 */
	private byte[] clientIV;

	/**
	 * Server IV (AEAD or BLOCK ciphers).
	 */
	private byte[] serverIV;

	/**
	 * Hide constructor.
	 */
	private ExchangeKeys() {
		// empty
	}

	/**
	 * Parses a TLS key block into {@link ExchangeKeys} according to the cipher suite.
	 * <p>
	 * The number of bytes read depends on the {@link BulkCipher} type:
	 * <ul>
	 * <li>{@link CipherType#AEAD}: reads encryption keys and fixed IVs only</li>
	 * <li>{@link CipherType#BLOCK}: reads MAC keys, encryption keys, and IVs</li>
	 * <li>{@link CipherType#STREAM}: reads MAC keys and encryption keys (no IVs)</li>
	 * <li>{@link CipherType#NO_ENCRYPTION}: reads nothing</li>
	 * </ul>
	 *
	 * @param keyBlock the concatenated key block derived from the PRF
	 * @param suite the cipher suite to determine key lengths and types
	 * @return a new {@link ExchangeKeys} instance
	 */
	public static ExchangeKeys from(final byte[] keyBlock, final CipherSuite suite) {
		LOGGER.debug("keyBlock: {}", Hex.string(keyBlock));
		LOGGER.debug("keyBlock length: {}", keyBlock.length);

		BulkCipher bulkCipher = suite.bulkCipher();
		CipherType type = bulkCipher.type();

		ExchangeKeys exchangeKeys = new ExchangeKeys();

		int macLen = suite.messageDigest().digestLength();
		int keyLen = bulkCipher.keyLength();
		int ivLen;

		ByteBuffer buffer = ByteBuffer.wrap(keyBlock);

		switch (type) {
			case AEAD -> {
				ivLen = bulkCipher.fixedIvLength();
				exchangeKeys.clientWriteKey = read(buffer, keyLen);
				exchangeKeys.serverWriteKey = read(buffer, keyLen);
				exchangeKeys.clientIV = read(buffer, ivLen);
				exchangeKeys.serverIV = read(buffer, ivLen);
			}
			case BLOCK -> {
				ivLen = bulkCipher.blockSize();
				exchangeKeys.clientMacKey = read(buffer, macLen);
				exchangeKeys.serverMacKey = read(buffer, macLen);
				exchangeKeys.clientWriteKey = read(buffer, keyLen);
				exchangeKeys.serverWriteKey = read(buffer, keyLen);
				exchangeKeys.clientIV = read(buffer, ivLen);
				exchangeKeys.serverIV = read(buffer, ivLen);
			}
			case STREAM -> {
				exchangeKeys.clientMacKey = read(buffer, macLen);
				exchangeKeys.serverMacKey = read(buffer, macLen);
				exchangeKeys.clientWriteKey = read(buffer, keyLen);
				exchangeKeys.serverWriteKey = read(buffer, keyLen);
				// no IVs for stream ciphers
			}
			case NO_ENCRYPTION -> {
				// no keys or IVs at all
			}
			default -> {
				// empty
			}
		}
		LOGGER.debug("clientMacKey: {}", Hex.string(exchangeKeys.clientMacKey));
		LOGGER.debug("serverMacKey: {}", Hex.string(exchangeKeys.serverMacKey));
		LOGGER.debug("clientWriteKey: {}", Hex.string(exchangeKeys.clientWriteKey));
		LOGGER.debug("serverWriteKey: {}", Hex.string(exchangeKeys.serverWriteKey));
		LOGGER.debug("clientIV: {}", Hex.string(exchangeKeys.clientIV));
		LOGGER.debug("serverIV: {}", Hex.string(exchangeKeys.serverIV));
		return exchangeKeys;
	}

	/**
	 * Reads a byte array of the specified length from the given {@link ByteBuffer}.
	 *
	 * @param buf the byte buffer to read from
	 * @param len number of bytes to read
	 * @return byte array of length {@code len} read from the buffer
	 */
	private static byte[] read(final ByteBuffer buf, final int len) {
		if (len == 0) {
			return new byte[0];
		}
		byte[] b = new byte[len];
		buf.get(b);
		return b;
	}

	/**
	 * @see #toString()
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the client MAC key (BLOCK or STREAM ciphers).
	 *
	 * @return the client MAC key (BLOCK or STREAM ciphers)
	 */
	public byte[] getClientMacKey() {
		return clientMacKey;
	}

	/**
	 * Returns the server MAC key (BLOCK or STREAM ciphers).
	 *
	 * @return the server MAC key (BLOCK or STREAM ciphers)
	 */
	public byte[] getServerMacKey() {
		return serverMacKey;
	}

	/**
	 * Returns the client write key.
	 *
	 * @return the client write key
	 */
	public byte[] getClientWriteKey() {
		return clientWriteKey;
	}

	/**
	 * Returns the server write key.
	 *
	 * @return the server write key
	 */
	public byte[] getServerWriteKey() {
		return serverWriteKey;
	}

	/**
	 * Returns the client IV (AEAD or BLOCK ciphers).
	 *
	 * @return the client IV (AEAD or BLOCK ciphers)
	 */
	public byte[] getClientIV() {
		return clientIV;
	}

	/**
	 * Returns the server IV (AEAD or BLOCK ciphers).
	 *
	 * @return the server IV (AEAD or BLOCK ciphers)
	 */
	public byte[] getServerIV() {
		return serverIV;
	}
}
