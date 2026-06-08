package org.apiphany.security.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.apiphany.http.HttpHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test class for {@link DefaultHttpSensitivity}.
 *
 * @author Radu Sebastian LAZIN
 */
class DefaultHttpSensitivityTest {

	private static final List<String> SENSITIVE_HEADER_STRINGS = List.of(
			"Authorization", "authorization", "AuTHoRiZaTiOn",
			"Proxy-Authorization", "proxy-authorization",
			"Cookie", "cookie",
			"Set-Cookie", "set-cookie",
			"Set-Cookie2", "set-cookie2",
			"WWW-Authenticate", "www-authenticate",
			"Api-Key", "api-key",
			"X-API-Key", "x-api-key",
			"X-Auth-Token", "x-auth-token",
			"X-Authorization", "x-authorization");

	private static List<String> provideSensitiveHeaderStrings() {
		return SENSITIVE_HEADER_STRINGS;
	}

	@ParameterizedTest
	@MethodSource("provideSensitiveHeaderStrings")
	void shouldReturnTrueForSensitiveHeaders(final String headerName) {
		boolean sensitive = DefaultHttpSensitivity.instance().isSensitiveHeader(headerName);

		assertThat(sensitive, is(true));
	}

	@ParameterizedTest
	@EnumSource(HttpHeader.class)
	void shouldReturnFalseForNonSensitiveHeaders(final HttpHeader header) {
		if (SENSITIVE_HEADER_STRINGS.contains(header.value())) {
			return; // skip sensitive headers
		}
		boolean sensitive = DefaultHttpSensitivity.instance().isSensitiveHeader(header.value());

		assertThat(sensitive, is(false));
	}

	@ParameterizedTest
	@ValueSource(
		strings = {
				"token", "TOKEN", "access_token", "refresh_token", "api_key", "apikey", "code", "client_secret", "password" })
	void shouldReturnTrueForSensitiveParams(final String paramName) {
		boolean sensitive = DefaultHttpSensitivity.instance().isSensitiveParameter(paramName);

		assertThat(sensitive, is(true));
	}

	@ParameterizedTest
	@ValueSource(
		strings = {
				"id", "page", "limit", "sort", "query", "lang" })
	void shouldReturnFalseForNonSensitiveParams(final String paramName) {
		boolean sensitive = DefaultHttpSensitivity.instance().isSensitiveParameter(paramName);

		assertThat(sensitive, is(false));
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void shouldReturnFalseForNullInputs(final boolean header) {
		boolean sensitive = header
				? DefaultHttpSensitivity.instance().isSensitiveHeader(null)
				: DefaultHttpSensitivity.instance().isSensitiveParameter(null);

		assertThat(sensitive, is(false));
	}

	@Test
	void shouldReturnSameSingletonInstance() {
		Object first = DefaultHttpSensitivity.instance();
		Object second = DefaultHttpSensitivity.instance();

		assertThat(first, sameInstance(second));
		assertThat(first instanceof HttpSensitivity, equalTo(true));
	}
}
