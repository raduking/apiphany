package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link EncryptedHandshake}.
 *
 * @author Radu Sebastian LAZIN
 */
class EncryptedHandshakeTest {

	private static final byte[] ENCRYPTED_PAYLOAD = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldCreateEncryptedHandshakeWithEncryptedPayload() {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		EncryptedHandshake appData = new EncryptedHandshake(encryptedPayload);

		assertArrayEquals(encryptedPayload.toByteArray(), appData.toByteArray());
		assertArrayEquals(ENCRYPTED_PAYLOAD, appData.toByteArray());
		assertEquals(ENCRYPTED_PAYLOAD.length, appData.sizeOf());
	}

	@Test
	void shouldCreateEncryptedHandshakeFromInputStream() throws Exception {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		EncryptedHandshake appData = EncryptedHandshake.from(
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
		EncryptedHandshake appData1 = new EncryptedHandshake(encryptedPayload1);
		EncryptedHandshake appData2 = new EncryptedHandshake(encryptedPayload2);

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
		EncryptedHandshake appData1 = new EncryptedHandshake(encryptedPayload1);
		EncryptedHandshake appData2 = new EncryptedHandshake(encryptedPayload2);

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
		EncryptedHandshake appData = new EncryptedHandshake(encryptedPayload);

		int expectedHashCode = Objects.hash(appData.getEncrypted());

		assertEquals(expectedHashCode, appData.hashCode());
	}
}
