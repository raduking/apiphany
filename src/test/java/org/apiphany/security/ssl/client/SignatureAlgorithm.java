package org.apiphany.security.ssl.client;

import java.util.List;
import java.util.Map;

import org.apiphany.io.UInt16;
import org.apiphany.security.tls.TLSObject;
import org.morphix.lang.Enums;

public enum SignatureAlgorithm implements TLSObject {

	/**
	 * RSA/PKCS1/SHA224
	 */
	RSA_PKCS1_SHA224((short) 0x301),

	/**
	 * DSA/SHA224.
	 */
	DSA_SHA224((short) 0x0302),

	/**
	 * ECDSA/SHA224.
	 */
	ECDSA_SHA224((short) 0x0303),

	/**
	 * RSA/PKCS1/SHA256.
	 */
	RSA_PKCS1_SHA256((short) 0x0401),

	/**
	 * DSA/SHA256.
	 */
	DSA_SHA256((short) 0x0402),

	/**
	 * ECDSA/SECP256r1/SHA256.
	 */
	ECDSA_SECP256R1_SHA256((short) 0x0403),

	/**
	 * RSA/PKCS1/SHA384.
	 */
	RSA_PKCS1_SHA384((short) 0x0501),

	/**
	 * DSA/SHA384.
	 */
	DSA_SHA384((short) 0x0502),

	/**
	 * ECDSA/SECP384r1/SHA384.
	 */
	ECDSA_SECP384R1_SHA384((short) 0x0503),

	/**
	 * RSA/PKCS1/SHA512.
	 */
	RSA_PKCS1_SHA512((short) 0x0601),

	/**
	 * DSA/SHA512.
	 */
	DSA_SHA512((short) 0x0602),

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
	ECDSA_SHA1((short) 0x0203),

	/**
	 * EdDSA/Ed25519.
	 */
	EDDSA_ED25519((short) 0x0807),

	/**
	 * EdDSA/Ed448.
	 */
	EDDSA_ED448((short) 0x0808),

	/**
	 * RSA/PSS/PSS/SHA256.
	 */
	RSA_PSS_PSS_SHA256((short) 0x0809),

	/**
	 * RSA/PSS/RSAE/SHA256.
	 */
	RSA_PSS_RSAE_SHA256((short) 0x0804),

	/**
	 * RSA/PSS/RSAE/SHA384.
	 */
	RSA_PSS_RSAE_SHA384((short) 0x0805),

	/**
	 * RSA/PSS/RSAE/SHA512.
	 */
	RSA_PSS_RSAE_SHA512((short) 0x0806),

	/**
	 * RSA/PSS/PSS/SHA384.
	 */
	RSA_PSS_PSS_SHA384((short) 0x080A),

	/**
	 * RSA/PSS/PSS/SHA512.
	 */
	RSA_PSS_PSS_SHA512((short) 0x080B);

	public static final List<SignatureAlgorithm> STRONG_ALGORITHMS = List.of(
			RSA_PSS_RSAE_SHA256,
			RSA_PSS_RSAE_SHA384,
			RSA_PSS_RSAE_SHA512,
			ECDSA_SECP256R1_SHA256,
			ECDSA_SECP384R1_SHA384,
			EDDSA_ED25519
	);

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
	public int sizeOf() {
		return BYTES;
	}

	@Override
	public byte[] toByteArray() {
		return UInt16.toByteArray(value);
	}
}
