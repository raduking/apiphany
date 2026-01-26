package org.apiphany.security.ssl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link KeyStoreType}.
 *
 * @author Radu Sebastian LAZIN
 */
class KeyStoreTypeTest {

	@ParameterizedTest
	@MethodSource("provideKeyStoreTypes")
	void shouldConvertFromStringAndBack(final String keyStoreTypeString, final KeyStoreType expected) {
		KeyStoreType keyStoreType = KeyStoreType.fromString(keyStoreTypeString);

		assertEquals(expected, keyStoreType);
		assertEquals(keyStoreTypeString.toUpperCase(), keyStoreType.value());
	}

	private static Object[][] provideKeyStoreTypes() {
		return new Object[][] {
				{ "JKS", KeyStoreType.JKS },
				{ "PKCS12", KeyStoreType.PKCS12 }
		};
	}
}
