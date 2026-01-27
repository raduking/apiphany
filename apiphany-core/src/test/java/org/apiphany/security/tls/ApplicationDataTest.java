package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ApplicationData}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApplicationDataTest {

	private static final byte[] ENCRYPTED_PAYLOAD = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldCreateApplicationDataWithEncryptedPayload() {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		ApplicationData appData = new ApplicationData(encryptedPayload);

		assertArrayEquals(encryptedPayload.toByteArray(), appData.toByteArray());
		assertArrayEquals(ENCRYPTED_PAYLOAD, appData.toByteArray());
		assertEquals(ENCRYPTED_PAYLOAD.length, appData.sizeOf());
	}

	@Test
	void shouldCreateApplicationDataFromInputStream() throws Exception {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		ApplicationData appData = ApplicationData.from(
				new ByteArrayInputStream(ENCRYPTED_PAYLOAD),
				ENCRYPTED_PAYLOAD.length);

		assertArrayEquals(encryptedPayload.toByteArray(), appData.toByteArray());
		assertArrayEquals(ENCRYPTED_PAYLOAD, appData.toByteArray());
		assertEquals(ENCRYPTED_PAYLOAD.length, appData.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Encrypted encryptedPayload1 = new Encrypted(ENCRYPTED_PAYLOAD);
		Encrypted encryptedPayload2 = new Encrypted(ENCRYPTED_PAYLOAD);
		ApplicationData appData1 = new ApplicationData(encryptedPayload1);
		ApplicationData appData2 = new ApplicationData(encryptedPayload2);

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
		Encrypted encryptedPayload1 = new Encrypted(ENCRYPTED_PAYLOAD);
		Encrypted encryptedPayload2 = new Encrypted(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		ApplicationData appData1 = new ApplicationData(encryptedPayload1);
		ApplicationData appData2 = new ApplicationData(encryptedPayload2);

		// different values
		assertNotEquals(appData1, appData2);
		assertNotEquals(appData2, appData1);

		// different types
		assertNotEquals(appData1, null);
		assertNotEquals(appData2, "some string");
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		ApplicationData appData = new ApplicationData(encryptedPayload);

		int expectedHashCode = Objects.hash(appData.getEncrypted());

		assertEquals(expectedHashCode, appData.hashCode());
	}
}
