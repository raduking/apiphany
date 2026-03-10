package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.EnumSet;
import java.util.Objects;
import java.util.stream.Stream;

import org.apiphany.io.BytesWrapper;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
		Encrypted encrypted = new Encrypted(dataPayload);

		assertArrayEquals(dataPayload.toByteArray(), encrypted.toByteArray());
		assertArrayEquals(DATA, encrypted.toByteArray());
		assertEquals(DATA.length, encrypted.sizeOf());
	}

	@Test
	void shouldCreateEncryptedFromInputStream() throws Exception {
		BytesWrapper dataPayload = new BytesWrapper(DATA);
		Encrypted encrypted = Encrypted.from(
				new ByteArrayInputStream(DATA),
				DATA.length);

		assertArrayEquals(dataPayload.toByteArray(), encrypted.toByteArray());
		assertArrayEquals(DATA, encrypted.toByteArray());
		assertEquals(DATA.length, encrypted.sizeOf());
	}

	@Test
	void shouldEqualSameValuesAndSameReference() {
		BytesWrapper dataPayload1 = new BytesWrapper(DATA);
		BytesWrapper dataPayload2 = new BytesWrapper(DATA);
		Encrypted encrypted1 = new Encrypted(dataPayload1);
		Encrypted encrypted2 = new Encrypted(dataPayload2);

		// same reference
		assertEquals(encrypted1, encrypted1);

		// different instance, same values
		assertEquals(encrypted1, encrypted2);
		assertEquals(encrypted2, encrypted1);

		// hashCode contract (important for coverage + correctness)
		assertEquals(encrypted1.hashCode(), encrypted2.hashCode());
	}

	@Test
	void shouldNotEqualIfDifferentObjects() {
		BytesWrapper dataPayload1 = new BytesWrapper(DATA);
		BytesWrapper dataPayload2 = new BytesWrapper(new byte[] { 0x05, 0x06, 0x07, 0x08 });
		Encrypted encrypted1 = new Encrypted(dataPayload1);
		Encrypted encrypted2 = new Encrypted(dataPayload2);

		// different values
		assertNotEquals(encrypted1, encrypted2);
		assertNotEquals(encrypted2, encrypted1);

		// different types
		assertThat(encrypted1, not(equalTo(null)));
		assertThat(encrypted1, not(equalTo("not-an-encrypted-object")));
	}

	@Test
	void shouldBuildHashcodeWithAllFields() {
		BytesWrapper dataPayload = new BytesWrapper(DATA);
		Encrypted encrypted = new Encrypted(dataPayload);

		int expectedHashCode = Objects.hash(encrypted.getData());

		assertEquals(expectedHashCode, encrypted.hashCode());
	}

	@ParameterizedTest
	@MethodSource("org.apiphany.security.tls.BulkCipher#aeadCiphers")
	void shouldExposeExplicitNonceAndCiphertextSeparately(final BulkCipher cipher) {
		int nonceLen = cipher.explicitNonceLength();
		if (nonceLen <= 0) {
			// skip AEAD ciphers without explicit nonce
			return;
		}

		byte[] nonce = new byte[nonceLen];
		byte[] ciphertext = new byte[32];

		for (int i = 0; i < nonceLen; i++) {
			nonce[i] = (byte) i;
		}
		for (int i = 0; i < ciphertext.length; i++) {
			ciphertext[i] = (byte) (0xA0 + i);
		}

		byte[] rec = new byte[nonceLen + ciphertext.length];
		System.arraycopy(nonce, 0, rec, 0, nonceLen);
		System.arraycopy(ciphertext, 0, rec, nonceLen, ciphertext.length);

		Encrypted encrypted = new Encrypted(rec);

		assertArrayEquals(nonce, encrypted.getNonce(cipher).toByteArray(),
				"Explicit nonce must be the first bytes");
		assertArrayEquals(ciphertext, encrypted.getEncryptedData(cipher).toByteArray(),
				"Encrypted data must exclude explicit nonce");
	}

	@ParameterizedTest
	@MethodSource("nonAeadCiphers")
	void nonAeadEncryptedMustNotExposeNonce(final BulkCipher cipher) {
		byte[] data = new byte[48];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) i;
		}

		Encrypted encrypted = new Encrypted(data);

		assertEquals(0, encrypted.getNonce(cipher).sizeOf(),
				"Non-AEAD ciphers must not expose a nonce");
		assertArrayEquals(data, encrypted.getEncryptedData(cipher).toByteArray(),
				"Encrypted data must be returned as-is");
	}

	@Test
	void shouldSerializeToString() {
		BytesWrapper dataPayload = new BytesWrapper(DATA);
		Encrypted encrypted = new Encrypted(dataPayload);

		String result = encrypted.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
	}

	private static Stream<BulkCipher> nonAeadCiphers() {
		return EnumSet.allOf(BulkCipher.class).stream()
				.filter(c -> c.type() != CipherType.AEAD);
	}
}
