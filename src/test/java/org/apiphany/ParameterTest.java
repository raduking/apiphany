package org.apiphany;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Parameter}.
 *
 * @author Radu Sebastian LAZIN
 */
class ParameterTest {

	private static final String PARAM_1 = "param1";
	private static final int TEST_INT = 666;
	private static final int TEST_INT_42 = 42;
	private static final Integer INTEGER_VALUE = TEST_INT;
	private static final String STRING_INTEGER_VALUE = String.valueOf(INTEGER_VALUE);

	static class Name {

		@Override
		public String toString() {
			return PARAM_1;
		}
	}

	@Test
	void shouldAddParametersToMapWithGenericArguments() {
		Parameter param = Parameter.of(new Name(), INTEGER_VALUE);

		Map<String, String> params = new HashMap<>();
		param.putInto(params);

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldAddParametersToAMapWithConsumerAPI() {
		Parameter param = Parameter.of(PARAM_1, STRING_INTEGER_VALUE);

		Map<String, String> params = new HashMap<>();
		param.accept(params);

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldAddNonStringParameterByConvertingItToString() {
		Map<String, String> params = RequestParameters.of(
				Parameter.of(PARAM_1, INTEGER_VALUE));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldAddNonStringSuppliedParameterByConvertingItToString() {
		Map<String, String> params = RequestParameters.of(
				Parameter.of(PARAM_1, () -> INTEGER_VALUE));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	static class A {

		String s;

		A(final String s) {
			this.s = s;
		}

		@Override
		public String toString() {
			return s;
		}
	}

	@Test
	void shouldAddNonStringSuppliedNonStringParameterByConvertingItToString() {
		Map<String, String> params = RequestParameters.of(
				Parameter.of(new A(PARAM_1), () -> INTEGER_VALUE));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldAddNonStringSuppliedNonStringSuppliedParameterByConvertingItToString() {
		Map<String, String> params = RequestParameters.of(
				Parameter.of(() -> new A(PARAM_1), () -> INTEGER_VALUE));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldAddAFilterParameter() {
		Filter filter = Filter.of("x", "==", "y");

		Map<String, String> params = RequestParameters.of(
				Parameter.of(filter));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(Filter.NAME), equalTo(filter.getValue()));
	}

	@Test
	void shouldAddFilterParameterWithValues() {
		Filter filter = Filter.of("x", "==", "y");

		Map<String, String> params = RequestParameters.of(
				Parameter.of(Filter.NAME, filter.getValue()));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(Filter.NAME), equalTo(filter.getValue()));
	}

	static class TestParameter implements Parameter {

		String name;
		String value;

		public TestParameter(final String name, final String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public void putInto(final Map<String, String> map) {
			map.put(name, value);
		}
	}

	@Test
	void shouldAddATestParameterParameter() {
		Parameter param = new TestParameter(PARAM_1, STRING_INTEGER_VALUE);

		Map<String, String> params = RequestParameters.of(
				Parameter.of(param));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldAddStringListParameter() {
		List<String> list = List.of("v1", "v2");

		Map<String, String> params = RequestParameters.of(
				Parameter.of(PARAM_1, list));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(String.join(",", list)));
	}

	@Test
	void shouldNotAddStringListParameterIfListIsEmpty() {
		List<String> list = Collections.emptyList();

		Map<String, String> params = RequestParameters.of(
				Parameter.of(PARAM_1, list));

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldNotAddStringListParameterIfListIsNull() {
		List<String> list = null;

		Map<String, String> params = RequestParameters.of(
				Parameter.of(PARAM_1, list));

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldAddListParameterByConvertingEachParameterToString() {
		List<Integer> list = List.of(INTEGER_VALUE, TEST_INT_42);

		Map<String, String> params = RequestParameters.of(
				Parameter.<String, Integer>of(PARAM_1, list));

		assertThat(params.get(PARAM_1), equalTo(String.join(",", list.stream().map(String::valueOf).toList())));
	}

	@Test
	void shouldPutAllValuesFromAMap() {
		Map<String, String> map = Map.of(PARAM_1, STRING_INTEGER_VALUE);

		Map<String, String> params = RequestParameters.of(
				Parameter.of(map));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldOverwriteExistingParametersFromAMap() {
		Map<String, String> map = Map.of(PARAM_1, STRING_INTEGER_VALUE);

		Map<String, String> params = RequestParameters.of(
				Parameter.of(PARAM_1, TEST_INT_42),
				Parameter.of(map));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldNotModifyExistingParametersWhenAddingNone() {
		Map<String, String> params = RequestParameters.of(
				Parameter.of(PARAM_1, STRING_INTEGER_VALUE),
				ParameterFunction.none());

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));

		Map<String, String> expected = new HashMap<>(params);

		Parameter.none().putInto(params);

		assertThat(params, equalTo(expected));
	}

	@Test
	void shouldReturnNoneOnParametersWhenParameterFunctionArrayIsNull() {
		Parameter param = Parameter.of((Parameter[]) null);

		assertThat(param, equalTo(Parameter.none()));
	}

	@Test
	void shouldReturnNoneOnWhenConditionIfTheConditionIsFalse() {
		Parameter param = new TestParameter(PARAM_1, STRING_INTEGER_VALUE);
		Parameter parameter = Parameter.when(false, param);

		assertThat(parameter, equalTo(Parameter.none()));
	}

	@Test
	void shouldAddParametersOnWhenConditionIfTheConditionIsTrue() {
		Parameter param = new TestParameter(PARAM_1, STRING_INTEGER_VALUE);
		Parameter parameter = Parameter.withNonNull(null, param);

		assertThat(parameter, equalTo(Parameter.none()));
	}

	@Test
	void shouldReturnNoneOnWhenNotNullConditionIfTheConditionIsFalse() {
		Parameter param = new TestParameter(PARAM_1, STRING_INTEGER_VALUE);
		Parameter parameter = Parameter.withNonNull(INTEGER_VALUE, param);

		Map<String, String> map = new HashMap<>();
		parameter.putInto(map);

		assertThat(map.entrySet(), hasSize(1));
		assertThat(map.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldReturnNoneOnWhenWithPredicateIfTheConditionIsFalse() {
		Parameter param = new TestParameter(PARAM_1, STRING_INTEGER_VALUE);
		Parameter parameter = Parameter.when(null, Objects::nonNull, param);

		assertThat(parameter, equalTo(Parameter.none()));
	}
}
