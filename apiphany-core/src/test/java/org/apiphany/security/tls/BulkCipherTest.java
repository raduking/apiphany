package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.EnumSet;
import java.util.stream.Stream;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link BulkCipher}.
 *
 * @author Radu Sebastian LAZIN
 */
class BulkCipherTest {

	@ParameterizedTest
	@EnumSource(BulkCipher.class)
	void shouldReturnCorrectValuesFromBulkCipherInfo(final BulkCipher bulkCipher) {
		BulkCipherInfo info = bulkCipher.info();

		assertThat(bulkCipher.algorithm(), equalTo(info.algorithm().jcaName()));
		assertThat(bulkCipher.keyLength(), equalTo(info.keyLength()));
		assertThat(bulkCipher.blockSize(), equalTo(info.blockSize()));
		assertThat(bulkCipher.fixedIvLength(), equalTo(info.fixedIvLength()));
		assertThat(bulkCipher.explicitNonceLength(), equalTo(info.explicitNonceLength()));
		assertThat(bulkCipher.type(), equalTo(info.type()));
		assertThat(bulkCipher.tagLength(), equalTo(info.tagLength()));
	}

	@Test
	void shouldCreateCipherWhenSpecIsProvidedForAeadCipher() {
		BulkCipher bulkCipher = BulkCipher.AES_128_GCM;
		byte[] key = new byte[bulkCipher.keyLength()];
		byte[] iv = new byte[bulkCipher.fixedIvLength() + bulkCipher.explicitNonceLength()];
		AlgorithmParameterSpec spec = new GCMParameterSpec(bulkCipher.tagLength() * 8, iv);

		Cipher cipher = bulkCipher.cipher(Cipher.ENCRYPT_MODE, key, spec);

		assertThat(cipher, is(notNullValue()));
		assertThat(cipher.getAlgorithm(), containsString("GCM"));
	}

	@Test
	void shouldCreateCipherWhenSpecIsNullForBlockCipher() {
		BulkCipher bulkCipher = BulkCipher.AES_128_CBC;
		byte[] key = new byte[bulkCipher.keyLength()];

		Cipher cipher = bulkCipher.cipher(Cipher.ENCRYPT_MODE, key, (AlgorithmParameterSpec) null);

		assertThat(cipher, is(notNullValue()));
		assertThat(cipher.getAlgorithm(), containsString("AES"));
	}

	@Test
	void shouldThrowSecurityExceptionWhenCipherInitializationFails() {
		BulkCipher bulkCipher = BulkCipher.AES_128_GCM;
		byte[] invalidKey = new byte[1];
		byte[] iv = new byte[bulkCipher.fixedIvLength() + bulkCipher.explicitNonceLength()];
		AlgorithmParameterSpec spec = new GCMParameterSpec(bulkCipher.tagLength() * 8, iv);

		SecurityException exception = assertThrows(SecurityException.class,
				() -> bulkCipher.cipher(Cipher.ENCRYPT_MODE, invalidKey, spec));

		assertThat(exception.getMessage(), startsWith("Error building cipher from: "));
		assertThat(exception.getCause(), is(instanceOf(GeneralSecurityException.class)));
	}

	private static Stream<BulkCipher> encryptingCiphers() {
		return EnumSet.allOf(BulkCipher.class).stream()
				.filter(c -> c.type() != CipherType.NO_ENCRYPTION);
	}

	private static Stream<BulkCipher> aeadCiphers() {
		return encryptingCiphers()
				.filter(c -> c.type() == CipherType.AEAD);
	}

	private static Stream<BulkCipher> ciphersThatAllowNullSpec() {
		return EnumSet.allOf(BulkCipher.class).stream()
				.filter(c -> c.type() == CipherType.BLOCK || c.type() == CipherType.STREAM)
				.filter(BulkCipher::isTransformationSupported);
	}

	@ParameterizedTest(name = "{0} should create cipher when spec is null")
	@MethodSource("ciphersThatAllowNullSpec")
	void shouldCreateCipherWhenSpecIsNull(final BulkCipher bulkCipher) {
		byte[] key = new byte[Math.max(1, bulkCipher.keyLength())];

		Cipher cipher = bulkCipher.cipher(Cipher.ENCRYPT_MODE, key, (AlgorithmParameterSpec) null);

		assertThat(cipher, is(notNullValue()));
	}

	@ParameterizedTest(name = "{0} should create cipher with valid AEAD spec")
	@MethodSource("aeadCiphers")
	void shouldCreateCipherWhenAeadSpecIsProvided(final BulkCipher bulkCipher) {
		byte[] key = new byte[bulkCipher.keyLength()];
		byte[] iv = new byte[bulkCipher.fixedIvLength() + bulkCipher.explicitNonceLength()];
		try {
			AlgorithmParameterSpec spec = bulkCipher.spec(iv);

			Cipher cipher = bulkCipher.cipher(Cipher.ENCRYPT_MODE, key, spec);

			assertThat(cipher, is(notNullValue()));
		} catch (SecurityException e) {
			assertInstanceOf(SecurityException.class, e);
			assertThat(e.getMessage(), equalTo("SunJCE does not support: " + bulkCipher.info().transformation()));
		}
	}

	@ParameterizedTest(name = "{0} should throw SecurityException for invalid key")
	@MethodSource("encryptingCiphers")
	void shouldThrowSecurityExceptionWhenKeyIsInvalid(final BulkCipher bulkCipher) {
		byte[] invalidKey = new byte[1];

		AlgorithmParameterSpec spec = switch (bulkCipher.type()) {
			case AEAD -> {
				byte[] iv = new byte[bulkCipher.fixedIvLength() + bulkCipher.explicitNonceLength()];
				yield new GCMParameterSpec(bulkCipher.tagLength() * 8, iv);
			}
			case BLOCK -> {
				byte[] iv = new byte[bulkCipher.fixedIvLength() + bulkCipher.explicitNonceLength()];
				yield new IvParameterSpec(iv);
			}
			case STREAM -> null;
			case NO_ENCRYPTION -> throw new IllegalArgumentException("No encryption ciphers should not be tested here");
		};

		SecurityException exception = assertThrows(SecurityException.class,
				() -> bulkCipher.cipher(Cipher.ENCRYPT_MODE, invalidKey, spec));

		assertThat(exception.getMessage(), startsWith("Error building cipher from: "));
		assertThat(exception.getCause(), is(instanceOf(GeneralSecurityException.class)));
	}

	@ParameterizedTest
	@EnumSource(BulkCipher.class)
	void shouldMatchUnsupportedTransformations(final BulkCipher bulkCipher) {
		boolean supportsTransformation = true;
		try {
			Cipher.getInstance(bulkCipher.info().transformation());
		} catch (GeneralSecurityException e) {
			supportsTransformation = false;
		}
		assertThat(bulkCipher.isTransformationSupported(), is(supportsTransformation));
		if (supportsTransformation) {
			assertThat(BulkCipher.UNSUPPORTED_SUN_JCE_BULK_CIPHERS, not(contains(bulkCipher)));
		}
	}
}
