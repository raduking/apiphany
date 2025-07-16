package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum SignatureAlgorithm implements Sizeable {

	/**
	 * RSA/PKCS1/SHA256.
	 */
	RSA_PKCS1_SHA256((short) 0x0401),

	/**
	 * ECDSA/SECP256r1/SHA256.
	 */
	ECDSA_SECP256R1_SHA256((short) 0x0403),

	/**
	 * RSA/PKCS1/SHA384.
	 */
	RSA_PKCS1_SHA384((short) 0x0501),

	/**
	 * ECDSA/SECP384r1/SHA384.
	 */
	ECDSA_SECP384R1_SHA384((short) 0x0503),

	/**
	 * RSA/PKCS1/SHA512.
	 */
	RSA_PKCS1_SHA512((short) 0x0601),

	/**
	 * ECDSA/SECP521r1/SHA512.
	 */
	ECDSA_SECP521R1_SHA512((short) 0x0603),

	/**
	 * RSA/PKCS1/SHA1.
	 */
	RSA_PKCS1_SHA1((short) 0x0201),

	/**
	 * ECDSA/SHA1.
	 */
	ECDSA_SHA1((short) 0x0203);

	public static final int BYTES = 2;

	private static final Map<Short, SignatureAlgorithm> VALUE_MAP = Enums.buildNameMap(values(), SignatureAlgorithm::value);

	private final short value;

	SignatureAlgorithm(final short value) {
		this.value = value;
	}

	public static SignatureAlgorithm fromValue(final short value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	public short value() {
		return value;
	}

	@Override
	public int size() {
		return BYTES;
	}
}
