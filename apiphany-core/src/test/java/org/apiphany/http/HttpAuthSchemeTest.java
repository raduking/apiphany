package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Test class for {@link HttpAuthScheme}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpAuthSchemeTest {

	@ParameterizedTest
	@EnumSource(HttpAuthScheme.class)
	void shouldBuildWithFromStringWithValidValue(final HttpAuthScheme httpAuthScheme) {
		String stringValue = httpAuthScheme.value();
		HttpAuthScheme result = HttpAuthScheme.fromString(stringValue);
		assertThat(result, equalTo(httpAuthScheme));
	}

}
