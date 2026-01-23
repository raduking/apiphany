package org.apiphany.security;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link JcaSignatureAlgorithm}.
 *
 * @author Radu Sebastian LAZIN
 */
class JcaSignatureAlgorithmTest {

	@ParameterizedTest
	@EnumSource(JcaSignatureAlgorithm.class)
	void shouldReturnValueOnToString(final JcaSignatureAlgorithm jcaSignatureAlgorithm) {
		String value = jcaSignatureAlgorithm.value();

		assertThat(value, equalTo(jcaSignatureAlgorithm.toString()));
	}

	@ParameterizedTest
	@EnumSource(JcaSignatureAlgorithm.class)
	void shouldReturnTrueOnIsSupportedByDefault(final JcaSignatureAlgorithm jcaSignatureAlgorithm) {
		boolean supported = jcaSignatureAlgorithm.isSupportedByDefault();

		assertTrue(supported);
	}

	@Test
	void shouldReturnFalseForUnsupportedAlgorithm() {
		boolean supported = JcaSignatureAlgorithm.isSupportedByDefault("unsupported");

		assertFalse(supported);
	}
}
