package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.stream.Stream;

import org.apiphany.security.MessageDigestAlgorithm;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link CipherSuite}.
 *
 * @author Radu Sebastian LAZIN
 */
class CipherSuiteTest {

	@ParameterizedTest
	@EnumSource(CipherSuite.class)
	void shouldReturnTheTotalKeyBlockLength(final CipherSuite cipherSuite) {
		BulkCipher bulkCipher = cipherSuite.bulkCipher();
		CipherType type = bulkCipher.type();
		MessageDigestAlgorithm messageDigest = cipherSuite.messageDigest();
		int expected = switch (type) {
			case AEAD -> 2 * (bulkCipher.keyLength() + bulkCipher.fixedIvLength());
			case BLOCK -> 2 * (messageDigest.digestLength() + bulkCipher.keyLength() + bulkCipher.blockSize());
			case STREAM -> 2 * (messageDigest.digestLength() + bulkCipher.keyLength());
			case NO_ENCRYPTION -> 0;
		};

		assertThat(cipherSuite.totalKeyBlockLength(), equalTo(expected));
	}

	@ParameterizedTest
	@EnumSource(CipherSuite.class)
	void shouldHaveKeyExchangeAlgorithm(final CipherSuite cipherSuite) {
		KeyExchangeAlgorithm keyExchangeAlgorithm = cipherSuite.keyExchange();

		assertThat(keyExchangeAlgorithm, notNullValue());
	}

	@ParameterizedTest
	@MethodSource("cipherSuitesWithRSAKeyExchange")
	void shouldHaveRSAKeyExchangeAlgorithm(final CipherSuite cipherSuite) {
		KeyExchangeAlgorithm keyExchangeAlgorithm = cipherSuite.keyExchange();

		assertThat(keyExchangeAlgorithm, equalTo(KeyExchangeAlgorithm.RSA));
	}

	@SuppressWarnings("deprecation")
	private static Stream<CipherSuite> cipherSuitesWithRSAKeyExchange() {
		return Stream.of(
				CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
				CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
				CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256,
				CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256,
				CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
				CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
				CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,
				CipherSuite.TLS_RSA_EXPORT1024_WITH_RC4_56_SHA,
				CipherSuite.TLS_RSA_WITH_RC4_128_SHA);
	}
}
