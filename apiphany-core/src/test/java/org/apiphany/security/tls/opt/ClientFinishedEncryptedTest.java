package org.apiphany.security.tls.opt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.apiphany.security.tls.Encrypted;
import org.apiphany.security.tls.EncryptedHandshake;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ClientFinishedEncrypted}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClientFinishedEncryptedTest {

	private static final byte[] ENCRYPTED_PAYLOAD = new byte[] {
			0x0A, 0x0B, 0x0C, 0x0D
	};

	@Test
	void shouldCreateClientFinishedWithEncryptedPayload() {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		ClientFinishedEncrypted clientFinished = new ClientFinishedEncrypted(encryptedPayload);

		assertArrayEquals(encryptedPayload.toByteArray(), clientFinished.toByteArray());
		assertArrayEquals(ENCRYPTED_PAYLOAD, clientFinished.toByteArray());
		assertEquals(ENCRYPTED_PAYLOAD.length, clientFinished.sizeOf());
	}

	@Test
	void shouldCreateClientFinishedFromInputStream() throws Exception {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		ClientFinishedEncrypted serverFinished = ClientFinishedEncrypted.from(
				new ByteArrayInputStream(ENCRYPTED_PAYLOAD),
				ENCRYPTED_PAYLOAD.length);

		assertArrayEquals(encryptedPayload.toByteArray(), serverFinished.toByteArray());
		assertArrayEquals(ENCRYPTED_PAYLOAD, serverFinished.toByteArray());
		assertEquals(ENCRYPTED_PAYLOAD.length, serverFinished.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Encrypted encryptedPayload1 = new Encrypted(ENCRYPTED_PAYLOAD);
		Encrypted encryptedPayload2 = new Encrypted(ENCRYPTED_PAYLOAD);
		ClientFinishedEncrypted cf1 = new ClientFinishedEncrypted(encryptedPayload1);
		ClientFinishedEncrypted cf2 = new ClientFinishedEncrypted(encryptedPayload2);

		// same reference
		assertEquals(cf1, cf1);

		// different instance, same values
		assertEquals(cf1, cf2);
		assertEquals(cf2, cf1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(cf1.hashCode(), cf2.hashCode());
	}

	@Test
	void shouldEqualSameValuesAndSameReferenceWithEncryptedHandshake() {
		Encrypted encryptedPayload1 = new Encrypted(ENCRYPTED_PAYLOAD);
		Encrypted encryptedPayload2 = new Encrypted(ENCRYPTED_PAYLOAD);
		ClientFinishedEncrypted cf = new ClientFinishedEncrypted(encryptedPayload1);
		EncryptedHandshake eh = new EncryptedHandshake(encryptedPayload2);

		// different instance, same values
		assertEquals(cf, eh);
		assertEquals(eh, cf);

		// hashCode contract (important for coverage + correctness)
		assertEquals(cf.hashCode(), eh.hashCode());
	}
}
