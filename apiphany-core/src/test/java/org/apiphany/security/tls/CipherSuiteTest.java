package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.security.MessageDigestAlgorithm;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

}
