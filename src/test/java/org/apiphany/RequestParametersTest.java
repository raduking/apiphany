package org.apiphany;

import static org.apiphany.ParameterFunction.parameter;
import static org.apiphany.ParameterFunction.withCondition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apiphany.utils.Tests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
	void shouldConvertParametersToUrlSuffix() {
		Map<String, String> params = RequestParameters.of(
				parameter("param1", "value1"),
				parameter("param2", "value2"));

		String urlSuffix = RequestParameters.asUrlSuffix(params);

		assertThat(urlSuffix, equalTo("?param1=value1&param2=value2"));
	}

	@Test
	void shouldEncodeParameters() {
		Map<String, String> params = RequestParameters.of(
				parameter("param 1", "value 1"),
				parameter("param&2", "value&2"));
		params = RequestParameters.encode(params);

		Map<String, String> expected = Map.of(
				"param+1", "value+1",
				"param%262", "value%262");

		assertThat(params, equalTo(expected));
	}

	@Test
	void shouldReturnEmptyStringAsUrlSuffixIfNoParameters() {
		Map<String, String> params = RequestParameters.of();

		String urlSuffix = RequestParameters.asUrlSuffix(params);

		assertThat(urlSuffix, equalTo(""));
	}

	@Test
	void shouldReturnEmptyMapIfNoParametersWereSupplied() {
		Map<String, String> params = RequestParameters.of();

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldReturnEmptyMapIfParametersIsNullSupplied() {
		Map<String, String> params = RequestParameters.of((ParameterFunction[]) null);

		assertThat(params.entrySet(), hasSize(0));
	}

	@ParameterizedTest
	@MethodSource("provideValuesForEmptyMapResult")
	void shouldReturnEmptyMap(final String parametersString) {
		Map<String, String> params = RequestParameters.from(parametersString);

		assertThat(params.entrySet(), hasSize(0));
	}

	static Stream<Arguments> provideValuesForEmptyMapResult() {
		return Stream.of(
				Arguments.of(""),
				Arguments.of((String) null));
	}

	@Test
	void shouldDecodeParametersCorrectly() {
		Map<String, String> params = RequestParameters.from("user%20name=John%20Doe");

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get("user name"), equalTo("John Doe"));
	}

	@Test
	void shouldReadParametersWithoutEquals() {
		Map<String, String> params = RequestParameters.from("user");

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get("user"), equalTo(""));
	}

	static class TestParams {

		private String param1;
		private String param2;

		public String getParam1() {
			return param1;
		}

		public void setParam1(final String param1) {
			this.param1 = param1;
		}

		public String getParam2() {
			return param2;
		}

		public void setParam2(final String param2) {
			this.param2 = param2;
		}
	}

	@Test
	void shouldConvertFromObject() {
		TestParams testParams = new TestParams();
		testParams.setParam1("value1");
		testParams.setParam2("value2");

		Map<String, String> params = RequestParameters.from(testParams);

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("param1"), equalTo("value1"));
		assertThat(params.get("param2"), equalTo("value2"));
	}

	static class B {

		private Integer key1;
		private Long key2;

		public Integer getKey1() {
			return key1;
		}

		public void setKey1(final Integer param1) {
			this.key1 = param1;
		}

		public Long getKey2() {
			return key2;
		}

		public void setKey2(final Long param2) {
			this.key2 = param2;
		}
	}

	@Test
	void shouldConvertFromObjectWithNonStringFields() {
		B b = new B();
		b.setKey1(100);
		b.setKey2(200L);

		Map<String, String> params = RequestParameters.from(b);

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("key1"), equalTo("100"));
		assertThat(params.get("key2"), equalTo("200"));
	}

	@ParameterizedTest
	@MethodSource("provideValuesForEmptyMapFromObject")
	void shouldConvertToEmptyMapFrom(final Object object) {
		Map<String, String> params = RequestParameters.from(object);

		assertThat(params.entrySet(), hasSize(0));
	}

	private static Stream<Arguments> provideValuesForEmptyMapFromObject() {
		return Stream.of(
				Arguments.of(new TestParams()),
				Arguments.of(new B()),
				Arguments.of(new Object()),
				Arguments.of((Object) null));
	}

	@Test
	void shouldThrowExceptionIfObjectIsList() {
		Object list = List.of("a", "b");
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> RequestParameters.from(list));

		assertThat(e.getMessage(), equalTo("Cannot convert a List into request parameters map. Expected a POJO or a Map."));
	}

	@Test
	void shouldThrowExceptionIfObjectIsSet() {
		Object set = Set.of("a", "b");
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> RequestParameters.from(set));

		assertThat(e.getMessage(), equalTo("Cannot convert a Set into request parameters map. Expected a POJO or a Map."));
	}

	@Test
	void shouldThrowExceptionIfObjectIsArray() {
		Object array = new String[] { "a", "b" };
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> RequestParameters.from(array));

		assertThat(e.getMessage(), equalTo("Cannot convert an Array into request parameters map. Expected a POJO or a Map."));
	}

	@Test
	void shouldConvertFromMap() {
		Map<String, Object> inputMap = Map.of(
				"param1", "value1",
				"param2", 123,
				"param3", List.of("a", "b", "c"),
				"param4", new boolean[] { true, false },
				"param5", new Integer[] { 1, 2, 3 });

		Map<String, String> params = RequestParameters.from(inputMap);

		assertThat(params.entrySet(), hasSize(inputMap.size()));

		assertThat(params.get("param1"), equalTo("value1"));
		assertThat(params.get("param2"), equalTo("123"));
		assertThat(params.get("param3"), equalTo("a,b,c"));
		assertThat(params.get("param4"), equalTo("true,false"));
		assertThat(params.get("param5"), equalTo("1,2,3"));
	}

	@Test
	void shouldAddParameterWithParameterRecord() {
		Map<String, String> params = RequestParameters.of(
				RequestParameter.of("param1", "value1"),
				RequestParameter.of("param2", "value2"));

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("param1"), equalTo("value1"));
		assertThat(params.get("param2"), equalTo("value2"));
	}
}
