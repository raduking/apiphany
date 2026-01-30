package org.apiphany.security.tls.opt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.apiphany.security.tls.Encrypted;
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
}
