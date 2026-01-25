package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link AuthorizationGrantType}.
 *
 * @author Radu Sebastian LAZIN
 */
class AuthorizationGrantTypeTest {

	@ParameterizedTest
	@EnumSource(AuthorizationGrantType.class)
	void shouldBuildWithFromStringWithValidValue(final AuthorizationGrantType authorizationGrantType) {
		String stringValue = authorizationGrantType.value();
		AuthorizationGrantType result = AuthorizationGrantType.fromString(stringValue);

		assertThat(result, equalTo(authorizationGrantType));
	}

	@ParameterizedTest
	@EnumSource(AuthorizationGrantType.class)
	void shouldMatchValidValue(final AuthorizationGrantType authorizationGrantType) {
		String stringValue = authorizationGrantType.value();
		boolean result = authorizationGrantType.matches(stringValue);

		assertThat(result, equalTo(true));
	}

	@ParameterizedTest
	@EnumSource(AuthorizationGrantType.class)
	void shouldNotMatchInvalidValue(final AuthorizationGrantType authorizationGrantType) {
		String stringValue = "invalid-" + authorizationGrantType.value();
		boolean result = authorizationGrantType.matches(stringValue);

		assertThat(result, equalTo(false));
	}
}
