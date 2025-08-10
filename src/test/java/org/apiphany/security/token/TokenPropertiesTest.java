package org.apiphany.security.token;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link TokenProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class TokenPropertiesTest {

	private static final String TOKEN = "token";

	@Test
	void shouldHaveSSLAsRootProperty() {
		assertThat(TokenProperties.ROOT, equalTo(TOKEN));
	}

}
