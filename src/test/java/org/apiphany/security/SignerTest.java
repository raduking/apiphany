package org.apiphany.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.apiphany.utils.Tests;
import org.apiphany.utils.security.ssl.Keys;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Signer}.
 *
 * @author Radu Sebastian LAZIN
 */
class SignerTest {

	private static final String TEXT = "some text to sign";

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(Signer.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldVerifySignature() throws Exception {
		byte[] data = TEXT.getBytes();
		PrivateKey privateKey = Keys.loadRSAPrivateKey("/security/oauth2/rsa_private.pem");
		PublicKey publicKey = Keys.loadRSAPublicKey("/security/oauth2/rsa_public.pem");

		byte[] signature = Signer.sign(privateKey, JwsAlgorithm.PS256, data);

		boolean valid = Signer.verify(publicKey, JwsAlgorithm.PS256, data, signature);

		assertTrue(valid);
	}

	@Test
	void shouldThrowSecurityExceptionIfSigningFails() {
		byte[] data = TEXT.getBytes();
		PrivateKey privateKey = mock(PrivateKey.class);

		SecurityException e = assertThrows(SecurityException.class, () -> Signer.sign(privateKey, JwsAlgorithm.PS256, data));

		assertThat(e.getMessage(), equalTo("Signing failed with " + JwsAlgorithm.PS256));
	}

	@Test
	void shouldThrowSecurityExceptionIfVerifyingFails() throws Exception {
		byte[] data = TEXT.getBytes();
		PrivateKey privateKey = Keys.loadRSAPrivateKey("/security/oauth2/rsa_private.pem");
		PublicKey publicKey = mock(PublicKey.class);

		byte[] signature = Signer.sign(privateKey, JwsAlgorithm.PS256, data);

		SecurityException e = assertThrows(SecurityException.class, () -> Signer.verify(publicKey, JwsAlgorithm.PS256, data, signature));

		assertThat(e.getMessage(), equalTo("Signature verification failed with " + JwsAlgorithm.PS256));
	}

}
