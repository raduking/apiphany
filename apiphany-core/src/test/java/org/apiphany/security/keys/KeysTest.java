package org.apiphany.security.keys;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

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
		SecurityException exception = assertThrows(SecurityException.class, () -> {
			Keys.getKeyFactory(INVALID_ALGORITHM);
		});

		assertThat(exception.getMessage(), is("Error initializing " + INVALID_ALGORITHM + " KeyFactory"));
		assertThat(exception.getCause(), is(instanceOf(NoSuchAlgorithmException.class)));
	}

	@Test
	void shouldGenerateValidKeyPair() {
		var keyPair = Keys.generateKeyPair(XDH);

		assertThat(keyPair, is(notNullValue()));
	}

	@Test
	void shouldThrowSecurityExceptionWhenGeneratingKeyPairWithInvalidAlgorithm() {
		SecurityException exception = assertThrows(SecurityException.class, () -> {
			Keys.generateKeyPair(INVALID_ALGORITHM);
		});

		assertThat(exception.getMessage(), is("Error generating key pair"));
		assertThat(exception.getCause(), is(instanceOf(NoSuchAlgorithmException.class)));
	}

	@Test
	void shouldGenerateSecretBetweenKeyPair() {
		KeyPair keyPair = Keys.generateKeyPair(XDH);

		byte[] sharedSecret = Keys.generateSecret(XDH, keyPair);

		assertThat(sharedSecret, is(notNullValue()));
		assertThat(sharedSecret.length, is(greaterThan(0)));
	}

	@Test
	void shouldGenerateSecretBetweenPublicAndPrivateKey() {
		KeyPair keyPair = Keys.generateKeyPair(XDH);

		byte[] sharedSecret = Keys.generateSecret(XDH, keyPair.getPublic(), keyPair.getPrivate());

		assertThat(sharedSecret, is(notNullValue()));
		assertThat(sharedSecret.length, is(greaterThan(0)));
	}

	@Test
	void shouldThrowSecurityExceptionWhenGeneratingSecretWithInvalidAlgorithm() {
		KeyPair keyPair = Keys.generateKeyPair(XDH);

		SecurityException exception = assertThrows(SecurityException.class, () -> {
			Keys.generateSecret(INVALID_ALGORITHM, keyPair);
		});

		assertThat(exception.getMessage(), is("Error generating shared secret"));
		assertThat(exception.getCause(), is(instanceOf(NoSuchAlgorithmException.class)));
	}

	@Test
	void shouldThrowSecurityExceptionWhenGeneratingSecretWithInvalidAlgorithmAndKeys() {
		KeyPair keyPair = Keys.generateKeyPair(XDH);

		SecurityException exception = assertThrows(SecurityException.class, () -> {
			Keys.generateSecret(INVALID_ALGORITHM, keyPair.getPublic(), keyPair.getPrivate());
		});

		assertThat(exception.getMessage(), is("Error generating shared secret"));
		assertThat(exception.getCause(), is(instanceOf(NoSuchAlgorithmException.class)));
	}

	@Test
	void shouldGeneratePublicKeyFromSpec() throws InvalidKeySpecException {
		KeyPair keyPair = Keys.generateKeyPair(XDH);
		KeyFactory keyFactory = Keys.getKeyFactory(XDH);
		X509EncodedKeySpec pubKeySpec = keyFactory.getKeySpec(keyPair.getPublic(), X509EncodedKeySpec.class);

		var publicKey = Keys.generatePublicKey(keyFactory, pubKeySpec);

		assertThat(publicKey, is(notNullValue()));
	}

	@Test
	void shouldThrowSecurityExceptionWhenGeneratingPublicKeyWithInvalidSpec() {
		KeyFactory keyFactory = Keys.getKeyFactory(XDH);

		SecurityException exception = assertThrows(SecurityException.class, () -> {
			Keys.generatePublicKey(keyFactory, new X509EncodedKeySpec(new byte[] { 0x00, 0x01, 0x02 }));
		});

		assertThat(exception.getMessage(), is("Error generating public key"));
		assertThat(exception.getCause(), is(instanceOf(InvalidKeySpecException.class)));
	}
}
