package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
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

	static class TestDto {

		private AuthenticationMethod authenticationMethod;

		public AuthenticationMethod getAuthenticationMethod() {
			return authenticationMethod;
		}

		public void setAuthenticationMethod(final AuthenticationMethod authenticationMethod) {
			this.authenticationMethod = authenticationMethod;
		}
	}

	@ParameterizedTest
	@EnumSource(AuthenticationMethod.class)
	void shouldSerializeAndDeserializeWithJackson(final AuthenticationMethod authenticationMethod) {
		TestDto test = new TestDto();
		test.setAuthenticationMethod(authenticationMethod);

		String json = JsonBuilder.toJson(test);

		TestDto result = JsonBuilder.fromJson(json, TestDto.class);

		assertThat(result.getAuthenticationMethod(), equalTo(authenticationMethod));
	}

	@ParameterizedTest
	@EnumSource(AuthenticationMethod.class)
	void shouldDeserializeFromLowerCaseValue(final AuthenticationMethod authenticationMethod) {
		String json = "{\"authenticationMethod\":\"" + authenticationMethod.value().toLowerCase() + "\"}";

		TestDto result = JsonBuilder.fromJson(json, TestDto.class);

		assertThat(result.getAuthenticationMethod(), equalTo(authenticationMethod));
	}

	@ParameterizedTest
	@EnumSource(AuthenticationMethod.class)
	void shouldDeserializeFromUpperCaseValue(final AuthenticationMethod authenticationMethod) {
		String json = "{\"authenticationMethod\":\"" + authenticationMethod.value().toUpperCase() + "\"}";

		TestDto result = JsonBuilder.fromJson(json, TestDto.class);

		assertThat(result.getAuthenticationMethod(), equalTo(authenticationMethod));
	}

	@ParameterizedTest
	@EnumSource(AuthenticationMethod.class)
	void shouldDeserializeFromLowerCaseName(final AuthenticationMethod authenticationMethod) {
		String json = "{\"authenticationMethod\":\"" + authenticationMethod.name().toLowerCase() + "\"}";

		TestDto result = JsonBuilder.fromJson(json, TestDto.class);

		assertThat(result.getAuthenticationMethod(), equalTo(authenticationMethod));
	}

	@ParameterizedTest
	@EnumSource(AuthenticationMethod.class)
	void shouldDeserializeFromUpperCaseName(final AuthenticationMethod authenticationMethod) {
		String json = "{\"authenticationMethod\":\"" + authenticationMethod.name().toUpperCase() + "\"}";

		TestDto result = JsonBuilder.fromJson(json, TestDto.class);

		assertThat(result.getAuthenticationMethod(), equalTo(authenticationMethod));
	}
}
