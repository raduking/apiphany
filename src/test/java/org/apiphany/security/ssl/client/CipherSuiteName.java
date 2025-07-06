package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum CipherSuiteName {

    /**
     * TLS 1.3 suites (no traditional hex codes, using pseudo-values)
     */
    TLS_AES_256_GCM_SHA384((short) 0x1302),
    TLS_AES_128_GCM_SHA256((short) 0x1301),
    TLS_CHACHA20_POLY1305_SHA256((short) 0x1303),

    /**
     * ECDHE suites.
     */
    TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256((short) 0xCCA8),
    TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384((short) 0xC030),
    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256((short) 0xC02F),
    TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384((short) 0xC02C),
    TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256((short) 0xC02B),
    TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256((short) 0xCCA9),

    /**
     * DHE suites.
     */
    TLS_DHE_RSA_WITH_AES_256_GCM_SHA384((short) 0x009F),
    TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256((short) 0xCCAA),
    TLS_DHE_DSS_WITH_AES_256_GCM_SHA384((short) 0x00A3),
    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256((short) 0x009E),
    TLS_DHE_DSS_WITH_AES_128_GCM_SHA256((short) 0x00A2),

    /**
     * CBC suites.
     */
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384((short) 0xC024),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384((short) 0xC028),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256((short) 0xC023),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256((short) 0xC027),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA256((short) 0x006B),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA256((short) 0x006A),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA256((short) 0x0067),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA256((short) 0x0040),

    /**
     * Legacy suites.
     */
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA((short) 0xC00A),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA((short) 0xC014),
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA((short) 0xC009),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA((short) 0xC013),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA((short) 0x0039),
    TLS_DHE_DSS_WITH_AES_256_CBC_SHA((short) 0x0038),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA((short) 0x0033),
    TLS_DHE_DSS_WITH_AES_128_CBC_SHA((short) 0x0032),

    /**
     * RSA suites.
     */
    TLS_RSA_WITH_AES_256_GCM_SHA384((short) 0x009D),
    TLS_RSA_WITH_AES_128_GCM_SHA256((short) 0x009C),
    TLS_RSA_WITH_AES_256_CBC_SHA256((short) 0x003D),
    TLS_RSA_WITH_AES_128_CBC_SHA256((short) 0x003C),
    TLS_RSA_WITH_AES_256_CBC_SHA((short) 0x0035),
    TLS_RSA_WITH_AES_128_CBC_SHA((short) 0x002F),

    /**
     * Old suites.
     */
	TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA((short) 0xC012),
	TLS_RSA_WITH_3DES_EDE_CBC_SHA((short) 0x000A),

    /**
     * Special.
     */
    TLS_EMPTY_RENEGOTIATION_INFO_SCSV((short) 0x00FF);

	public static final int BYTES = 2;

	private static final Map<Short, CipherSuiteName> VALUE_MAP = Enums.buildNameMap(values(), CipherSuiteName::value);

	private short value;

	CipherSuiteName(final short value) {
		this.value = value;
	}

	public static CipherSuiteName fromValue(final short value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	public short value() {
		return value;
	}

}
