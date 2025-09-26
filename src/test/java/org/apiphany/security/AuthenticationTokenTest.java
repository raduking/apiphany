package org.apiphany.security;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link AuthenticationToken}.
 *
 * @author Radu Sebastian LAZIN
 */
class AuthenticationTokenTest {

	private static final int NOT_BEFORE_POLICY = 7;
	private static final String ACCESS_TOKEN = "accesstoken1234";
	private static final String REFRESH_TOKEN = "refreshtoken1234";
	private static final int EXPIRES_IN = 3683;
	private static final String BASIC = "basic";
	private static final int REFRESH_EXPIRES_IN = 11122;
	private static final String READ_WRITE = "read write";

	@Test
	void shouldSerializeTheAuthenticationTokenToString() {
		AuthenticationToken token = new AuthenticationToken();
		token.setAccessToken(ACCESS_TOKEN);
		token.setRefreshToken(REFRESH_TOKEN);
		token.setExpiresIn(EXPIRES_IN);
		token.setTokenType(BASIC);
		token.setRefreshExpiresIn(REFRESH_EXPIRES_IN);
		token.setNotBeforePolicy(NOT_BEFORE_POLICY);
		token.setScope(READ_WRITE);

		String expected = Strings.removeAllWhitespace(Strings.fromFile("/security/authentication-token.json"));

		String result = Strings.removeAllWhitespace(token.toString());

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTrueOnIsExpiredForExpiredToken() {
		AuthenticationToken token = new AuthenticationToken();
		token.setExpiration(Instant.now().minusSeconds(1));

		boolean result = token.isExpired();

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseOnIsExpiredIfTokenHasNoExpirationSet() {
		AuthenticationToken token = new AuthenticationToken();

		boolean result = token.isExpired();

		assertFalse(result);
	}
}
