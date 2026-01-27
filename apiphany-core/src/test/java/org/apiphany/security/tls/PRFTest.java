package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.security.GeneralSecurityException;

import org.apiphany.security.MessageDigestAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link PRF}.
 *
 * @author Radu Sebastian LAZIN
 */
class PRFTest {

	private static final String HMAC = MessageDigestAlgorithm.SHA256.hmacAlgorithmName();

	@Test
	void shouldHaveDeterministicLengthRespectingPHash() throws GeneralSecurityException {
		byte[] secret = "secret".getBytes();
		byte[] seed = "seed".getBytes();

		byte[] out1 = PRF.pHash(secret, seed, 64, HMAC);
		byte[] out2 = PRF.pHash(secret, seed, 64, HMAC);

		assertEquals(64, out1.length);
		assertArrayEquals(out1, out2);

		// sanity: should not be all zeroes
		assertFalse(isAllZero(out1));
	}

	private static boolean isAllZero(final byte[] data) {
		for (byte b : data) {
			if (b != 0) {
				return false;
			}
		}
		return true;
	}

	@Test
	void shouldBeEquivalentOnApplyStringLabelAndByteLabel() throws Exception {
		byte[] secret = "secret".getBytes();
		byte[] seed = "seed".getBytes();
		String label = "test label";

		byte[] a = PRF.apply(secret, label, seed, 48, HMAC);
		byte[] b = PRF.apply(secret, label.getBytes(), seed, 48, HMAC);

		assertArrayEquals(a, b);
	}

	@ParameterizedTest
	@EnumSource(PRFLabel.class)
	void shouldBeEquivalentOnApplyPRFLabelAndRawLabel(final PRFLabel label) throws Exception {
		byte[] secret = "secret".getBytes();
		byte[] seed = "seed".getBytes();

		byte[] a = PRF.apply(secret, label, seed, 48, HMAC);
		byte[] b = PRF.apply(secret, label.toByteArray(), seed, 48, HMAC);

		assertArrayEquals(a, b);
	}
}
