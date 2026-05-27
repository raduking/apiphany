package org.apiphany.security.ssl;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SSLContextAware}.
 *
 * @author Radu Sebastian LAZIN
 */
class SSLContextAwareTest {

	@Test
	void shouldReturnNullSSLContextByDefault() {
		SSLContextAware sslContextAware = new SSLContextAware() {
			// empty implementation
		};

		assertNull(sslContextAware.getSslContext());
	}
}
