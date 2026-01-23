package org.apiphany.security;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

/**
 * Test class for {@link MessageDigestAlgorithm}.
 *
 * @author Radu Sebastian LAZIN
 */
class MessageDigestAlgorithmTest {

	private static final String MESSAGE = "message";

	@ParameterizedTest
	@EnumSource(MessageDigestAlgorithm.class)
	void shouldBuildWithFromStringWithValidValue(final MessageDigestAlgorithm algorithm) {
		String stringValue = algorithm.value();
		MessageDigestAlgorithm result = MessageDigestAlgorithm.fromValue(stringValue);
		assertThat(result, equalTo(algorithm));
	}

	@Test
	void shouldThrowExceptionOnDigestForNoneDigestAlgorithm() {
		MessageDigestAlgorithm mda = MessageDigestAlgorithm.NONE;

		SecurityException e = assertThrows(SecurityException.class, () -> mda.digest("message"));

		assertThat(e.getMessage(), equalTo("Digest algorithm '" + mda + "' does not support digesting."));
	}

	@Test
	void shouldThrowExceptionOnSanitizedDigestForInvalidDigestAlgorithm() {
		MessageDigestAlgorithm mda = MessageDigestAlgorithm.NONE;

		SecurityException e = assertThrows(SecurityException.class, () -> mda.sanitizedDigest(new byte[] { }));

		assertThat(e.getMessage(), equalTo("Digest algorithm '" + mda + "' does not support digesting."));
	}

	@ParameterizedTest
	@EnumSource(names = { "NONE", "MD2", "MD5" })
	void shouldThrowExceptionOnHmacAlgorithmNameDigestForInvalidDigestAlgorithm(final MessageDigestAlgorithm mda) {
		SecurityException e = assertThrows(SecurityException.class, mda::hmacAlgorithmName);

		assertThat(e.getMessage(), equalTo("Invalid digest algorithm for HMAC PRF: " + mda));
	}

	@ParameterizedTest
	@EnumSource(names = { "SHA1", "SHA256", "SHA384", "SHA512", "SHA224", "SHA512_224", "SHA512_256" })
	void shouldReturnHmacAlgorithmNameForValidDigestAlgorithm(final MessageDigestAlgorithm mda) {
		String expected = "Hmac" + mda.value().replaceAll("-", "");

		assertThat(mda.hmacAlgorithmName(), equalTo(expected));
	}

	@ParameterizedTest
	@EnumSource(names = { "SHA1", "SHA256", "SHA384", "SHA512", "SHA224", "SHA512_224", "SHA512_256" })
	void shouldDigestTheInputWithTheGivenAlgorithm(final MessageDigestAlgorithm mda) throws Exception {
		byte[] bytes = MESSAGE.getBytes(StandardCharsets.UTF_8);

		MessageDigest md = MessageDigest.getInstance(mda.value());
		byte[] expected = md.digest(bytes);

		byte[] result = mda.digest(MESSAGE);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldThrowExceptionOnDigestForInvalidDigestAlgorithm() {
		byte[] bytes = MESSAGE.getBytes(StandardCharsets.UTF_8);

		SecurityException e = assertThrows(SecurityException.class, () -> MessageDigestAlgorithm.digest(bytes, "$invalid$"));

		assertThat(e.getMessage(), equalTo("Error digesting input"));
		assertThat(e.getCause(), instanceOf(NoSuchAlgorithmException.class));
	}

	@Test
	void shouldThrowExceptionOnHmacForInvalidInput() {
		MessageDigestAlgorithm mda = MessageDigestAlgorithm.SHA256;

		SecurityException e = assertThrows(SecurityException.class, () -> mda.hmac(null, null));

		assertThat(e.getMessage(), equalTo("Error computing HMAC"));
		assertThat(e.getCause(), notNullValue());
	}

	@ParameterizedTest
	@EnumSource(mode = Mode.EXCLUDE, names = { "SHA1", "SHA256", "SHA384", "SHA512" })
	void shouldThrowExceptionOnSanitizedValueForInvalidAlgorithm(final MessageDigestAlgorithm mda) {
		SecurityException e = assertThrows(SecurityException.class, mda::sanitizedValue);

		assertThat(e.getMessage(), equalTo("Unsupported digest algorithm: " + mda));
	}
}
