package org.apiphany.security.oauth2;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apiphany.security.AuthenticationTokenProvider;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OAuth2TokenClientSupplier}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2TokenClientSupplierTest {

	@Test
	void shouldSupplyNull() {
		OAuth2TokenClientSupplier supplier = OAuth2TokenClientSupplier.supplyNull();

		AuthenticationTokenProvider tokenProvider = supplier.get(null, null);

		assertNotNull(supplier);
		assertNull(tokenProvider);
	}
}
