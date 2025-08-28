package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link ClientAuthenticationMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClientAuthenticationMethodTest {

	@ParameterizedTest
	@EnumSource(ClientAuthenticationMethod.class)
	void shouldReturnEnumFromString(final ClientAuthenticationMethod cam) {
		ClientAuthenticationMethod result = ClientAuthenticationMethod.fromString(cam.value());

		assertThat(result, equalTo(cam));
	}

}
