package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link AuthenticationMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
class AuthenticationMethodTest {

	@ParameterizedTest
	@EnumSource(AuthenticationMethod.class)
	void shouldBuildWithFromStringWithValidValue(final AuthenticationMethod authenticationMethod) {
		String stringValue = authenticationMethod.value();
		AuthenticationMethod result = AuthenticationMethod.fromString(stringValue);
		assertThat(result, equalTo(authenticationMethod));
	}

}
