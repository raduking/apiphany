package org.apiphany.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.morphix.lang.Enums;

/**
 * Represents supported message digest algorithms for cryptographic operations.
 * <p>
 * This enum provides standard names for common hash algorithms used in digital signatures, message authentication
 * codes, and other security protocols.
 *
 * @author Radu Sebastian LAZIN
 */
public enum MessageDigestAlgorithm {

	/**
	 * SHA-1 algorithm (FIPS 180-4). Note: Considered weak for most security purposes.
	 */
	SHA1("SHA-1"),

	/**
	 * SHA-256 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA256("SHA-256"),

	/**
	 * SHA-384 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA384("SHA-384"),

	/**
	 * SHA-512 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA512("SHA-512"),

	/**
	 * SHA-224 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA224("SHA-224"),

	/**
	 * SHA-512/224 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA512_224("SHA-512/224"),

	/**
	 * SHA-512/256 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA512_256("SHA-512/256"),

	/**
	 * GOST R 34.11-94 hash algorithm.
	 */
	GOST3411("GOST3411"),

	/**
	 * GOST R 34.11-2012 Streebog 256-bit hash algorithm.
	 */
	GOST3411_2012_256("GOST3411-2012-256"),

	/**
	 * GOST R 34.11-2012 Streebog 512-bit hash algorithm.
	 */
	GOST3411_2012_512("GOST3411-2012-512"),

	/**
	 * SM3 cryptographic hash algorithm (OSCCA GM/T 0004-2012).
	 */
	SM3("SM3"),

	/**
	 * MD2 algorithm (RFC 1319). Note: Considered insecure and deprecated.
	 */
	@Deprecated
	MD2("MD2"),

	/**
	 * MD5 algorithm (RFC 1321). Note: Considered cryptographically broken.
	 */
	MD5("MD5"),

	/**
	 * Indicates no message digest algorithm is used.
	 */
	NONE("NONE");

	/**
	 * The name map for easy {@link #fromValue(String)} implementation.
	 */
	private static final Map<String, MessageDigestAlgorithm> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The standard algorithm name as recognized by Java security providers.
	 */
	private final String value;

	/**
	 * Constructs a new MessageDigestAlgorithm enum constant.
	 *
	 * @param algorithm the standard algorithm name
	 */
	MessageDigestAlgorithm(final String algorithm) {
		this.value = algorithm;
	}

	/**
	 * Applies the algorithm on the given input.
	 *
	 * @param input the input to digest
	 * @return the digested input
	 */
	public byte[] digest(final byte[] input) {
		if (this == NONE) {
			throw new UnsupportedOperationException("Digest algorithm '" + this + "' does not support digesting.");
		}
		try {
			MessageDigest digest = MessageDigest.getInstance(getValue());
			return digest.digest(input);
		} catch (NoSuchAlgorithmException e) {
			throw new SecurityException("Error digesting input", e);
		}
	}

	/**
	 * Applies the algorithm on the given input.
	 *
	 * @param input the input to digest
	 * @return the digested input
	 */
	public byte[] digest(final String input) {
		return digest(input.getBytes(StandardCharsets.US_ASCII));
	}

	/**
	 * @see #toString()
	 */
	@Override
	public String toString() {
		return getValue();
	}

	/**
	 * Returns the HMAC (Hash-based Message Authentication Code) string value associated with this message digest algorithm.
	 *
	 * @return the HMAC string value associated with this message digest algorithm
	 */
	public String hmacName() {
		if (this == NONE || this == MD2 || this == MD5) {
			throw new UnsupportedOperationException("Invalid digest algorithm for HMAC PRF: " + this);
		}
		return "Hmac" + getValue().replace("-", "");
	}

	/**
	 * Returns the standard algorithm name.
	 *
	 * @return the algorithm name as recognized by security providers
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Parses a string value (case-sensitive) into a {@link MessageDigestAlgorithm}.
	 *
	 * @param value the string representation of the algorithm (e.g., "SHA-256", "SHA-384").
	 * @return the corresponding enum constant, or {@code null} if no match is found.
	 * @throws IllegalArgumentException If {@code value} is {@code null} (if enforced by {@link Enums#fromString}).
	 */
	public static MessageDigestAlgorithm fromValue(final String value) {
		return Enums.fromString(value, NAME_MAP, values());
	}
}
