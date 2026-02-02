package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Signature}.
 *
 * @author Radu Sebastian LAZIN
 */
class SignatureTest {

	private static final byte[] DATA = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Signature sig1 = new Signature(DATA);
		Signature sig2 = new Signature(DATA);

		// same reference
		assertEquals(sig1, sig1);

		// different instance, same values
		assertEquals(sig1, sig2);
		assertEquals(sig2, sig1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(sig1.hashCode(), sig2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		Signature sig1 = new Signature(DATA);
		Signature sig2 = new Signature(new byte[] { 0x05, 0x06, 0x07 });

		// different objects
		assertNotEquals(sig1, sig2);
		assertNotEquals(sig1, null);
		assertNotEquals(sig2, "not-an-aad");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		Signature sig = new Signature(DATA);

		int expectedHash = Objects.hash(
				sig.getReserved(),
				sig.getLength(),
				sig.getValue());

		assertEquals(expectedHash, sig.hashCode());
	}

	@Test
	void shouldReadFromInputStream() throws Exception {
		byte[] bytes = new byte[] {
				// reserved: 2 bytes
				0x00, 0x00,
				// length: 2 bytes
				0x00, 0x04,
				// data: 4 bytes
				0x01, 0x02, 0x03, 0x04
		};
		Signature sig = Signature.from(new ByteArrayInputStream(bytes));

		assertEquals(bytes.length, sig.sizeOf());
		assertArrayEquals(bytes, sig.toByteArray());
	}

	@Test
	void shouldSerializeToString() {
		Signature sig = new Signature(DATA);

		String result = sig.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
	}
}
