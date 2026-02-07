package org.apiphany.security.tls;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apiphany.lang.Bytes;
import org.apiphany.security.MessageDigestAlgorithm;

/**
 * Generates pseudo-random functions (PRF) for TLS key expansion and derivation. Implements the TLS PRF function using
 * HMAC-based key expansion.
 *
 * @author Radu Sebastian LAZIN
 */
public interface PRF {

	/**
	 * Returns the PRF algorithm name for the given message digest algorithm (for TLS 1.2 it can only be HMAC-SHA256 or
	 * HMAC-SHA384, if it is not one of those it defaults to HMAC-SHA256).
	 * <p>
	 * TODO: Extend this method to support TLS 1.3 PRF algorithms as well.
	 *
	 * @param digestAlgorithm the message digest algorithm
	 * @return the corresponding HMAC algorithm name
	 */
	static String algorithmName(final MessageDigestAlgorithm digestAlgorithm) {
		return switch (digestAlgorithm) {
			case SHA256, SHA384 -> digestAlgorithm.hmacAlgorithmName();
			default -> MessageDigestAlgorithm.SHA256.hmacAlgorithmName();
		};
	}

	/**
	 * Applies the PRF function using a string label.
	 *
	 * @param secret the secret key material
	 * @param label the ASCII label for the PRF
	 * @param seed the seed value
	 * @param length the desired output length
	 * @param algorithm the HMAC algorithm to use (e.g., "HmacSHA256")
	 * @return the generated pseudo-random bytes
	 * @throws GeneralSecurityException if cryptographic operations fail
	 */
	static byte[] apply(final byte[] secret, final String label, final byte[] seed,
			final int length, final String algorithm) throws GeneralSecurityException {
		return apply(secret, label.getBytes(StandardCharsets.US_ASCII), seed, length, algorithm);
	}

	/**
	 * Applies the PRF function using a PRFLabel enum.
	 *
	 * @param secret the secret key material
	 * @param label the PRFLabel for the specific TLS context
	 * @param seed the seed value
	 * @param length the desired output length
	 * @param algorithm the HMAC algorithm to use
	 * @return the generated pseudo-random bytes
	 * @throws GeneralSecurityException if cryptographic operations fail
	 */
	static byte[] apply(final byte[] secret, final PRFLabel label, final byte[] seed,
			final int length, final String algorithm) throws GeneralSecurityException {
		return apply(secret, label.toByteArray(), seed, length, algorithm);
	}

	/**
	 * Applies the PRF function using a byte array label.
	 *
	 * @param secret the secret key material
	 * @param label the label bytes
	 * @param seed the seed value
	 * @param length the desired output length
	 * @param algorithm the HMAC algorithm to use
	 * @return the generated pseudo-random bytes
	 * @throws GeneralSecurityException if cryptographic operations fail
	 */
	static byte[] apply(final byte[] secret, final byte[] label, final byte[] seed,
			final int length, final String algorithm) throws GeneralSecurityException {
		return pHash(secret, Bytes.concatenate(label, seed), length, algorithm);
	}

	/**
	 * Implements the P_hash function from TLS specification, which is the core of the TLS PRF using HMAC for key expansion.
	 *
	 * @param secret the secret key material
	 * @param seed the seed value
	 * @param length the desired output length
	 * @param algorithm the HMAC algorithm to use
	 * @return the expanded key material
	 * @throws GeneralSecurityException if cryptographic operations fail
	 */
	static byte[] pHash(final byte[] secret, final byte[] seed, final int length, final String algorithm) throws GeneralSecurityException {
		Mac mac = Mac.getInstance(algorithm);
		mac.init(new SecretKeySpec(secret, algorithm));

		byte[] a = seed;
		byte[] result = new byte[length];

		int offset = 0;
		while (offset < length) {
			a = mac.doFinal(a); // a(i) = HMAC(secret, a(i-1))
			byte[] output = mac.doFinal(Bytes.concatenate(a, seed));
			int copyLength = Math.min(output.length, length - offset);
			System.arraycopy(output, 0, result, offset, copyLength);
			offset += copyLength;
		}
		return result;
	}
}
