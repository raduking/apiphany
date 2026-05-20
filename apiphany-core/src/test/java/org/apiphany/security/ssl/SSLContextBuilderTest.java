package org.apiphany.security.ssl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SSLContextBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class SSLContextBuilderTest {

	private static final String KEYSTORE_PATH = "security/ssl/keystore.jks";
	private static final String KEYSTORE_PASSWORD = "keystorepassword123";
	private static final String TRUSTSTORE_PATH = "security/ssl/truststore.jks";
	private static final String TRUSTSTORE_PASSWORD = "truststorepassword123";

	@Test
	void shouldBuildSslContextWithKeystoreAndTruststore() {
		SSLContext sslContext = SSLContextBuilder.create()
				.keystore(KEYSTORE_PATH, KEYSTORE_PASSWORD.toCharArray())
				.truststore(TRUSTSTORE_PATH, TRUSTSTORE_PASSWORD.toCharArray())
				.protocol(SSLProtocol.TLS_1_3)
				.build();

		assertNotNull(sslContext);
	}

	@Test
	void shouldBuildSslContextWithTypeSpecified() {
		SSLContext sslContext = SSLContextBuilder.create()
				.keystore(KEYSTORE_PATH, KEYSTORE_PASSWORD.toCharArray(), KeyStoreType.JKS.value())
				.truststore(TRUSTSTORE_PATH, TRUSTSTORE_PASSWORD.toCharArray(), KeyStoreType.JKS.value())
				.build();

		assertNotNull(sslContext);
	}

	@Test
	void shouldBuildSslContextWithOnlyTruststore() {
		SSLContext sslContext = SSLContextBuilder.create()
				.truststore(TRUSTSTORE_PATH, TRUSTSTORE_PASSWORD.toCharArray())
				.build();

		assertNotNull(sslContext);
	}

	@Test
	void shouldThrowExceptionWhenKeystoreNotFound() {
		SSLContextBuilder builder = SSLContextBuilder.create()
				.keystore("invalid/path/keystore.jks", "wrongpassword".toCharArray());

		SecurityException exception = assertThrows(SecurityException.class, builder::build);

		assertThat(exception.getMessage(), equalTo("Error initializing SSL context"));
		assertThat(exception.getCause().getMessage(), equalTo("Error loading key store: invalid/path/keystore.jks"));
	}

	@Test
	void shouldThrowExceptionWhenKeystorePasswordIsWrong() {
		SSLContextBuilder builder = SSLContextBuilder.create()
				.keystore(KEYSTORE_PATH, "wrongpassword".toCharArray());

		SecurityException exception = assertThrows(SecurityException.class, builder::build);

		assertThat(exception.getMessage(), equalTo("Error initializing SSL context"));
	}
}
