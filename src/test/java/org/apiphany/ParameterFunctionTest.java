package org.apiphany;

import static org.apiphany.ParameterFunction.parameter;
import static org.apiphany.ParameterFunction.parameters;
import static org.apiphany.ParameterFunction.when;
import static org.apiphany.ParameterFunction.withNonNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ParameterFunction}.
 *
 * @author Radu Sebastian LAZIN
 */
class ParameterFunctionTest {

	private static final String PARAM_1 = "param1";
	private static final int TEST_INT = 666;
	private static final int TEST_INT_42 = 42;
	private static final Integer INTEGER_VALUE = TEST_INT;
	private static final String STRING_INTEGER_VALUE = String.valueOf(INTEGER_VALUE);

	@Test
	void shouldAddParametersToAMapWithConsumerAPI() {
		ParameterFunction param = parameter(PARAM_1, STRING_INTEGER_VALUE);

		Map<String, String> params = new HashMap<>();
		param.accept(params);

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldAddNonStringParameterByConvertingItToString() {
		Map<String, String> params = RequestParameters.of(
				parameter(PARAM_1, INTEGER_VALUE));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldAddNonStringSuppliedParameterByConvertingItToString() {
		Map<String, String> params = RequestParameters.of(
				parameter(PARAM_1, () -> INTEGER_VALUE));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	static class A {

		final String s;

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
				parameter(new A(PARAM_1), () -> INTEGER_VALUE));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldAddNonStringSuppliedNonStringSuppliedParameterByConvertingItToString() {
		Map<String, String> params = RequestParameters.of(
				parameter(() -> new A(PARAM_1), () -> INTEGER_VALUE));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldAddAFilterParameter() {
		Filter filter = Filter.of("x", "==", "y");

		Map<String, String> params = RequestParameters.of(
				parameter(filter));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(Filter.NAME), equalTo(filter.getValue()));
	}

	@Test
	void shouldAddFilterParameterWithValues() {
		Filter filter = Filter.of("x", "==", "y");

		Map<String, String> params = RequestParameters.of(
				parameter(Filter.NAME, filter.getValue()));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(Filter.NAME), equalTo(filter.getValue()));
	}

	static class Parameter implements ParameterFunction {

		final String name;
		final String value;

		public Parameter(final String name, final String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public void putInto(final Map<String, String> map) {
			map.put(name, value);
		}
	}

	@Test
	void shouldAddAParameterFunctionParameter() {
		Parameter param = new Parameter(PARAM_1, STRING_INTEGER_VALUE);

		Map<String, String> params = RequestParameters.of(
				parameter(param));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldAddStringListParameter() {
		List<String> list = List.of("v1", "v2");

		Map<String, String> params = RequestParameters.of(
				parameter(PARAM_1, list));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(String.join(",", list)));
	}

	@Test
	void shouldNotAddStringListParameterIfListIsEmpty() {
		List<String> list = Collections.emptyList();

		Map<String, String> params = RequestParameters.of(
				parameter(PARAM_1, list));

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldNotAddStringListParameterIfListIsNull() {
		List<String> list = null;

		Map<String, String> params = RequestParameters.of(
				parameter(PARAM_1, list));

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldAddListParameterByConvertingEachParameterToString() {
		List<Integer> list = List.of(INTEGER_VALUE, TEST_INT_42);

		Map<String, String> params = RequestParameters.of(
				ParameterFunction.<String, Integer>parameter(PARAM_1, list));

		assertThat(params.get(PARAM_1), equalTo(String.join(",", list.stream().map(String::valueOf).toList())));
	}

	@Test
	void shouldPutAllValuesFromAMap() {
		Map<String, String> map = Map.of(PARAM_1, STRING_INTEGER_VALUE);

		Map<String, String> params = RequestParameters.of(
				parameters(map));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldOverwriteExistingParametersFromAMap() {
		Map<String, String> map = Map.of(PARAM_1, STRING_INTEGER_VALUE);

		Map<String, String> params = RequestParameters.of(
				parameter(PARAM_1, TEST_INT_42),
				parameters(map));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldNotModifyExistingParametersWhenAddingNone() {
		Map<String, String> params = RequestParameters.of(
				parameter(PARAM_1, STRING_INTEGER_VALUE),
				ParameterFunction.none());

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));

		@SuppressWarnings("unchecked")
		Map<String, String> mockMap = mock(Map.class);
		ParameterFunction.none().putInto(mockMap);

		verifyNoInteractions(mockMap);
	}

	@Test
	void shouldReturnNoneOnParametersWhenParameterFunctionArrayIsNull() {
		ParameterFunction parameterFunction = parameters((ParameterFunction[]) null);

		@SuppressWarnings("unchecked")
		Map<String, String> mockMap = mock(Map.class);
		parameterFunction.putInto(mockMap);

		verifyNoInteractions(mockMap);
	}

	@Test
	void shouldReturnNoneOnWhenConditionIfTheConditionIsFalse() {
		Parameter param = new Parameter(PARAM_1, STRING_INTEGER_VALUE);
		ParameterFunction parameterFunction = when(false, param);

		@SuppressWarnings("unchecked")
		Map<String, String> mockMap = mock(Map.class);
		parameterFunction.putInto(mockMap);

		verifyNoInteractions(mockMap);
	}

	@Test
	void shouldAddParametersOnWhenConditionIfTheConditionIsTrue() {
		Parameter param = new Parameter(PARAM_1, STRING_INTEGER_VALUE);
		ParameterFunction parameterFunction = withNonNull(null, param);

		@SuppressWarnings("unchecked")
		Map<String, String> mockMap = mock(Map.class);
		parameterFunction.putInto(mockMap);

		verifyNoInteractions(mockMap);
	}

	@Test
	void shouldReturnNoneOnWhenNotNullConditionIfTheConditionIsFalse() {
		Parameter param = new Parameter(PARAM_1, STRING_INTEGER_VALUE);
		ParameterFunction parameterFunction = withNonNull(INTEGER_VALUE, param);

		Map<String, String> map = new HashMap<>();
		parameterFunction.putInto(map);

		assertThat(map.entrySet(), hasSize(1));
		assertThat(map.get(PARAM_1), equalTo(STRING_INTEGER_VALUE));
	}

	@Test
	void shouldReturnNoneOnWhenWithPredicateIfTheConditionIsFalse() {
		Parameter param = new Parameter(PARAM_1, STRING_INTEGER_VALUE);
		ParameterFunction parameterFunction = when(null, Objects::nonNull, param);

		@SuppressWarnings("unchecked")
		Map<String, String> mockMap = mock(Map.class);
		parameterFunction.putInto(mockMap);

		verifyNoInteractions(mockMap);
	}
}
