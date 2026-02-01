package org.apiphany.security.tls;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Stream;

import org.apiphany.lang.Hex;
import org.apiphany.security.MessageDigestAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link PRF}.
 *
 * @author Radu Sebastian LAZIN
 */
class PRFTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(PRFTest.class);

	private static final String HMAC_SHA256 = MessageDigestAlgorithm.SHA256.hmacAlgorithmName();
	private static final String HMAC_SHA384 = MessageDigestAlgorithm.SHA384.hmacAlgorithmName();

	@Test
	void shouldHaveDeterministicLengthRespectingPHash() throws GeneralSecurityException {
		byte[] secret = "secret".getBytes();
		byte[] seed = "seed".getBytes();

		byte[] out1 = PRF.pHash(secret, seed, 64, HMAC_SHA256);
		byte[] out2 = PRF.pHash(secret, seed, 64, HMAC_SHA256);

		assertEquals(64, out1.length);
		assertArrayEquals(out1, out2);

		// sanity: should not be all zeroes
		assertFalse(isAllZero(out1));
	}

	@ParameterizedTest
	@MethodSource("supportedAlgorithms")
	void shouldReturnAlgorithmNameForSupportedDigestAlgorithms(final MessageDigestAlgorithm algorithm) {
		assertThat(PRF.algorithmName(algorithm), equalTo(algorithm.hmacAlgorithmName()));
	}

	@ParameterizedTest
	@MethodSource("unsupportedAlgorithms")
	void shouldReturnSHA256AlgorithmNameForUnsupportedDigestAlgorithms(final MessageDigestAlgorithm algorithm) {
		assertThat(PRF.algorithmName(algorithm), equalTo(MessageDigestAlgorithm.SHA256.hmacAlgorithmName()));
	}

	@Test
	void shouldBeEquivalentOnApplyStringLabelAndByteLabel() throws Exception {
		byte[] secret = "secret".getBytes();
		byte[] seed = "seed".getBytes();
		String label = "test label";

		byte[] a = PRF.apply(secret, label, seed, 48, HMAC_SHA256);
		byte[] b = PRF.apply(secret, label.getBytes(), seed, 48, HMAC_SHA256);

		assertArrayEquals(a, b);
	}

	@ParameterizedTest
	@EnumSource(PRFLabel.class)
	void shouldBeEquivalentOnApplyPRFLabelAndRawLabel(final PRFLabel label) throws Exception {
		byte[] secret = "secret".getBytes();
		byte[] seed = "seed".getBytes();

		byte[] a = PRF.apply(secret, label, seed, 48, HMAC_SHA256);
		byte[] b = PRF.apply(secret, label.toByteArray(), seed, 48, HMAC_SHA256);

		assertArrayEquals(a, b);
	}

	@ParameterizedTest
	@MethodSource("providePRFArguments")
	void shouldApplyTheCorrectPseudoRandomFunction(final String label, final String seed, final int length, final String expected)
			throws Exception {
		byte[] secret = new byte[48];
		Arrays.fill(secret, (byte) 0x0b);

		byte[] seedBytes = seed.getBytes(StandardCharsets.US_ASCII);
		byte[] output = PRF.apply(secret, label, seedBytes, length, HMAC_SHA384);

		String hexOutput = Hex.stringSpaced(output).toLowerCase().trim();

		String expectedSingleLine = expected.replace("\n", " ").trim();
		LOGGER.info("PRF Output: {}", hexOutput);
		LOGGER.info("PRF Expect: {}", expectedSingleLine);

		assertThat(hexOutput, equalTo(expectedSingleLine));
	}

	private static boolean isAllZero(final byte[] data) {
		for (byte b : data) {
			if (b != 0) {
				return false;
			}
		}
		return true;
	}

	private static Stream<MessageDigestAlgorithm> supportedAlgorithms() {
		return Stream.of(MessageDigestAlgorithm.SHA256, MessageDigestAlgorithm.SHA384);
	}

	private static Stream<MessageDigestAlgorithm> unsupportedAlgorithms() {
		return EnumSet.allOf(MessageDigestAlgorithm.class).stream()
				.filter(alg -> alg != MessageDigestAlgorithm.SHA256)
				.filter(alg -> alg != MessageDigestAlgorithm.SHA384);
	}

	private static Stream<Arguments> providePRFArguments() {
		return Stream.of(
				Arguments.of(
						"test label",
						"test seed",
						32, """
						cc 3a 20 27 3a 70 78 6a 85 65 6d 30 c0 ad 0c 7b
						e2 0b fd 51 d5 d1 5c 43 82 25 d8 fb 6a 94 82 f1
						"""),
				Arguments.of(
						"another label",
						"different seed",
						48, """
						e1 1d fa 82 07 7c 04 77 2f b6 4a 1a 1a e5 40 59
						cc 3b 19 9d f6 4b 50 23 28 50 08 7f ee 74 b8 68
						63 90 53 de bf 4e 6f ce a8 a8 a3 34 7e f2 9d 7b
						"""),
				Arguments.of(
						"label3",
						"seed3",
						64, """
						91 56 02 f6 ba 3f 29 ae 16 6c d7 26 e9 aa e5 16
						ec 6b e9 01 29 e5 a9 11 2f ed f3 f1 bd 09 b2 a2
						67 a1 ee 35 f5 ab da 72 97 4e 6d 41 87 ca d1 84
						c5 c7 73 75 4f 8e 03 61 83 1e 26 c3 09 5c f9 2a
						"""),
				Arguments.of(
						"test label",
						"test seed",
						16,
						"cc 3a 20 27 3a 70 78 6a 85 65 6d 30 c0 ad 0c 7b"));
	}
}
