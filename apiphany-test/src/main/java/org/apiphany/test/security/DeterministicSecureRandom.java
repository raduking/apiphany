package org.apiphany.test.security;

import java.io.Serial;
import java.security.SecureRandom;
import java.security.SecureRandomParameters;

/**
 * A deterministic {@link SecureRandom} implementation for testing purposes. This implementation
 * generates predictable sequences of bytes being filled with incremental byte values (0, 1, 2, ...).
 * <p>
 * WARNING: This implementation is NOT secure and MUST NOT be used in production systems.
 *
 * @author Radu Sebastian LAZIN
 */
public class DeterministicSecureRandom extends SecureRandom {

	/**
	 * Serial version UID.
	 */
	@Serial
	private static final long serialVersionUID = -6506932033686117081L;

	/**
	 * Default constructor.
	 */
	public DeterministicSecureRandom() {
		// empty
	}

	/**
	 * Fills the provided byte array with incremental byte values (0, 1, 2, ...).
	 *
	 * @param bytes the byte array to fill
	 */
	@Override
	public void nextBytes(final byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (i & 0xFF);
		}
	}

	/**
	 * Returns zero.
	 *
	 * @return zero
	 */
	@Override
	public int nextInt() {
		return 0;
	}

	/**
	 * Ignores the parameters and generates bytes using {@link #nextBytes(byte[])}.
	 *
	 * @param bytes the byte array to fill
	 * @param params the secure random parameters (ignored)
	 */
	@Override
	public void nextBytes(final byte[] bytes, final SecureRandomParameters params) {
		nextBytes(bytes);
	}
}
