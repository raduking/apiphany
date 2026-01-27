package org.apiphany.security.keys;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link RSAKeys}.
 *
 * @author Radu Sebastian LAZIN
 */
class RSAKeysTest {

	private static final String PATH_RSA_PUBLIC_PEM = "security/oauth2/rsa_public.pem";
	private static final String PATH_RSA_PRIVATE_PEM = "security/oauth2/rsa_private.pem";

	private static final String INVALID_PATH = "security/oauth2/invalid_rsa_public.pem";
	private static final String INVALID_RSA_PUBLIC_PEM = "security/invalid_rsa_public.pem";
	private static final String INVALID_RSA_PRIVATE_PEM = "security/invalid_rsa_private.pem";

	@Test
	void shouldThrowExceptionOnInstantiation() {
		UnsupportedOperationException exception = assertDefaultConstructorThrows(RSAKeys.class);

		assertThat(exception.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldLoadPublicKey() {
		PublicKey publicKey = RSAKeys.loadPEMPublicKey(PATH_RSA_PUBLIC_PEM);

		assertNotNull(publicKey);
	}

	@Test
	void shouldLoadPrivateKey() {
		PrivateKey privateKey = RSAKeys.loadPEMPrivateKey(PATH_RSA_PRIVATE_PEM);

		assertNotNull(privateKey);
	}

	@Test
	void shouldThrowSecurityExceptionWhenLoadingInvalidPathPublicKey() {
		SecurityException exception = assertThrows(SecurityException.class, () -> RSAKeys.loadPEMPublicKey(INVALID_PATH));

		assertThat(exception.getMessage(), equalTo("Cannot read RSA public key from PEM file: " + INVALID_PATH));
	}

	@Test
	void shouldThrowSecurityExceptionWhenLoadingInvalidPathPrivateKey() {
		SecurityException exception = assertThrows(SecurityException.class, () -> RSAKeys.loadPEMPrivateKey(INVALID_PATH));

		assertThat(exception.getMessage(), equalTo("Cannot read RSA private key from PEM file: " + INVALID_PATH));
	}

	@Test
	void shouldThrowSecurityExceptionWhenLoadingInvalidPublicKey() {
		SecurityException exception = assertThrows(SecurityException.class, () -> RSAKeys.loadPEMPublicKey(INVALID_RSA_PUBLIC_PEM));

		assertThat(exception.getMessage(), equalTo("Cannot load RSA public key from PEM file: " + INVALID_RSA_PUBLIC_PEM));
	}

	@Test
	void shouldThrowSecurityExceptionWhenLoadingInvalidPrivateKey() {
		SecurityException exception = assertThrows(SecurityException.class, () -> RSAKeys.loadPEMPrivateKey(INVALID_RSA_PRIVATE_PEM));

		assertThat(exception.getMessage(), equalTo("Cannot load RSA private key from PEM file: " + INVALID_RSA_PRIVATE_PEM));
	}
}
