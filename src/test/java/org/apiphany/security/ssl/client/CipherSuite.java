package org.apiphany.security.ssl.client;

import java.util.Map;

import org.apiphany.io.UInt16;
import org.apiphany.security.tls.KeyExchangeAlgorithm;
import org.apiphany.security.tls.TLSObject;
import org.morphix.lang.Enums;

public enum CipherSuite implements TLSObject {

    /**
     * TLS 1.3 suites (no traditional hex codes, using pseudo-values)
     */
    TLS_AES_256_GCM_SHA384((short) 0x1302, KeyExchangeAlgorithm.NONE),
    TLS_AES_128_GCM_SHA256((short) 0x1301, KeyExchangeAlgorithm.NONE),
    TLS_CHACHA20_POLY1305_SHA256((short) 0x1303, KeyExchangeAlgorithm.NONE),

    /**
     * ECDHE suites.
     */
    TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256((short) 0xCCA8, KeyExchangeAlgorithm.ECDHE),
    TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384((short) 0xC030, KeyExchangeAlgorithm.ECDHE),
    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256((short) 0xC02F, KeyExchangeAlgorithm.ECDHE),
    TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384((short) 0xC02C, KeyExchangeAlgorithm.ECDHE),
    TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256((short) 0xC02B, KeyExchangeAlgorithm.ECDHE),
    TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256((short) 0xCCA9, KeyExchangeAlgorithm.ECDHE),

    /**
     * DHE suites.
     */
    TLS_DHE_RSA_WITH_AES_256_GCM_SHA384((short) 0x009F, KeyExchangeAlgorithm.DHE),
    TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256((short) 0xCCAA, KeyExchangeAlgorithm.DHE),
    TLS_DHE_DSS_WITH_AES_256_GCM_SHA384((short) 0x00A3, KeyExchangeAlgorithm.DHE),
    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256((short) 0x009E, KeyExchangeAlgorithm.DHE),
    TLS_DHE_DSS_WITH_AES_128_GCM_SHA256((short) 0x00A2, KeyExchangeAlgorithm.DHE),

    /**
     * CBC suites.
     */
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384((short) 0xC024, KeyExchangeAlgorithm.ECDHE),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384((short) 0xC028, KeyExchangeAlgorithm.ECDHE),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256((short) 0xC023, KeyExchangeAlgorithm.ECDHE),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256((short) 0xC027, KeyExchangeAlgorithm.ECDHE),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA256((short) 0x006B, KeyExchangeAlgorithm.DHE),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA256((short) 0x006A, KeyExchangeAlgorithm.DHE),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA256((short) 0x0067, KeyExchangeAlgorithm.DHE),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA256((short) 0x0066, KeyExchangeAlgorithm.DHE),

    /**
     * Legacy suites.
     */
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA((short) 0xC00A, KeyExchangeAlgorithm.ECDHE),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA((short) 0xC014, KeyExchangeAlgorithm.ECDHE),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA((short) 0xC009, KeyExchangeAlgorithm.ECDHE),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA((short) 0xC013, KeyExchangeAlgorithm.ECDHE),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA((short) 0x0039, KeyExchangeAlgorithm.DHE),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA((short) 0x0038, KeyExchangeAlgorithm.DHE),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA((short) 0x0033, KeyExchangeAlgorithm.DHE),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA((short) 0x0032, KeyExchangeAlgorithm.DHE),

    /**
     * RSA suites.
     */
    TLS_RSA_WITH_AES_256_GCM_SHA384((short) 0x009D, KeyExchangeAlgorithm.RSA),
    TLS_RSA_WITH_AES_128_GCM_SHA256((short) 0x009C, KeyExchangeAlgorithm.RSA),
    TLS_RSA_WITH_AES_256_CBC_SHA256((short) 0x003D, KeyExchangeAlgorithm.RSA),
    TLS_RSA_WITH_AES_128_CBC_SHA256((short) 0x003C, KeyExchangeAlgorithm.RSA),
    TLS_RSA_WITH_AES_256_CBC_SHA((short) 0x0035, KeyExchangeAlgorithm.RSA),
    TLS_RSA_WITH_AES_128_CBC_SHA((short) 0x002F, KeyExchangeAlgorithm.RSA),

    /**
     * Open SSL suites.
     */
    DHE_RSA_WITH_AES_128_CCM_8((short) 0xFF9E, KeyExchangeAlgorithm.DHE),
    DHE_RSA_WITH_AES_256_CCM_8((short) 0xFF9F, KeyExchangeAlgorithm.DHE),
    DHE_RSA_WITH_AES_128_CCM((short) 0xFF9C, KeyExchangeAlgorithm.DHE),
    DHE_RSA_WITH_AES_256_CCM((short) 0xFF9D, KeyExchangeAlgorithm.DHE),

    /**
     * GOST suites.
     */
    TLS_GOSTR341112_256_WITH_KUZNYECHIK_CTR_OMAC((short) 0xCBA9, KeyExchangeAlgorithm.NONE),
    TLS_GOSTR341112_256_WITH_MAGMA_CTR_OMAC((short) 0xCBA8, KeyExchangeAlgorithm.NONE),
    TLS_GOSTR341112_256_WITH_28147_CNT_IMIT((short) 0xCBAA, KeyExchangeAlgorithm.NONE),

    /**
     * Old suites.
     */
	TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA((short) 0xC012, KeyExchangeAlgorithm.ECDHE),
	TLS_RSA_WITH_3DES_EDE_CBC_SHA((short) 0x000A, KeyExchangeAlgorithm.RSA),
	TLS_RSA_EXPORT1024_WITH_RC4_56_SHA((short) 0x0040, KeyExchangeAlgorithm.RSA),

    /**
     * Special.
     */
    TLS_EMPTY_RENEGOTIATION_INFO_SCSV((short) 0x00FF, KeyExchangeAlgorithm.NONE),
	RESERVED((short) 0xFFFF, KeyExchangeAlgorithm.NONE),
	UNASSIGNED((short) 0x0100, KeyExchangeAlgorithm.NONE);

	public static final int BYTES = 2;

	private static final Map<Short, CipherSuite> VALUE_MAP = Enums.buildNameMap(values(), CipherSuite::value);

	private final short value;

	private final KeyExchangeAlgorithm keyExchange;

	CipherSuite(final short value, final KeyExchangeAlgorithm keyExchange) {
		this.value = value;
		this.keyExchange = keyExchange;
	}

	public static CipherSuite fromValue(final short value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	public short value() {
		return value;
	}

	public KeyExchangeAlgorithm keyExchange() {
		return keyExchange;
	}

	@Override
	public int sizeOf() {
		return BYTES;
	}

	@Override
	public byte[] toByteArray() {
		return UInt16.toByteArray(value);
	}
}
