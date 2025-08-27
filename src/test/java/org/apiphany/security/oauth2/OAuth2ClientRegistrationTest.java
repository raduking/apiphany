package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OAuth2ClientRegistration}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2ClientRegistrationTest {

	@Test
	void shouldLoadFromFile() {
		String json = Strings.fromFile("/security/oauth2/oauth2-client-registration.json");

		OAuth2ClientRegistration result = JsonBuilder.fromJson(json, OAuth2ClientRegistration.class);

		assertThat(result, notNullValue());
	}

	@Test
	void shouldSerializeToJson() {
		String json = Strings.fromFile("/security/oauth2/oauth2-client-registration.json");

		OAuth2ClientRegistration result1 = JsonBuilder.fromJson(json, OAuth2ClientRegistration.class);

		json = result1.toString();

		OAuth2ClientRegistration result2 = JsonBuilder.fromJson(json, OAuth2ClientRegistration.class);

		assertThat(result1.toString(), equalTo(result2.toString()));
	}
}
