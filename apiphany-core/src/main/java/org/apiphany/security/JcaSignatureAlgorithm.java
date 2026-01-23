package org.apiphany.security;

import java.security.Signature;

/**
 * Enumeration of standard JCA (Java Cryptography Architecture) signature algorithm names.
 * <p>
 * These constants represent canonical algorithm identifiers accepted by {@link Signature#getInstance(String)} across
 * all compliant providers.
 *
 * <h2>Mapping Notes</h2>
 * <ul>
 * <li>RSA (PKCS#1 v1.5) → {@code SHAxxxwithRSA}</li>
 * <li>RSA-PSS → {@code RSASSA-PSS} (provider may require explicit {@link java.security.spec.PSSParameterSpec})</li>
 * <li>ECDSA → {@code SHAxxxwithECDSA}</li>
 * <li>DSA (legacy) → {@code SHAxxxwithDSA}</li>
 * <li>EdDSA → {@code Ed25519} or {@code Ed448}</li>
 * </ul>
 *
 * <p>
 * Algorithm availability depends on the installed security providers and JDK version:
 * <ul>
 * <li>{@code Ed25519} and {@code Ed448} are available since Java 15.</li>
 * <li>{@code RSASSA-PSS} is available since Java 11 (in SunRsaSign provider).</li>
 * </ul>
 *
 * @see Signature
 * @see <a href=
 * "https://docs.oracle.com/en/java/javase/21/security/oracle-providers.html#GUID-85D8C1B8-9C68-4AE5-833D-72786C5F49B1">
 * Oracle JCA Standard Algorithm Names</a>
 *
 * @author Radu Sebastian LAZIN
 */
public enum JcaSignatureAlgorithm {

	/**
	 * RSA signature using SHA-1 hashing. Weak and deprecated for new use.
	 */
	SHA1_WITH_RSA("SHA1withRSA"),

	/**
	 * RSA signature using SHA-224 hashing. Rarely used outside FIPS contexts.
	 */
	SHA224_WITH_RSA("SHA224withRSA"),

	/**
	 * RSA signature using SHA-256 hashing. Common modern default for RSA-based signatures.
	 */
	SHA256_WITH_RSA("SHA256withRSA"),

	/**
	 * RSA signature using SHA-384 hashing. Typically used with RSA-3072 or RSA-4096 keys.
	 */
	SHA384_WITH_RSA("SHA384withRSA"),

	/**
	 * RSA signature using SHA-512 hashing. Suitable for high-assurance or large-key applications.
	 */
	SHA512_WITH_RSA("SHA512withRSA"),

	/**
	 * Probabilistic RSA signature with PSS padding. Preferred for new RSA applications.
	 */
	RSASSA_PSS("RSASSA-PSS"),

	/**
	 * ECDSA signature using SHA-1 hashing. Legacy, not recommended for new use.
	 */
	SHA1_WITH_ECDSA("SHA1withECDSA"),

	/**
	 * ECDSA signature using SHA-224 hashing. Rarely used, mainly in FIPS-compliant environments.
	 */
	SHA224_WITH_ECDSA("SHA224withECDSA"),

	/**
	 * ECDSA signature using SHA-256 hashing. Standard choice for P-256 and modern TLS.
	 */
	SHA256_WITH_ECDSA("SHA256withECDSA"),

	/**
	 * ECDSA signature using SHA-384 hashing. Standard for P-384 and TLS 1.2+ with ECDHE.
	 */
	SHA384_WITH_ECDSA("SHA384withECDSA"),

	/**
	 * ECDSA signature using SHA-512 hashing. Typically paired with P-521 curve.
	 */
	SHA512_WITH_ECDSA("SHA512withECDSA"),

	/**
	 * DSA signature using SHA-1 hashing. Obsolete, historically used in early X.509 and TLS.
	 */
	SHA1_WITH_DSA("SHA1withDSA"),

	/**
	 * DSA signature using SHA-224 hashing. Rarely implemented; used in older FIPS suites.
	 */
	SHA224_WITH_DSA("SHA224withDSA"),

	/**
	 * DSA signature using SHA-256 hashing. Modernized DSA variant, still uncommon.
	 */
	SHA256_WITH_DSA("SHA256withDSA"),

	/**
	 * DSA signature using SHA-384 hashing. Rare, limited provider support.
	 */
	SHA384_WITH_DSA("SHA384withDSA"),

	/**
	 * DSA signature using SHA-512 hashing. Rare, limited provider support.
	 */
	SHA512_WITH_DSA("SHA512withDSA"),

	/**
	 * Edwards-curve digital signature (Ed25519). Deterministic, fast, and modern.
	 */
	ED25519("Ed25519"),

	/**
	 * Edwards-curve digital signature (Ed448). Higher security margin, less common.
	 */
	ED448("Ed448");

	/**
	 * The JCA name.
	 */
	private final String value;

	/**
	 * Constructs the enum.
	 *
	 * @param value the JCA name
	 */
	JcaSignatureAlgorithm(final String value) {
		this.value = value;
	}

	/**
	 * Returns the canonical JCA algorithm name as accepted by {@link Signature#getInstance(String)}.
	 *
	 * @return the JCA algorithm name (e.g. {@code "SHA256withRSA"})
	 */
	public String value() {
		return value;
	}

	/**
	 * Returns whether this algorithm is natively supported by the default JDK security providers.
	 *
	 * @return {@code true} if available, {@code false} otherwise
	 */
	public boolean isSupportedByDefault() {
		return isSupportedByDefault(value);
	}

	/**
	 * Returns whether this algorithm is natively supported by the default JDK security providers.
	 *
	 * @param algorithm the algorithm to check
	 * @return {@code true} if available, {@code false} otherwise
	 */
	public static boolean isSupportedByDefault(final String algorithm) {
		try {
			Signature.getInstance(algorithm);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @see JcaSignatureAlgorithm#toString()
	 */
	@Override
	public String toString() {
		return value();
	}
}
