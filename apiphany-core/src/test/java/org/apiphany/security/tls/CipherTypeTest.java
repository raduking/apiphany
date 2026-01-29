package org.apiphany.security.tls;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link CipherType}.
 *
 * @author Radu Sebastian LAZIN
 */
class CipherTypeTest {

	@Test
	void shouldReturnCorrectProperties() {
		CipherType aead = CipherType.AEAD;
		assertTrue(aead.authenticated());
		assertFalse(aead.usesMac());
		assertFalse(aead.usesPadding());

		CipherType block = CipherType.BLOCK;
		assertFalse(block.authenticated());
		assertTrue(block.usesMac());
		assertTrue(block.usesPadding());

		CipherType stream = CipherType.STREAM;
		assertFalse(stream.authenticated());
		assertTrue(stream.usesMac());
		assertFalse(stream.usesPadding());

		CipherType noEncryption = CipherType.NO_ENCRYPTION;
		assertFalse(noEncryption.authenticated());
		assertTrue(noEncryption.usesMac());
		assertFalse(noEncryption.usesPadding());
	}
}
