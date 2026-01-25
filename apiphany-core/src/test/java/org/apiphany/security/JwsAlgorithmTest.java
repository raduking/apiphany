package org.apiphany.security;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link JwsAlgorithm}.
 *
 * @author Radu Sebastian LAZIN
 */
class JwsAlgorithmTest {

	@ParameterizedTest
	@EnumSource(JwsAlgorithm.class)
	void shouldReturnValueOnToString(final JwsAlgorithm jwsAlgorithm) {
		String value = jwsAlgorithm.value();

		assertThat(value, equalTo(jwsAlgorithm.toString()));
	}

	@ParameterizedTest
	@EnumSource(JwsAlgorithm.class)
	void shouldReturnSameAlgorithmOnFromString(final JwsAlgorithm jwsAlgorithm) {
		JwsAlgorithm fromStringAlgorithm = JwsAlgorithm.fromString(jwsAlgorithm.value());

		assertThat(fromStringAlgorithm, equalTo(jwsAlgorithm));
	}
}
