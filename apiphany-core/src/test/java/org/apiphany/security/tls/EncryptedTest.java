package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.apiphany.io.BytesWrapper;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Encrypted}.
 *
 * @author Radu Sebastian LAZIN
 */
class EncryptedTest {

	private static final byte[] DATA = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldCreateEncryptedWithEncryptedDataPayload() {
		BytesWrapper dataPayload = new BytesWrapper(DATA);
		Encrypted appData = new Encrypted(dataPayload);

		assertArrayEquals(dataPayload.toByteArray(), appData.toByteArray());
		assertArrayEquals(DATA, appData.toByteArray());
		assertEquals(DATA.length, appData.sizeOf());
	}

	@Test
	void shouldCreateEncryptedFromInputStream() throws Exception {
		BytesWrapper dataPayload = new BytesWrapper(DATA);
		Encrypted appData = Encrypted.from(
				new ByteArrayInputStream(DATA),
				DATA.length);

		assertArrayEquals(dataPayload.toByteArray(), appData.toByteArray());
		assertArrayEquals(DATA, appData.toByteArray());
		assertEquals(DATA.length, appData.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		BytesWrapper dataPayload1 = new BytesWrapper(DATA);
		BytesWrapper dataPayload2 = new BytesWrapper(DATA);
		Encrypted appData1 = new Encrypted(dataPayload1);
		Encrypted appData2 = new Encrypted(dataPayload2);

		// same reference
		assertEquals(appData1, appData1);

		// different instance, same values
		assertEquals(appData1, appData2);
		assertEquals(appData2, appData1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(appData1.hashCode(), appData2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		BytesWrapper dataPayload1 = new BytesWrapper(DATA);
		BytesWrapper dataPayload2 = new BytesWrapper(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		Encrypted appData1 = new Encrypted(dataPayload1);
		Encrypted appData2 = new Encrypted(dataPayload2);

		// different values
		assertNotEquals(appData1, appData2);
		assertNotEquals(appData2, appData1);

		// different types
		assertNotEquals(appData1, null);
		assertNotEquals(appData2, "some string");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		BytesWrapper dataPayload = new BytesWrapper(DATA);
		Encrypted encrypted = new Encrypted(dataPayload);

		int expectedHashCode = Objects.hash(encrypted.getData());

		assertEquals(expectedHashCode, encrypted.hashCode());
	}
}
