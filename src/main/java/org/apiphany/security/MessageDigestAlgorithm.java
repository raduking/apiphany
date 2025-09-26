package org.apiphany.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
	SHA1("SHA-1", 20),

	/**
	 * SHA-256 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA256("SHA-256", 32),

	/**
	 * SHA-384 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA384("SHA-384", 48),

	/**
	 * SHA-512 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA512("SHA-512", 64),

	/**
	 * SHA-224 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA224("SHA-224", 28),

	/**
	 * SHA-512/224 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA512_224("SHA-512/224", 28),

	/**
	 * SHA-512/256 algorithm (FIPS 180-4). Part of the SHA-2 family.
	 */
	SHA512_256("SHA-512/256", 32),

	/**
	 * GOST R 34.11-94 hash algorithm.
	 */
	GOST3411("GOST3411", 32),

	/**
	 * GOST R 34.11-2012 Streebog 256-bit hash algorithm.
	 */
	GOST3411_2012_256("GOST3411-2012-256", 32),

	/**
	 * GOST R 34.11-2012 Streebog 512-bit hash algorithm.
	 */
	GOST3411_2012_512("GOST3411-2012-512", 64),

	/**
	 * SM3 cryptographic hash algorithm (OSCCA GM/T 0004-2012).
	 */
	SM3("SM3", 32),

	/**
	 * MD2 algorithm (RFC 1319). Note: Considered insecure and deprecated.
	 */
	@Deprecated
	MD2("MD2", 16),

	/**
	 * MD5 algorithm (RFC 1321). Note: Considered cryptographically broken.
	 */
	MD5("MD5", 16),

	/**
	 * Indicates no message digest algorithm is used.
	 */
	NONE("NONE", 0);

	/**
	 * The name map for easy {@link #fromValue(String)} implementation.
	 */
	private static final Map<String, MessageDigestAlgorithm> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The standard algorithm name as recognized by Java security providers.
	 */
	private final String value;

	/**
	 * Digest length in bytes.
	 */
	private final int digestLength;

	/**
	 * Constructs a new MessageDigestAlgorithm enum constant.
	 *
	 * @param algorithm the standard algorithm name
	 */
	MessageDigestAlgorithm(final String algorithm, final int digestLength) {
		this.value = algorithm;
		this.digestLength = digestLength;
	}

	/**
	 * Applies the algorithm on the given input.
	 *
	 * @param input the input to digest
	 * @return the digested input
	 */
	public byte[] digest(final byte[] input) {
		if (this == NONE) {
			throw new SecurityException("Digest algorithm '" + this + "' does not support digesting.");
		}
		return digest(input, value());
	}

	/**
	 * Applies the algorithm on the given input using the {@link #sanitizedValue()} as the algorithm.
	 * <p>
	 * In Java TLS 1.2 for example instead of {@link #SHA1} it defaults to {@link #SHA256}.
	 *
	 * @param input the input to digest
	 * @return the digested input
	 */
	public byte[] sanitizedDigest(final byte[] input) {
		if (this == NONE) {
			throw new SecurityException("Digest algorithm '" + this + "' does not support digesting.");
		}
		return digest(input, sanitizedValue());
	}

	/**
	 * Applies the algorithm on the given input.
	 *
	 * @param input the input to digest
	 * @param algorithm digest algorithm
	 * @return the digested input
	 */
	public static byte[] digest(final byte[] input, final String algorithm) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
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
		return value();
	}

	/**
	 * Returns the HMAC (Hash-based Message Authentication Code) string value associated with this message digest algorithm.
	 *
	 * @return the HMAC string value associated with this message digest algorithm
	 */
	public String hmacAlgorithmName() {
		if (NONE == this || MD2 == this || MD5 == this) { // NOSONAR
			throw new SecurityException("Invalid digest algorithm for HMAC PRF: " + this);
		}
		return "Hmac" + value().replace("-", "");
	}

	/**
	 * Returns the PRF algorithm name (for TLS 1.2 it can only be HMAC-SHA256 or HMAC-SHA384, if it is not one of those it
	 * defaults to HMAC-SHA256).
	 *
	 * @return the PRF algorithm name
	 */
	public String prfHmacAlgorithmName() {
		if (SHA256 == this || SHA384 == this) {
			return hmacAlgorithmName();
		}
		return SHA256.hmacAlgorithmName();
	}

	/**
	 * Returns the sanitized digest algorithm name.
	 *
	 * @return the sanitized digest algorithm name
	 */
	public String sanitizedValue() {
		return switch (this) {
			case SHA1 -> SHA256.value();
			case SHA256, SHA384, SHA512 -> value();
			default -> throw new SecurityException("Unsupported digest algorithm: " + this);
		};
	}

	/**
	 * Computes the HMAC given a key and data.
	 *
	 * @param key the key
	 * @param data the data
	 * @return the HMAC
	 */
	public byte[] hmac(final byte[] key, final byte[] data) {
		String algorithm = hmacAlgorithmName();
		try {
			Mac mac = Mac.getInstance(algorithm);
			mac.init(new SecretKeySpec(key, algorithm));
			return mac.doFinal(data);
		} catch (Exception e) {
			throw new SecurityException("Error computing HMAC", e);
		}
	}

	/**
	 * Returns the standard algorithm name.
	 *
	 * @return the algorithm name as recognized by security providers
	 */
	public String value() {
		return value;
	}

	/**
	 * Returns the digest length in bytes.
	 *
	 * @return digest length in bytes
	 */
	public int digestLength() {
		return digestLength;
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
