package org.apiphany.security.tls.opt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.apiphany.security.tls.Encrypted;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ServerFinishedEncrypted}.
 *
 * @author Radu Sebastian LAZIN
 */
class ServerFinishedEncryptedTest {

	private static final byte[] ENCRYPTED_PAYLOAD = new byte[] {
			0x0A, 0x0B, 0x0C, 0x0D
	};

	@Test
	void shouldCreateServerFinishedWithEncryptedPayload() {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		ServerFinishedEncrypted serverFinished = new ServerFinishedEncrypted(encryptedPayload);

		assertArrayEquals(encryptedPayload.toByteArray(), serverFinished.toByteArray());
		assertArrayEquals(ENCRYPTED_PAYLOAD, serverFinished.toByteArray());
		assertEquals(ENCRYPTED_PAYLOAD.length, serverFinished.sizeOf());
	}

	@Test
	void shouldCreateServerFinishedFromInputStream() throws Exception {
		Encrypted encryptedPayload = new Encrypted(ENCRYPTED_PAYLOAD);
		ServerFinishedEncrypted serverFinished = ServerFinishedEncrypted.from(
				new ByteArrayInputStream(ENCRYPTED_PAYLOAD),
				ENCRYPTED_PAYLOAD.length);

		assertArrayEquals(encryptedPayload.toByteArray(), serverFinished.toByteArray());
		assertArrayEquals(ENCRYPTED_PAYLOAD, serverFinished.toByteArray());
		assertEquals(ENCRYPTED_PAYLOAD.length, serverFinished.sizeOf());
	}
}
