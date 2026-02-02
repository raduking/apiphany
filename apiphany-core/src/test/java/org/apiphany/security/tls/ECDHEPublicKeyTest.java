package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ECDHEPublicKey}.
 *
 * @author Radu Sebastian LAZIN
 */
class ECDHEPublicKeyTest {

	private static final byte[] DATA = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldEqualSameValuesAndSameReference() {
		ECDHEPublicKey pk1 = new ECDHEPublicKey(DATA);
		ECDHEPublicKey pk2 = new ECDHEPublicKey(DATA);

		// same reference
		assertEquals(pk1, pk1);

		// different instance, same values
		assertEquals(pk1, pk2);
		assertEquals(pk2, pk1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(pk1.hashCode(), pk2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		ECDHEPublicKey pk1 = new ECDHEPublicKey(DATA);
		ECDHEPublicKey pk2 = new ECDHEPublicKey(new byte[] { 0x05, 0x06, 0x07 });

		// different objects
		assertNotEquals(pk1, pk2);
		assertNotEquals(pk1, null);
		assertNotEquals(pk2, "not-an-pk");
	}

	@Test
	void shouldReadECDHEPublicKeyFromInputStream() throws Exception {
		byte[] bytes = new byte[] {
				// length (1 byte)
				0x04,
				// data (4 bytes)
				0x0B, 0x0C, 0x0D, 0x0E
		};
		ECDHEPublicKey pk = ECDHEPublicKey.from(new ByteArrayInputStream(bytes));

		assertEquals(bytes.length, pk.sizeOf());
		assertArrayEquals(bytes, pk.toByteArray());
		assertEquals(pk.getData().sizeOf(), pk.sizeOf() - pk.getLength().sizeOf());
	}

	@Test
	void shouldSerializeToString() {
		ECDHEPublicKey pk = new ECDHEPublicKey(DATA);

		String result = pk.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
	}
}
