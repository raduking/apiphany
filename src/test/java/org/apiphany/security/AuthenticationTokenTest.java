package org.apiphany.security;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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

		String expected = Strings.fromFile("/security/authentication-token.json").trim();

		String result = token.toString().trim();

		assertThat(result, equalTo(expected));
	}

}
