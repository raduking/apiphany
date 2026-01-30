package org.apiphany.security.tls.opt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.apiphany.security.tls.Encrypted;
import org.apiphany.security.tls.EncryptedHandshake;
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

	@Test
	void shouldEqualSameValuesAndSameReference() {
		Encrypted encryptedPayload1 = new Encrypted(ENCRYPTED_PAYLOAD);
		Encrypted encryptedPayload2 = new Encrypted(ENCRYPTED_PAYLOAD);
		ServerFinishedEncrypted sf1 = new ServerFinishedEncrypted(encryptedPayload1);
		ServerFinishedEncrypted sf2 = new ServerFinishedEncrypted(encryptedPayload2);

		// same reference
		assertEquals(sf1, sf1);

		// different instance, same values
		assertEquals(sf1, sf2);
		assertEquals(sf2, sf1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(sf1.hashCode(), sf2.hashCode());
	}

	@Test
	void shouldEqualSameValuesAndSameReferenceWithEncryptedHandshake() {
		Encrypted encryptedPayload1 = new Encrypted(ENCRYPTED_PAYLOAD);
		Encrypted encryptedPayload2 = new Encrypted(ENCRYPTED_PAYLOAD);
		ServerFinishedEncrypted sf = new ServerFinishedEncrypted(encryptedPayload1);
		EncryptedHandshake eh = new EncryptedHandshake(encryptedPayload2);

		// different instance, same values
		assertEquals(sf, eh);
		assertEquals(eh, sf);

		// hashCode contract (important for coverage + correctness)
		assertEquals(sf.hashCode(), eh.hashCode());
	}
}
