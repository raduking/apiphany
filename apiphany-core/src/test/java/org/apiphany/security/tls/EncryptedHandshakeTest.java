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
		EncryptedHandshake eh = new EncryptedHandshake(encryptedPayload);

		assertArrayEquals(encryptedPayload.toByteArray(), eh.toByteArray());
		assertArrayEquals(ENCRYPTED_PAYLOAD, eh.toByteArray());
		assertEquals(ENCRYPTED_PAYLOAD.length, eh.sizeOf());
	}

	@Test
	void shouldCreateEncryptedHandshakeFromInputStream() throws Exception {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		EncryptedHandshake eh = EncryptedHandshake.from(
				new ByteArrayInputStream(ENCRYPTED_PAYLOAD),
				ENCRYPTED_PAYLOAD.length);

		assertArrayEquals(encryptedPayload.toByteArray(), eh.toByteArray());
		assertArrayEquals(ENCRYPTED_PAYLOAD, eh.toByteArray());
		assertEquals(ENCRYPTED_PAYLOAD.length, eh.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Encrypted encryptedPayload1 = new Encrypted(ENCRYPTED_PAYLOAD);
		Encrypted encryptedPayload2 = new Encrypted(ENCRYPTED_PAYLOAD);
		EncryptedHandshake eh1 = new EncryptedHandshake(encryptedPayload1);
		EncryptedHandshake eh2 = new EncryptedHandshake(encryptedPayload2);

		// same reference
		assertEquals(eh1, eh1);

		// different instance, same values
		assertEquals(eh1, eh2);
		assertEquals(eh2, eh1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(eh1.hashCode(), eh2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		Encrypted encryptedPayload1 = new Encrypted(ENCRYPTED_PAYLOAD);
		Encrypted encryptedPayload2 = new Encrypted(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		EncryptedHandshake eh1 = new EncryptedHandshake(encryptedPayload1);
		EncryptedHandshake eh2 = new EncryptedHandshake(encryptedPayload2);

		// different values
		assertNotEquals(eh1, eh2);
		assertNotEquals(eh2, eh1);

		// different types
		assertThat(eh1, not(equalTo(null)));
		assertThat(eh1, not(equalTo("not-an-encrypted-handshake")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		EncryptedHandshake eh = new EncryptedHandshake(encryptedPayload);

		int expectedHashCode = Objects.hash(eh.getEncrypted());

		assertEquals(expectedHashCode, eh.hashCode());
	}
}
