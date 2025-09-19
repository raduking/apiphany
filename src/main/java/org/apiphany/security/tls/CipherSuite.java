package org.apiphany.security.tls;

import java.util.Map;

import org.apiphany.io.UInt16;
import org.apiphany.security.MessageDigestAlgorithm;
import org.morphix.lang.Enums;

/**
 * Represents all supported TLS cipher suites with their protocol codes and key exchange algorithms.
 * <p>
 * Each enum constant represents a specific cipher suite combination including:
 * <ul>
 * <li>key exchange algorithm</li>
 * <li>bulk encryption algorithm</li>
 * <li>message authentication code (MAC) algorithm</li>
 * </ul>
 *
 * <p>
 * Organized into logical groups (TLS 1.3, ECDHE, DHE, CBC, legacy, etc.) for better readability. Implements
 * {@link TLSObject} for protocol serialization.
 *
 * @see <a href="https://www.iana.org/assignments/tls-parameters/tls-parameters.xhtml">IANA TLS Cipher Suite
 * Registry</a>
 *
 * @author Radu Sebastian LAZIN
 */
public enum CipherSuite implements TLSObject {

	/**
	 * TLS 1.3 AES-256 GCM cipher suite with SHA-384.
	 */
	TLS_AES_256_GCM_SHA384((short) 0x1302,
			KeyExchangeAlgorithm.NONE, BulkCipher.AES_256_GCM, MessageDigestAlgorithm.SHA384),

	/**
	 * TLS 1.3 AES-128 GCM cipher suite with SHA-256.
	 */
	TLS_AES_128_GCM_SHA256((short) 0x1301,
			KeyExchangeAlgorithm.NONE, BulkCipher.AES_128_GCM, MessageDigestAlgorithm.SHA256),

	/**
	 * TLS 1.3 ChaCha20-Poly1305 cipher suite.
	 */
	TLS_CHACHA20_POLY1305_SHA256((short) 0x1303,
			KeyExchangeAlgorithm.NONE, BulkCipher.CHACHA20_POLY1305, MessageDigestAlgorithm.SHA256),

	/**
	 * ECDHE-RSA with ChaCha20-Poly1305 cipher suite.
	 */
	TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256((short) 0xCCA8,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.CHACHA20_POLY1305, MessageDigestAlgorithm.SHA256),

	/**
	 * ECDHE-RSA with AES-256 GCM cipher suite.
	 */
	TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384((short) 0xC030,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_256_GCM, MessageDigestAlgorithm.SHA384),

	/**
	 * ECDHE-RSA with AES-128 GCM cipher suite.
	 */
	TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256((short) 0xC02F,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_128_GCM, MessageDigestAlgorithm.SHA256),

	/**
	 * ECDHE-ECDSA with AES-256 GCM cipher suite.
	 */
	TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384((short) 0xC02C,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_256_GCM, MessageDigestAlgorithm.SHA384),

	/**
	 * ECDHE-ECDSA with AES-128 GCM cipher suite.
	 */
	TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256((short) 0xC02B,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_128_GCM, MessageDigestAlgorithm.SHA256),

	/**
	 * ECDHE-ECDSA with ChaCha20-Poly1305 cipher suite.
	 */
	TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256((short) 0xCCA9,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.CHACHA20_POLY1305, MessageDigestAlgorithm.SHA256),

	/**
	 * DHE-RSA with AES-256 GCM cipher suite.
	 */
	TLS_DHE_RSA_WITH_AES_256_GCM_SHA384((short) 0x009F,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_256_GCM, MessageDigestAlgorithm.SHA384),

	/**
	 * DHE-RSA with ChaCha20-Poly1305 cipher suite.
	 */
	TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256((short) 0xCCAA,
			KeyExchangeAlgorithm.DHE, BulkCipher.CHACHA20_POLY1305, MessageDigestAlgorithm.SHA256),

	/**
	 * DHE-DSS with AES-256 GCM cipher suite.
	 */
	TLS_DHE_DSS_WITH_AES_256_GCM_SHA384((short) 0x00A3,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_256_GCM, MessageDigestAlgorithm.SHA384),

	/**
	 * DHE-RSA with AES-128 GCM cipher suite.
	 */
	TLS_DHE_RSA_WITH_AES_128_GCM_SHA256((short) 0x009E,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_128_GCM, MessageDigestAlgorithm.SHA256),

	/**
	 * DHE-DSS with AES-128 GCM cipher suite.
	 */
	TLS_DHE_DSS_WITH_AES_128_GCM_SHA256((short) 0x00A2,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_128_GCM, MessageDigestAlgorithm.SHA256),

	/**
	 * ECDHE-ECDSA with AES-256 CBC cipher suite.
	 */
	TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384((short) 0xC024,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_256_CBC, MessageDigestAlgorithm.SHA384),

	/**
	 * ECDHE-RSA with AES-256 CBC cipher suite.
	 */
	TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384((short) 0xC028,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_256_CBC, MessageDigestAlgorithm.SHA384),

	/**
	 * ECDHE-ECDSA with AES-128 CBC cipher suite.
	 */
	TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256((short) 0xC023,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_128_CBC, MessageDigestAlgorithm.SHA256),

	/**
	 * ECDHE-RSA with AES-128 CBC cipher suite.
	 */
	TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256((short) 0xC027,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_128_CBC, MessageDigestAlgorithm.SHA256),

	/**
	 * DHE-RSA with AES-256 CBC cipher suite.
	 */
	TLS_DHE_RSA_WITH_AES_256_CBC_SHA256((short) 0x006B,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_256_CBC, MessageDigestAlgorithm.SHA256),

	/**
	 * DHE-DSS with AES-256 CBC cipher suite.
	 */
	TLS_DHE_DSS_WITH_AES_256_CBC_SHA256((short) 0x006A,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_256_CBC, MessageDigestAlgorithm.SHA256),

	/**
	 * DHE-RSA with AES-128 CBC cipher suite.
	 */
	TLS_DHE_RSA_WITH_AES_128_CBC_SHA256((short) 0x0067,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_128_CBC, MessageDigestAlgorithm.SHA256),

	/**
	 * DHE-DSS with AES-128 CBC cipher suite.
	 */
	TLS_DHE_DSS_WITH_AES_128_CBC_SHA256((short) 0x0066,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_128_CBC, MessageDigestAlgorithm.SHA256),

	/**
	 * ECDHE-ECDSA with AES-256 CBC legacy cipher suite.
	 */
	TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA((short) 0xC00A,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_256_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * ECDHE-RSA with AES-256 CBC legacy cipher suite.
	 */
	TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA((short) 0xC014,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_256_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * ECDHE-ECDSA with AES-128 CBC legacy cipher suite.
	 */
	TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA((short) 0xC009,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_128_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * ECDHE-RSA with AES-128 CBC legacy cipher suite.
	 */
	TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA((short) 0xC013,
			KeyExchangeAlgorithm.ECDHE, BulkCipher.AES_128_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * DHE-RSA with AES-256 CBC legacy cipher suite.
	 */
	TLS_DHE_RSA_WITH_AES_256_CBC_SHA((short) 0x0039,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_256_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * DHE-DSS with AES-256 CBC legacy cipher suite.
	 */
	TLS_DHE_DSS_WITH_AES_256_CBC_SHA((short) 0x0038,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_256_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * DHE-RSA with AES-128 CBC legacy cipher suite.
	 */
	TLS_DHE_RSA_WITH_AES_128_CBC_SHA((short) 0x0033,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_128_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * DHE-DSS with AES-128 CBC legacy cipher suite.
	 */
	TLS_DHE_DSS_WITH_AES_128_CBC_SHA((short) 0x0032,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_128_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * RSA with AES-256 GCM cipher suite.
	 */
	TLS_RSA_WITH_AES_256_GCM_SHA384((short) 0x009D,
			KeyExchangeAlgorithm.RSA, BulkCipher.AES_256_GCM, MessageDigestAlgorithm.SHA384),

	/**
	 * RSA with AES-128 GCM cipher suite.
	 */
	TLS_RSA_WITH_AES_128_GCM_SHA256((short) 0x009C,
			KeyExchangeAlgorithm.RSA, BulkCipher.AES_128_GCM, MessageDigestAlgorithm.SHA256),

	/**
	 * RSA with AES-256 CBC cipher suite.
	 */
	TLS_RSA_WITH_AES_256_CBC_SHA256((short) 0x003D,
			KeyExchangeAlgorithm.RSA, BulkCipher.AES_256_CBC, MessageDigestAlgorithm.SHA256),

	/**
	 * RSA with AES-128 CBC cipher suite.
	 */
	TLS_RSA_WITH_AES_128_CBC_SHA256((short) 0x003C,
			KeyExchangeAlgorithm.RSA, BulkCipher.AES_128_CBC, MessageDigestAlgorithm.SHA256),

	/**
	 * RSA with AES-256 CBC legacy cipher suite.
	 */
	TLS_RSA_WITH_AES_256_CBC_SHA((short) 0x0035,
			KeyExchangeAlgorithm.RSA, BulkCipher.AES_256_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * RSA with AES-128 CBC legacy cipher suite.
	 */
	TLS_RSA_WITH_AES_128_CBC_SHA((short) 0x002F,
			KeyExchangeAlgorithm.RSA, BulkCipher.AES_128_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * OpenSSL DHE-RSA with AES-128 CCM-8 cipher suite.
	 */
	DHE_RSA_WITH_AES_128_CCM_8((short) 0xFF9E,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_128_CCM, MessageDigestAlgorithm.SHA256),

	/**
	 * OpenSSL DHE-RSA with AES-256 CCM-8 cipher suite.
	 */
	DHE_RSA_WITH_AES_256_CCM_8((short) 0xFF9F,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_256_CCM, MessageDigestAlgorithm.SHA256),

	/**
	 * OpenSSL DHE-RSA with AES-128 CCM cipher suite.
	 */
	DHE_RSA_WITH_AES_128_CCM((short) 0xFF9C,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_128_CCM, MessageDigestAlgorithm.SHA256),

	/**
	 * OpenSSL DHE-RSA with AES-256 CCM cipher suite.
	 */
	DHE_RSA_WITH_AES_256_CCM((short) 0xFF9D,
			KeyExchangeAlgorithm.DHE, BulkCipher.AES_256_CCM, MessageDigestAlgorithm.SHA256),

	/**
	 * GOST with Kuznyechik CTR cipher suite.
	 */
	TLS_GOSTR341112_256_WITH_KUZNYECHIK_CTR_OMAC((short) 0xCBA9,
			KeyExchangeAlgorithm.NONE, BulkCipher.KUZNYECHIK_CTR, MessageDigestAlgorithm.GOST3411_2012_256),

	/**
	 * GOST with Magma CTR cipher suite.
	 */
	TLS_GOSTR341112_256_WITH_MAGMA_CTR_OMAC((short) 0xCBA8,
			KeyExchangeAlgorithm.NONE, BulkCipher.MAGMA_CTR, MessageDigestAlgorithm.GOST3411_2012_256),

	/**
	 * GOST with 28147 CNT cipher suite.
	 */
	TLS_GOSTR341112_256_WITH_28147_CNT_IMIT((short) 0xCBAA,
			KeyExchangeAlgorithm.NONE, BulkCipher.GOST_28147_CNT, MessageDigestAlgorithm.GOST3411_2012_256),

	/**
	 * ECDHE-RSA with 3DES EDE CBC legacy cipher suite.
	 */
	TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA((short) 0xC012,
			KeyExchangeAlgorithm.ECDHE, BulkCipher._3DES_EDE_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * RSA with 3DES EDE CBC legacy cipher suite.
	 */
	TLS_RSA_WITH_3DES_EDE_CBC_SHA((short) 0x000A,
			KeyExchangeAlgorithm.RSA, BulkCipher._3DES_EDE_CBC, MessageDigestAlgorithm.SHA1),

	/**
	 * RSA export with RC4-56 legacy cipher suite.
	 */
	TLS_RSA_EXPORT1024_WITH_RC4_56_SHA((short) 0x0040,
			KeyExchangeAlgorithm.RSA, BulkCipher.RC4_56, MessageDigestAlgorithm.SHA1),

	/**
	 * Empty renegotiation info signaling cipher suite.
	 */
	TLS_EMPTY_RENEGOTIATION_INFO_SCSV((short) 0x00FF,
			KeyExchangeAlgorithm.NONE, BulkCipher.UNENCRYPTED, MessageDigestAlgorithm.NONE),

	/**
	 * Reserved cipher suite value.
	 */
	RESERVED((short) 0xFFFF,
			KeyExchangeAlgorithm.NONE, BulkCipher.UNENCRYPTED, MessageDigestAlgorithm.NONE),

	/**
	 * Unassigned cipher suite value.
	 */
	UNASSIGNED((short) 0x0100,
			KeyExchangeAlgorithm.NONE, BulkCipher.UNENCRYPTED, MessageDigestAlgorithm.NONE);

	/**
	 * the size in bytes of a cipher suite when serialized (always 2 bytes)
	 */
	public static final int BYTES = 2;

	/**
	 * Value map for easy {@link #fromValue(short)} implementation.
	 */
	private static final Map<Short, CipherSuite> VALUE_MAP = Enums.buildNameMap(values(), CipherSuite::value);

	/**
	 * The TLS encapsulated value.
	 */
	private final short value;

	/**
	 * The key exchange algorithm.
	 */
	private final KeyExchangeAlgorithm keyExchange;

	/**
	 * The bulk encryption algorithm information.
	 */
	private final BulkCipher bulkCipher;

	/**
	 * The message digest algorithm.
	 */
	private final MessageDigestAlgorithm messageDigest;

	/**
	 * Constructs a cipher suite enum constant.
	 *
	 * @param value the 2-byte code assigned to this cipher suite
	 * @param keyExchange the key exchange algorithm used by this suite
	 */
	CipherSuite(final short value, final KeyExchangeAlgorithm keyExchange, final BulkCipher bulkCipher, final MessageDigestAlgorithm messageDigest) {
		this.value = value;
		this.keyExchange = keyExchange;
		this.bulkCipher = bulkCipher;
		this.messageDigest = messageDigest;
	}

	/**
	 * Computes the total key block length for this cipher suite.
	 * <p>
	 * Includes MAC keys, encryption keys, and IVs depending on the {@link BulkCipher} type.
	 *
	 * @return the total number of bytes required for the key block
	 */
	public int totalKeyBlockLength() {
		CipherType type = bulkCipher().type();
		return switch (type) {
			case AEAD -> 2 * (bulkCipher().keyLength() + bulkCipher().fixedIvLength());
			case BLOCK -> 2 * (messageDigest().digestLength() + bulkCipher().keyLength() + bulkCipher().blockSize());
			case STREAM -> 2 * (messageDigest().digestLength() + bulkCipher().keyLength());
			case NO_ENCRYPTION -> 0;
		};
	}

	/**
	 * Looks up a cipher suite by its 2-byte value.
	 *
	 * @param value the cipher suite code to look up
	 * @return the matching cipher suite enum constant
	 * @throws IllegalArgumentException if no matching cipher suite is found
	 */
	public static CipherSuite fromValue(final short value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	/**
	 * Returns the 2-byte code assigned to this cipher suite.
	 *
	 * @return the protocol value for this cipher suite
	 */
	public short value() {
		return value;
	}

	/**
	 * Returns the key exchange algorithm used by this cipher suite.
	 *
	 * @return the key exchange algorithm enum constant
	 */
	public KeyExchangeAlgorithm keyExchange() {
		return keyExchange;
	}

	/**
	 * Returns the message digest algorithm used by this cipher suite.
	 *
	 * @return the message digest algorithm enum constant
	 */
	public MessageDigestAlgorithm messageDigest() {
		return messageDigest;
	}

	/**
	 * Returns the bulk encryption algorithm information.
	 *
	 * @return the bulk encryption algorithm information
	 */
	public BulkCipher bulkCipher() {
		return bulkCipher;
	}

	/**
	 * Returns the size of this cipher suite when serialized.
	 *
	 * @return always returns {@value #BYTES} (2) as cipher suites are always two bytes
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Returns the binary representation of this cipher suite.
	 *
	 * @return 2-byte array containing the cipher suite code
	 */
	@Override
	public byte[] toByteArray() {
		return UInt16.toByteArray(value);
	}
}
