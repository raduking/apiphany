package org.apiphany.security;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link MessageDigestAlgorithm}.
 *
 * @author Radu Sebastian LAZIN
 */
class MessageDigestAlgorithmTest {

	@ParameterizedTest
	@EnumSource(MessageDigestAlgorithm.class)
	void shouldBuildWithFromStringWithValidValue(final MessageDigestAlgorithm algorithm) {
		String stringValue = algorithm.value();
		MessageDigestAlgorithm result = MessageDigestAlgorithm.fromValue(stringValue);
		assertThat(result, equalTo(algorithm));
	}

}
