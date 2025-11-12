package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OAuth2ErrorResponse}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2ErrorResponseTest {

	@Test
	void shouldLoadFromFile() {
		String json = Strings.fromFile("/security/oauth2/oauth2-error-response.json");

		OAuth2ErrorResponse result = JsonBuilder.fromJson(json, OAuth2ErrorResponse.class);

		assertThat(result, notNullValue());
		assertThat(result.getError(), equalTo(OAuth2ErrorCode.INVALID_CLIENT));
		assertThat(result.getErrorDescription(), equalTo("Client authentication failed"));
		assertThat(result.getErrorUri(), equalTo("https://server.example.com/errors/invalid_client"));
		assertThat(result.getState(), equalTo("some optional state"));
		assertThat(result.getHint(), equalTo("some optional hint"));
	}

	@Test
	void shouldSerializeToJson() {
		String json = Strings.fromFile("/security/oauth2/oauth2-error-response.json");

		OAuth2ErrorResponse result1 = JsonBuilder.fromJson(json, OAuth2ErrorResponse.class);

		json = result1.toString();

		OAuth2ErrorResponse result2 = JsonBuilder.fromJson(json, OAuth2ErrorResponse.class);

		assertThat(result1.toString(), equalTo(result2.toString()));
	}

	@Test
	void shouldBuildFromJasonWithInvalidErrorCode() {
		String json = "{\"error\": \"mumu\"}";

		OAuth2ErrorResponse result = JsonBuilder.fromJson(json, OAuth2ErrorResponse.class);

		assertThat(result, notNullValue());
		assertThat(result.getError(), equalTo(OAuth2ErrorCode.UNKNOWN));
	}
}
