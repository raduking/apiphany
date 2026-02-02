package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.morphix.reflection.Fields;

/**
 * Test class for {@link ExchangeKeys}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExchangeKeysTest {

	@Test
	void shouldCreateExchangeKeysInstance() {
		ExchangeKeys exchangeKeys = new ExchangeKeys();

		assertNotNull(exchangeKeys);
	}

	@Test
	void shouldThrowExceptionOnLoadExchangeKeysFromByteArrayAndCipherSuiteWhenNotEnoughData() {
		byte[] exchangeKeysBytes = new byte[] {
				0x00, 0x01, 0x02, 0x03
		};

		CipherSuite cipherSuite = CipherSuite.TLS_AES_128_GCM_SHA256;

		IllegalArgumentException exception =
				assertThrows(IllegalArgumentException.class, () -> ExchangeKeys.from(exchangeKeysBytes, cipherSuite));

		assertThat(exception.getMessage(), equalTo("Insufficient data in key block to read " + cipherSuite.bulkCipher().blockSize() + " bytes"));
	}

	@Test
	void shouldLoadExchangeKeysFromByteArrayWhenEnoughDataWithAEADCipherSuite() {
		// AEAD
		byte[] exchangeKeysBytes = new byte[] {
				// client write key (16 bytes)
				0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
				// server write key (16 bytes)
				0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
				// client write IV (4 bytes)
				0x20, 0x21, 0x22, 0x23,
				// server write IV (4 bytes)
				0x30, 0x31, 0x32, 0x33,
		};

		CipherSuite cipherSuite = CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256;

		ExchangeKeys exchangeKeys = ExchangeKeys.from(exchangeKeysBytes, cipherSuite);

		assertThat(exchangeKeys.getClientWriteKey(), equalTo(new byte[] {
				0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F }));
		assertThat(exchangeKeys.getServerWriteKey(), equalTo(new byte[] {
				0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F }));

		assertThat(exchangeKeys.getClientIV(), equalTo(new byte[] {
				0x20, 0x21, 0x22, 0x23 }));
		assertThat(exchangeKeys.getServerIV(), equalTo(new byte[] {
				0x30, 0x31, 0x32, 0x33 }));
	}

	@Test
	void shouldLoadExchangeKeysFromByteArrayWhenEnoughDataWithBLOCKCipherSuite() {
		// BLOCK
		byte[] exchangeKeysBytes = new byte[] {
				// client write MAC key (20 bytes)
				0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13,
				// server write MAC key (20 bytes)
				0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x23,
				// client write key (16 bytes)
				0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
				// server write key (16 bytes)
				0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
				// client write IV (16 bytes)
				0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
				// server write IV (16 bytes)
				0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F
		};

		CipherSuite cipherSuite = CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA;

		ExchangeKeys exchangeKeys = ExchangeKeys.from(exchangeKeysBytes, cipherSuite);

		assertThat(exchangeKeys.getClientMacKey(), equalTo(new byte[] {
				0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13 }));
		assertThat(exchangeKeys.getServerMacKey(), equalTo(new byte[] {
				0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x23 }));

		assertThat(exchangeKeys.getClientWriteKey(), equalTo(new byte[] {
				0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F }));
		assertThat(exchangeKeys.getServerWriteKey(), equalTo(new byte[] {
				0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F }));

		assertThat(exchangeKeys.getClientIV(), equalTo(new byte[] {
				0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F }));
		assertThat(exchangeKeys.getServerIV(), equalTo(new byte[] {
				0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F }));
	}

	@ParameterizedTest
	@EnumSource(CipherSuite.class)
	void shouldCreateExchangeKeysForAllCipherSuites(final CipherSuite cipherSuite) {
		byte[] exchangeKeysBytes = buildExchangeKeysBytesForCipherSuite(cipherSuite);

		ExchangeKeys exchangeKeys = ExchangeKeys.from(exchangeKeysBytes, cipherSuite);

		assertNotNull(exchangeKeys);
	}

	@Test
	void shouldSerializeToString() {
		// AEAD
		byte[] exchangeKeysBytes = new byte[] {
				// client write key (16 bytes)
				0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
				// server write key (16 bytes)
				0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
				// client write IV (4 bytes)
				0x20, 0x21, 0x22, 0x23,
				// server write IV (4 bytes)
				0x30, 0x31, 0x32, 0x33,
		};

		CipherSuite cipherSuite = CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256;

		ExchangeKeys exchangeKeys = ExchangeKeys.from(exchangeKeysBytes, cipherSuite);

		String result = exchangeKeys.toString();

		assertNotNull(result);
		assertFalse(Strings.isBlank(result));
		// all fields are sensitive
		for (Field field : Fields.getAllDeclared(ExchangeKeys.class)) {
			assertFalse(result.contains(field.getName()));
		}
	}

	private static byte[] buildExchangeKeysBytesForCipherSuite(final CipherSuite suite) {
		BulkCipher bulkCipher = suite.bulkCipher();
		CipherType type = bulkCipher.type();

		int macLen = suite.messageDigest().digestLength();
		int keyLen = bulkCipher.keyLength();

		int length = switch (type) {
			case AEAD -> keyLen * 2 + bulkCipher.fixedIvLength() * 2;
			case BLOCK -> macLen * 2 + keyLen * 2 + bulkCipher.blockSize() * 2;
			case STREAM -> macLen * 2 + keyLen * 2;
			case NO_ENCRYPTION -> 0;
		};

		byte[] exchangeKeysBytes = new byte[length];
		for (int i = 0; i < length; i++) {
			exchangeKeysBytes[i] = (byte) i;
		}
		return exchangeKeysBytes;
	}
}
