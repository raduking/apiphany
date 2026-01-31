package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Certificate}.
 *
 * @author Radu Sebastian LAZIN
 */
class CertificateTest {

	private static final byte[] DATA = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Certificate aad1 = new Certificate(DATA);
		Certificate aad2 = new Certificate(DATA);

		// same reference
		assertEquals(aad1, aad1);

		// different instance, same values
		assertEquals(aad1, aad2);
		assertEquals(aad2, aad1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(aad1.hashCode(), aad2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		Certificate aad1 = new Certificate(DATA);
		Certificate aad2 = new Certificate(new byte[] { 0x05, 0x06, 0x07 });

		// different objects
		assertNotEquals(aad1, aad2);
		assertNotEquals(aad1, null);
		assertNotEquals(aad2, "not-an-aad");
	}
}
