package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OAuth2ProviderDetails}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2ProviderDetailsTest {

	@Test
	void shouldLoadFromFile() {
		String json = Strings.fromFile("/security/oauth2/oauth2-provider-details.json");

		OAuth2ProviderDetails result = JsonBuilder.fromJson(json, OAuth2ProviderDetails.class);

		assertThat(result, notNullValue());
	}

	@Test
	void shouldSerializeToJson() {
		String json = Strings.fromFile("/security/oauth2/oauth2-provider-details.json");

		OAuth2ProviderDetails result1 = JsonBuilder.fromJson(json, OAuth2ProviderDetails.class);

		json = result1.toString();

		OAuth2ProviderDetails result2 = JsonBuilder.fromJson(json, OAuth2ProviderDetails.class);

		assertThat(result1.toString(), equalTo(result2.toString()));
	}
}
