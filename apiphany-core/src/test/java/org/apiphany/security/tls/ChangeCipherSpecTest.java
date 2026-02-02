package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.UInt8;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ChangeCipherSpec}.
 *
 * @author Radu Sebastian LAZIN
 */
class ChangeCipherSpecTest {

	private static final byte[] CHANGE_CIPHER_SPEC_BYTES = new byte[] { 0x01 };

	@Test
	void shouldCreateChangeCipherSpecWithTLSObjectsConstructor() {
		ChangeCipherSpec ccs = new ChangeCipherSpec(UInt8.of((byte) 0x01));

		assertArrayEquals(CHANGE_CIPHER_SPEC_BYTES, ccs.toByteArray());
		assertEquals(CHANGE_CIPHER_SPEC_BYTES.length, ccs.sizeOf());
	}

	@Test
	void shouldCreateChangeCipherSpecWithPrimitiveValuesConstructor() {
		ChangeCipherSpec ccs = new ChangeCipherSpec((byte) 0x01);

		assertArrayEquals(CHANGE_CIPHER_SPEC_BYTES, ccs.toByteArray());
		assertEquals(CHANGE_CIPHER_SPEC_BYTES.length, ccs.sizeOf());
	}

	@Test
	void shouldCreateChangeCipherSpecFromInputStream() throws Exception {
		ChangeCipherSpec ccs = ChangeCipherSpec.from(new ByteArrayInputStream(CHANGE_CIPHER_SPEC_BYTES));

		assertArrayEquals(CHANGE_CIPHER_SPEC_BYTES, ccs.toByteArray());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		ChangeCipherSpec ccs1 = new ChangeCipherSpec();
		ChangeCipherSpec ccs2 = new ChangeCipherSpec((byte) 0x01);

		// same reference
		assertEquals(ccs1, ccs1);

		// different instance, same values
		assertEquals(ccs1, ccs2);
		assertEquals(ccs2, ccs1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(ccs1.hashCode(), ccs2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		ChangeCipherSpec ccs1 = new ChangeCipherSpec();
		ChangeCipherSpec ccs2 = new ChangeCipherSpec((byte) 0x02);

		// different objects
		assertNotEquals(ccs1, ccs2);
		assertNotEquals(ccs1, null);
		assertNotEquals(ccs1, "some string");
	}

	@Test
	void shouldBuildHashCodeWithAllFields() {
		ChangeCipherSpec ccs = new ChangeCipherSpec((byte) 0x01);

		int expectedHashCode = Objects.hash(ccs.getPayload());

		assertEquals(expectedHashCode, ccs.hashCode());
	}

	@Test
	void shouldSerializeToString() {
		ChangeCipherSpec ccs = new ChangeCipherSpec();

		String result = ccs.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
	}
}
