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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import org.apiphany.security.MessageDigestAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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

		assertThat(bulkCipher.jcaKeyAlgorithm(), equalTo(info.algorithm().jcaName()));
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

	@ParameterizedTest(name = "{0} should create cipher when IV is null")
	@EnumSource(
		mode = EnumSource.Mode.INCLUDE,
		names = {
				"RC4_128",
				"RC4_56"
		})
	void shouldCreateCipherWhenIVIsNull(final BulkCipher bulkCipher) {
		byte[] key = new byte[Math.max(1, bulkCipher.keyLength())];

		Cipher cipher = bulkCipher.cipher(Cipher.ENCRYPT_MODE, key);

		assertThat(cipher, is(notNullValue()));
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

	@ParameterizedTest(name = "{0} should concatenate fixed IV and explicit nonce for AEAD")
	@MethodSource("aeadCiphers")
	void shouldBuildFullIvForAead(final BulkCipher cipher) {
		byte[] fixedIv = new byte[cipher.fixedIvLength()];
		byte[] explicitNonce = new byte[cipher.explicitNonceLength()];

		Arrays.fill(fixedIv, (byte) 0xAA);
		Arrays.fill(explicitNonce, (byte) 0xBB);

		byte[] fullIv = cipher.fullIV(fixedIv, explicitNonce);

		assertThat(fullIv.length,
				is(cipher.fixedIvLength() + cipher.explicitNonceLength()));

		assertThat(
				Arrays.copyOfRange(fullIv, 0, fixedIv.length),
				is(fixedIv));

		if (explicitNonce.length > 0) {
			assertThat(
					Arrays.copyOfRange(fullIv, fixedIv.length, fullIv.length),
					is(explicitNonce));
		}
	}

	@ParameterizedTest(name = "{0} should generate random IV for BLOCK cipher")
	@MethodSource("blockCiphers")
	void shouldGenerateRandomIvForBlockCipher(final BulkCipher cipher) {
		byte[] iv1 = cipher.fullIV(null, null);
		byte[] iv2 = cipher.fullIV(null, null);

		assertThat(iv1.length, is(cipher.blockSize()));
		assertThat(iv2.length, is(cipher.blockSize()));

		// extremely unlikely to fail unless IVs are constant
		assertThat(iv1, is(not(iv2)));
	}

	@ParameterizedTest(name = "{0} should return key IV for STREAM cipher")
	@MethodSource("streamCiphers")
	void shouldReturnKeyIvForStreamCipher(final BulkCipher cipher) {
		byte[] keyIv = new byte[] { 1, 2, 3, 4 };

		byte[] fullIv = cipher.fullIV(keyIv, null);

		assertThat(fullIv, is(keyIv));
	}

	@Test
	void noEncryptionShouldReturnEmptyIv() {
		byte[] iv = BulkCipher.UNENCRYPTED.fullIV(null, null);

		assertThat(iv.length, is(0));
	}

	@ParameterizedTest
	@MethodSource("aeadCiphers")
	void aeadIvMustNeverBeNull(final BulkCipher cipher) {
		if (cipher.explicitNonceLength() > 0) {
			SecurityException exception = assertThrows(SecurityException.class,
					() -> cipher.fullIV(null, null));

			assertThat(exception.getMessage(), is("keyIV cannot be null or empty for AEAD ciphers with explicit nonce"));
		} else {
			byte[] iv = cipher.fullIV(null, null);

			assertThat(iv.length, is(cipher.fixedIvLength()));
		}
	}

	@ParameterizedTest
	@MethodSource("aeadCiphers")
	void aeadNonceMustNeverBeNull(final BulkCipher cipher) {
		byte[] fixedIv = new byte[cipher.fixedIvLength()];
		if (cipher.explicitNonceLength() > 0) {
			SecurityException exception = assertThrows(SecurityException.class,
					() -> cipher.fullIV(fixedIv, null));

			assertThat(exception.getMessage(), is("explicitNonce cannot be null or empty for AEAD ciphers with explicit nonce"));
		} else {
			byte[] iv = cipher.fullIV(fixedIv, null);

			assertThat(iv.length, is(cipher.fixedIvLength()));
		}
	}

	@ParameterizedTest
	@EnumSource(CipherType.class)
	void shouldHaveConsistentBulkCipherGrouping(final CipherType type) {
		List<BulkCipher> expected = switch (type) {
			case AEAD -> aeadCiphers().toList();
			case BLOCK -> blockCiphers().toList();
			case STREAM -> streamCiphers().toList();
			case NO_ENCRYPTION -> List.of();
		};

		List<BulkCipher> actual = switch (type) {
			case AEAD -> BulkCipher.aeadCiphers();
			case BLOCK -> BulkCipher.blockCiphers();
			case STREAM -> BulkCipher.streamCiphers();
			case NO_ENCRYPTION -> List.of();
		};

		assertThat(actual, is(expected));
	}

	@Test
	void shouldHaveConsistentEncryptingBulkCipherGrouping() {
		List<BulkCipher> expectedEncryptingCiphers = encryptingCiphers().toList();
		List<BulkCipher> actualEncryptingCiphers = BulkCipher.encryptingCiphers();

		assertThat(actualEncryptingCiphers, is(expectedEncryptingCiphers));
	}

	@ParameterizedTest(name = "{0} should build spec for BLOCK ciphers")
	@MethodSource("blockCiphers")
	void shouldBuildSpecForBlockCiphers(final BulkCipher bulkCipher) {
		byte[] iv = new byte[bulkCipher.fixedIvLength() + bulkCipher.explicitNonceLength()];

		if (!bulkCipher.isTransformationSupported()) {
			return;
		}
		AlgorithmParameterSpec spec = bulkCipher.spec(iv);

		assertThat(spec, is(instanceOf(IvParameterSpec.class)));
		assertThat(((IvParameterSpec) spec).getIV(), is(iv));
	}

	@ParameterizedTest
	@MethodSource("derivedMacLengthCiphers")
	void shouldReturnDigestLengthWhenMacKeyIsDerived(final BulkCipher cipher, final MessageDigestAlgorithm digest) {
		assertThat(cipher.macKeyLength(digest), is(digest.digestLength()));
	}

	@ParameterizedTest
	@MethodSource("fixedMacLengthCiphers")
	void shouldReturnFixedMacKeyLengthRegardlessOfDigest(final BulkCipher cipher, final int expectedMacLength) {
		for (MessageDigestAlgorithm digest : MessageDigestAlgorithm.values()) {
			assertThat(cipher.macKeyLength(digest), is(expectedMacLength));
		}
	}

	private static Stream<BulkCipher> aeadCiphers() {
		return encryptingCiphers(CipherType.AEAD);
	}

	private static Stream<BulkCipher> streamCiphers() {
		return encryptingCiphers(CipherType.STREAM);
	}

	private static Stream<BulkCipher> blockCiphers() {
		return encryptingCiphers(CipherType.BLOCK);
	}

	private static Stream<BulkCipher> encryptingCiphers() {
		return EnumSet.allOf(BulkCipher.class).stream()
				.filter(c -> c.type() != CipherType.NO_ENCRYPTION);
	}

	private static Stream<BulkCipher> encryptingCiphers(final CipherType type) {
		return EnumSet.allOf(BulkCipher.class).stream()
				.filter(c -> c.type() != CipherType.NO_ENCRYPTION)
				.filter(c -> c.type() == type);
	}

	private static Stream<BulkCipher> ciphersThatAllowNullSpec() {
		return EnumSet.allOf(BulkCipher.class).stream()
				.filter(c -> c.type() == CipherType.BLOCK || c.type() == CipherType.STREAM)
				.filter(BulkCipher::isTransformationSupported);
	}

	private static Stream<Arguments> derivedMacLengthCiphers() {
		return EnumSet.allOf(BulkCipher.class).stream()
				.filter(c -> c.info().macKeyLength() == -1)
				.flatMap(c -> Stream.of(
						Arguments.of(c, MessageDigestAlgorithm.SHA1),
						Arguments.of(c, MessageDigestAlgorithm.SHA256),
						Arguments.of(c, MessageDigestAlgorithm.SHA384)));
	}

	private static Stream<Arguments> fixedMacLengthCiphers() {
		return Stream.of(
				Arguments.of(BulkCipher.UNENCRYPTED, 0)
		// add more here *only if* you introduce fixed-MAC ciphers later
		);
	}
}
