package org.apiphany.security.keys;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Keys}.
 *
 * @author Radu Sebastian LAZIN
 */
class KeysTest {

	private static final String XDH = "XDH";
	private static final String INVALID_ALGORITHM = "InvalidAlgorithm";

	@Test
	void shouldInstantiateKeyFactory() {
		KeyFactory keyFactory = Keys.getKeyFactory(XDH);

		assertThat(keyFactory, is(notNullValue()));
	}

	@Test
	void shouldThrowSecurityExceptionWhenAlgorithmIsInvalid() {
		try {
			Keys.getKeyFactory(INVALID_ALGORITHM);
		} catch (RuntimeException ex) {
			assertThat(ex.getCause(), is(instanceOf(NoSuchAlgorithmException.class)));
		}
	}

	@Test
	void shouldGenerateValidKeyPair() {
		var keyPair = Keys.generateKeyPair(XDH);

		assertThat(keyPair, is(notNullValue()));
	}

	@Test
	void shouldThrowSecurityExceptionWhenGeneratingKeyPairWithInvalidAlgorithm() {
		try {
			Keys.generateKeyPair(INVALID_ALGORITHM);
		} catch (RuntimeException ex) {
			assertThat(ex.getCause(), is(instanceOf(NoSuchAlgorithmException.class)));
		}
	}
}
