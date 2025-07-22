package org.apiphany.security.tls;

import java.util.List;
import java.util.Map;

import org.apiphany.io.UInt16;
import org.morphix.lang.Enums;

/**
 * Represents signature algorithms supported in TLS 1.2+ handshakes.
 * <p>
 * Each algorithm combines a cryptographic scheme (e.g., RSA, ECDSA) with a hash function (e.g., SHA256). Values are
 * encoded as 2-byte identifiers per
 * <a href="https://www.iana.org/assignments/tls-parameters/tls-parameters.xhtml#tls-signaturescheme">IANA TLS
 * SignatureScheme Registry</a>.
 * </p>
 *
 * <p>
 * <b>Security Recommendations:</b>
 * </p>
 * <ul>
 * <li><b>Preferred:</b> {@link #EDDSA_ED25519}, {@link #ECDSA_SECP256R1_SHA256}, and RSA-PSS variants.</li>
 * <li><b>Legacy:</b> PKCS#1-based RSA and SHA-1 algorithms should be disabled in modern deployments.</li>
 * <li><b>Deprecated:</b> DSA (weak key sizes) and ECDSA-SHA1 (collision attacks).</li>
 * </ul>
 *
 * <p>
 * <b>Thread Safety:</b>
 * </p>
 * This enum and its methods are thread-safe.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8446#section-4.2.3">TLS 1.3 Signature Algorithms</a>
 *
 * @author Radu Sebastian LAZIN
 */
public enum SignatureAlgorithm implements TLSObject {

	/**
	 * RSA/PKCS1/SHA224. RSA with PKCS#1 v1.5 padding and SHA-224. TLS identifier: {@code 0x0301}. Not recommended due to
	 * PKCS#1v1.5 vulnerabilities.
	 */
	RSA_PKCS1_SHA224((short) 0x301),

	/**
	 * DSA/SHA224. DSA with SHA-224. TLS identifier: {@code 0x0302}. Avoid (DSA requires precise 1024/2048-bit keys).
	 */
	DSA_SHA224((short) 0x0302),

	/**
	 * ECDSA/SHA224. ECDSA with SHA-224. TLS identifier: {@code 0x0303}. Prefer {@link #ECDSA_SECP256R1_SHA256} instead.
	 */
	ECDSA_SHA224((short) 0x0303),

	/**
	 * RSA/PKCS1/SHA256. RSA with PKCS#1 v1.5 padding and SHA-256. TLS identifier: {@code 0x0401}. Acceptable if RSA-PSS is
	 * unavailable.
	 */
	RSA_PKCS1_SHA256((short) 0x0401),

	/**
	 * DSA/SHA256. DSA with SHA-256. TLS identifier: {@code 0x0402}. Avoid (see {@link #DSA_SHA224}).
	 */
	DSA_SHA256((short) 0x0402),

	/**
	 * ECDSA/SECP256r1/SHA256. ECDSA using NIST P-256 curve with SHA-256. TLS identifier: {@code 0x0403}. Recommended for
	 * ECDSA deployments.
	 */
	ECDSA_SECP256R1_SHA256((short) 0x0403),

	/**
	 * RSA/PKCS1/SHA384. RSA with PKCS#1 v1.5 padding and SHA-384. TLS identifier: {@code 0x0501}. Prefer
	 * {@link #RSA_PSS_RSAE_SHA384}.
	 */
	RSA_PKCS1_SHA384((short) 0x0501),

	/**
	 * DSA/SHA384. DSA with SHA-384. TLS identifier: {@code 0x0502}. Avoid (see {@link #DSA_SHA224}).
	 */
	DSA_SHA384((short) 0x0502),

	/**
	 * ECDSA/SECP384r1/SHA384. ECDSA using NIST P-384 curve with SHA-384. TLS identifier: {@code 0x0503}. Use for
	 * higher-security ECDSA needs.
	 */
	ECDSA_SECP384R1_SHA384((short) 0x0503),

	/**
	 * RSA/PKCS1/SHA512. RSA with PKCS#1 v1.5 padding and SHA-512. TLS identifier: {@code 0x0601}. Prefer
	 * {@link #RSA_PSS_RSAE_SHA512}.
	 */
	RSA_PKCS1_SHA512((short) 0x0601),

	/**
	 * DSA/SHA512. DSA with SHA-512. TLS identifier: {@code 0x0602}. Avoid (see {@link #DSA_SHA224}).
	 */
	DSA_SHA512((short) 0x0602),

	/**
	 * ECDSA/SECP521r1/SHA512. ECDSA using NIST P-521 curve with SHA-512. TLS identifier: {@code 0x0603}. Rarely needed due
	 * to performance overhead.
	 */
	ECDSA_SECP521R1_SHA512((short) 0x0603),

	/**
	 * RSA/PKCS1/SHA1. RSA with PKCS#1 v1.5 padding and SHA-1. TLS identifier: {@code 0x0201}. <b>Deprecated</b> due to
	 * SHA-1 collisions.
	 */
	@Deprecated
	RSA_PKCS1_SHA1((short) 0x0201),

	/**
	 * ECDSA/SHA1. ECDSA with SHA-1. TLS identifier: {@code 0x0203}. <b>Deprecated</b> (see {@link #RSA_PKCS1_SHA1}).
	 */
	@Deprecated
	ECDSA_SHA1((short) 0x0203),

	/**
	 * EdDSA/Ed25519. Ed25519 (EdDSA with Curve25519). TLS identifier: {@code 0x0807}. <b>Preferred</b> for modern
	 * deployments (fast, secure).
	 */
	EDDSA_ED25519((short) 0x0807),

	/**
	 * EdDSA/Ed448. Ed448 (EdDSA with Curve448). TLS identifier: {@code 0x0808}. Use for post-quantum resistance where
	 * performance is acceptable.
	 */
	EDDSA_ED448((short) 0x0808),

	/**
	 * RSA/PSS/PSS/SHA256. RSA-PSS with PSS padding using SHA-256 and MGF1. TLS identifier: {@code 0x0809}. Recommended over
	 * PKCS#1 for RSA.
	 */
	RSA_PSS_PSS_SHA256((short) 0x0809),

	/**
	 * RSA/PSS/RSAE/SHA256. RSA-PSS with PKCS#1v1.5 key and SHA-256. TLS identifier: {@code 0x0804}. Widely supported
	 * RSA-PSS variant.
	 */
	RSA_PSS_RSAE_SHA256((short) 0x0804),

	/**
	 * RSA/PSS/RSAE/SHA384. RSA-PSS with PKCS#1v1.5 key and SHA-384. TLS identifier: {@code 0x0805}. Higher-security RSA-PSS
	 * option.
	 */
	RSA_PSS_RSAE_SHA384((short) 0x0805),

	/**
	 * RSA/PSS/RSAE/SHA512. RSA-PSS with PKCS#1v1.5 key and SHA-512. TLS identifier: {@code 0x0806}. Highest-security
	 * RSA-PSS option.
	 */
	RSA_PSS_RSAE_SHA512((short) 0x0806),

	/**
	 * RSA/PSS/PSS/SHA384. RSA-PSS with PSS padding using SHA-384 and MGF1. TLS identifier: {@code 0x080A}. Less common than
	 * {@link #RSA_PSS_RSAE_SHA384}.
	 */
	RSA_PSS_PSS_SHA384((short) 0x080A),

	/**
	 * RSA/PSS/PSS/SHA512. RSA-PSS with PSS padding using SHA-512 and MGF1. TLS identifier: {@code 0x080B}. Less common than
	 * {@link #RSA_PSS_RSAE_SHA512}.
	 */
	RSA_PSS_PSS_SHA512((short) 0x080B);

	/**
	 * A list of algorithms considered cryptographically strong for TLS 1.3+. Includes:
	 * <ul>
	 * <li>RSA-PSS variants</li>
	 * <li>ECDSA with NIST P-256/P-384</li>
	 * <li>Ed25519</li>
	 * </ul>
	 */
	public static final List<SignatureAlgorithm> STRONG_ALGORITHMS = List.of(
			RSA_PSS_RSAE_SHA256,
			RSA_PSS_RSAE_SHA384,
			RSA_PSS_RSAE_SHA512,
			ECDSA_SECP256R1_SHA256,
			ECDSA_SECP384R1_SHA384,
			EDDSA_ED25519);

	/**
	 * The size (in bytes) of a {@link SignatureAlgorithm} identifier in TLS messages.
	 */
	public static final int BYTES = 2;

	/**
	 * Value map for easy {@link #fromValue(short)} implementation.
	 */
	private static final Map<Short, SignatureAlgorithm> VALUE_MAP = Enums.buildNameMap(values(), SignatureAlgorithm::value);

	/**
	 * The actual TLS encapsulated value.
	 */
	private final short value;

	/**
	 * Creates a {@link SignatureAlgorithm} with the given TLS-encoded value.
	 *
	 * @param value The 2-byte identifier assigned to this algorithm in the IANA registry.
	 */
	SignatureAlgorithm(final short value) {
		this.value = value;
	}

	/**
	 * Parses a TLS-encoded signature algorithm identifier.
	 *
	 * @param value the 2-byte identifier (e.g., {@code 0x0807} for {@link #EDDSA_ED25519}).
	 * @return the corresponding {@link SignatureAlgorithm}, or {@code null} if unmatched.
	 * @throws IllegalArgumentException If {@code value} is invalid (if enforced by {@link Enums#from}).
	 */
	public static SignatureAlgorithm fromValue(final short value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	/**
	 * Returns the TLS-encoded 2-byte identifier for this algorithm.
	 *
	 * @return the IANA-assigned value (e.g., {@code 0x0807} for Ed25519).
	 */
	public short value() {
		return value;
	}

	/**
	 * Returns the size of this object when serialized in TLS (always {@value #BYTES} bytes). Signature algorithms are
	 * always represented as 2-byte values in TLS handshakes.
	 *
	 * @return the size of this object when serialized
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Serializes this {@link SignatureAlgorithm} to its TLS-encoded 2-byte representation. The returned array is formatted
	 * according to {@link UInt16#toByteArray(short)}.
	 *
	 * @return the object serialized as byte array
	 */
	@Override
	public byte[] toByteArray() {
		return UInt16.toByteArray(value);
	}
}
