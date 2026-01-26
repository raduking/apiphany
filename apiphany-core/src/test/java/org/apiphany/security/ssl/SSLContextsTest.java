package org.apiphany.security.ssl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link SSLContexts}.
 *
 * @author Radu Sebastian LAZIN
 */
class SSLContextsTest {

	private static final String KEYSTORE_PATH = "security/ssl/keystore.jks";
	private static final String KEYSTORE_TYPE = "JKS";

	private static final String SSL_PROPERTIES_JSON = Strings.fromFile("security/ssl/ssl-properties.json");

	@Test
	void shouldCreateSSLContextSuccessfully() {
		SSLProperties sslProperties = JsonBuilder.fromJson(SSL_PROPERTIES_JSON, SSLProperties.class);

		SSLContext sslContext = SSLContexts.create(sslProperties);

		assertNotNull(sslContext);
	}

	@Test
	void shouldThrowSecurityExceptionIfCreateFailsForAnyReason() {
		SecurityException exception = assertThrows(SecurityException.class, () -> SSLContexts.create(null));

		assertThat(exception.getMessage(), equalTo("Error initializing SSL context"));
		assertThat(exception.getCause().getClass(), equalTo(NullPointerException.class));
	}

	@ParameterizedTest
	@MethodSource("provideEmptyPasswords")
	void shouldLoadKeyStoreWithEmptyPasswordButFailToLoadKey(final char[] password) throws KeyStoreException {
		KeyStore keyStore = SSLContexts.keyStore(KEYSTORE_PATH, KEYSTORE_TYPE, password, false);

		assertThat(keyStore.size(), equalTo(1));

		String alias = keyStore.aliases().nextElement();
		UnrecoverableKeyException exception = assertThrows(UnrecoverableKeyException.class, () -> keyStore.getKey(alias, null));

		assertNotNull(exception);
		assertThat(exception.getClass(), equalTo(UnrecoverableKeyException.class));
		assertThat(exception.getMessage(), equalTo("Get Key failed: Cannot read the array length because \"password\" is null"));
	}

	private static char[][] provideEmptyPasswords() {
		return new char[][] {
				null,
				new char[] { },
		};
	}

	@Test
	void shouldFailToLoadKeyStoreWithWrongPassword() {
		char[] wrongPassword = "wrongpassword".toCharArray();
		SecurityException exception =
				assertThrows(SecurityException.class, () -> SSLContexts.keyStore(KEYSTORE_PATH, KEYSTORE_TYPE, wrongPassword, false));

		assertNotNull(exception);
		assertThat(exception.getMessage(), equalTo("Error loading key store: " + KEYSTORE_PATH));

		IOException ioException = (IOException) exception.getCause();
		assertThat(ioException.getMessage(), equalTo("keystore password was incorrect"));
		assertThat(ioException.getCause().getClass(), equalTo(UnrecoverableKeyException.class));
	}

	@Test
	void shouldLoadKeyStoreSuccessfully() throws KeyStoreException {
		char[] correctPassword = "keystorepassword123".toCharArray();
		KeyStore keyStore = SSLContexts.keyStore(KEYSTORE_PATH, KEYSTORE_TYPE, correctPassword, false);

		assertThat(keyStore.size(), equalTo(1));
	}

	@Test
	void shouldLoadKeyWithNullPasswordIfAllowed() throws KeyStoreException {
		KeyStore keyStore = SSLContexts.keyStore(KEYSTORE_PATH, KEYSTORE_TYPE, null, false);

		assertThat(keyStore.size(), equalTo(1));
	}

	@Test
	void shouldLoadKeyWithEmptyPasswordIfAllowed() throws KeyStoreException {
		KeyStore keyStore = SSLContexts.keyStore(KEYSTORE_PATH, KEYSTORE_TYPE, new char[] { }, false);

		assertThat(keyStore.size(), equalTo(1));
	}

	@Test
	void shouldReturnNullIfKeyStoreTypeIsEmpty() {
		KeyStore keyStore = SSLContexts.keyStore(KEYSTORE_PATH, "", null, false);

		assertNull(keyStore);
	}

	@Test
	void shouldReturnNullIfKeyStoreTypeIsNull() {
		KeyStore keyStore = SSLContexts.keyStore(KEYSTORE_PATH, null, null, false);

		assertNull(keyStore);
	}

	@Test
	void shouldReturnNullIfKeyStoreLocationIsEmpty() {
		KeyStore keyStore = SSLContexts.keyStore("", KEYSTORE_TYPE, null, false);

		assertNull(keyStore);
	}
}
