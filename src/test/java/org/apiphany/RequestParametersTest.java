package org.apiphany;

import static org.apiphany.ParameterFunction.parameter;
import static org.apiphany.ParameterFunction.withCondition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.Map;

import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link RequestParameters}.
 *
 * @author Radu Sebastian LAZIN
 */
class RequestParametersTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = Tests.verifyDefaultConstructorThrows(RequestParameters.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldCreateMapWithAllParameters() {
		Map<String, String> requestParameters = RequestParameters.of(
				parameter("x1", "y1"),
				withCondition(true,
						parameter("x2", "y2")));

		Map<String, String> expected = new HashMap<>();
		expected.put("x1", "y1");
		expected.put("x2", "y2");

		assertThat(requestParameters, equalTo(expected));
	}

	@Test
	void shouldReturnEmptyMapIfNoParametersWereSupplied() {
		Map<String, String> params = RequestParameters.of(new ParameterFunction[0]);

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldReturnEmptyMapIfParametersIsNullSupplied() {
		Map<String, String> params = RequestParameters.of((ParameterFunction[]) null);

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldReturnEmptyMapIfParametersStringIsEmpty() {
		Map<String, String> params = RequestParameters.from("");

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldReturnEmptyMapIfParametersStringIsNull() {
		Map<String, String> params = RequestParameters.from(null);

		assertThat(params.entrySet(), hasSize(0));
	}
}
