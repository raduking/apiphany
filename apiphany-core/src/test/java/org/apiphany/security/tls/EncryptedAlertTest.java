package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link EncryptedAlert}.
 *
 * @author Radu Sebastian LAZIN
 */
class EncryptedAlertTest {

	private static final byte[] ENCRYPTED_PAYLOAD = new byte[] {
			0x01, 0x02, 0x03, 0x04
	};

	@Test
	void shouldCreateEncryptedAlertWithEncryptedPayload() {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		EncryptedAlert ea = new EncryptedAlert(encryptedPayload);

		assertArrayEquals(encryptedPayload.toByteArray(), ea.toByteArray());
		assertArrayEquals(ENCRYPTED_PAYLOAD, ea.toByteArray());
		assertEquals(ENCRYPTED_PAYLOAD.length, ea.sizeOf());
	}

	@Test
	void shouldCreateEncryptedAlertFromInputStream() throws Exception {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		EncryptedAlert ea = EncryptedAlert.from(
				new ByteArrayInputStream(ENCRYPTED_PAYLOAD),
				ENCRYPTED_PAYLOAD.length);

		assertArrayEquals(encryptedPayload.toByteArray(), ea.toByteArray());
		assertArrayEquals(ENCRYPTED_PAYLOAD, ea.toByteArray());
		assertEquals(ENCRYPTED_PAYLOAD.length, ea.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Encrypted encryptedPayload1 = new Encrypted(ENCRYPTED_PAYLOAD);
		Encrypted encryptedPayload2 = new Encrypted(ENCRYPTED_PAYLOAD);
		EncryptedAlert ea1 = new EncryptedAlert(encryptedPayload1);
		EncryptedAlert ea2 = new EncryptedAlert(encryptedPayload2);

		// same reference
		assertEquals(ea1, ea1);

		// different instance, same values
		assertEquals(ea1, ea2);
		assertEquals(ea2, ea1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(ea1.hashCode(), ea2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		Encrypted encryptedPayload1 = new Encrypted(ENCRYPTED_PAYLOAD);
		Encrypted encryptedPayload2 = new Encrypted(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		EncryptedAlert ea1 = new EncryptedAlert(encryptedPayload1);
		EncryptedAlert ea2 = new EncryptedAlert(encryptedPayload2);

		// different values
		assertNotEquals(ea1, ea2);
		assertNotEquals(ea2, ea1);

		// different types
		assertThat(ea1, not(equalTo(null)));
		assertThat(ea1, not(equalTo("not-an-encrypted-alert")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		EncryptedAlert ea = new EncryptedAlert(encryptedPayload);

		int expectedHashCode = Objects.hash(ea.getEncrypted());

		assertEquals(expectedHashCode, ea.hashCode());
	}
}
