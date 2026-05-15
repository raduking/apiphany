package org.apiphany;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ApiMethod}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiMethodTest {

	private static final String SOME_NAME = "someName";

	@Test
	void shouldReturnNameOnToString() {
		ApiMethod method = ApiMethod.of(SOME_NAME);

		assertThat(method.toString(), equalTo(SOME_NAME));
	}

	@Test
	void shouldReturnNameOnValue() {
		ApiMethod method = ApiMethod.of(SOME_NAME);

		assertThat(method.value(), equalTo(SOME_NAME));
	}

	@Test
	void shouldReturnUndefinedNameOnUndefined() {
		assertThat(ApiMethod.UNDEFINED.toString(), equalTo(ApiMethod.UNDEFINED_NAME));
		assertThat(ApiMethod.UNDEFINED.value(), equalTo(ApiMethod.UNDEFINED_NAME));
	}
}
