package org.apiphany;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

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
	void shouldReturnNullNameOnBuildingNull() {
		ApiMethod method = ApiMethod.of(null);

		assertThat(method.toString(), equalTo(null));
		assertThat(method.value(), equalTo(null));
	}

	@Test
	void shouldReturnUndefinedNameOnUndefined() {
		assertThat(ApiMethod.UNDEFINED.toString(), equalTo(ApiMethod.Value.UNDEFINED));
		assertThat(ApiMethod.UNDEFINED.value(), equalTo(ApiMethod.Value.UNDEFINED));
	}

	@Test
	void shouldThrowExceptionOnInstantiatingValue() {
		UnsupportedOperationException exception = Assertions.assertDefaultConstructorThrows(ApiMethod.Value.class);

		assertThat(exception.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}
}
