package org.apiphany.security.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link HttpAuthenticationScheme}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpAuthenticationSchemeTest {

	@ParameterizedTest
	@EnumSource(HttpAuthenticationScheme.class)
	void shouldBuildWithFromStringWithValidValue(final HttpAuthenticationScheme httpAuthScheme) {
		String stringValue = httpAuthScheme.value();
		HttpAuthenticationScheme result = HttpAuthenticationScheme.fromString(stringValue);
		assertThat(result, equalTo(httpAuthScheme));
	}
}
