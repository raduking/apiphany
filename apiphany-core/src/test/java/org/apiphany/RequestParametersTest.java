package org.apiphany;

import static org.apiphany.ParameterFunction.parameter;
import static org.apiphany.ParameterFunction.withCondition;
import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apiphany.openapi.MultiValueStrategy;
import org.apiphany.openapi.ParameterMode;
import org.apiphany.openapi.ParameterStyle;
import org.apiphany.openapi.QueryParam;
import org.junit.jupiter.api.Nested;
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
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(RequestParameters.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldCreateMapWithAllParameters() {
		Map<String, List<String>> requestParameters = RequestParameters.of(
				parameter("x1", "y1"),
				withCondition(true,
						parameter("x2", "y2")));

		Map<String, List<String>> expected = new HashMap<>();
		expected.put("x1", List.of("y1"));
		expected.put("x2", List.of("y2"));

		assertThat(requestParameters, equalTo(expected));
	}

	@Test
	void shouldConvertParametersToUrlSuffix() {
		Map<String, List<String>> params = RequestParameters.of(
				parameter("param1", "value1"),
				parameter("param2", "value2"));

		String urlSuffix = RequestParameters.asUrlSuffix(params);

		assertThat(urlSuffix, equalTo("?param1=value1&param2=value2"));
	}

	@Test
	void shouldEncodeParameters() {
		Map<String, List<String>> params = RequestParameters.of(
				parameter("param 1", "value 1"),
				parameter("param&2", "value&2"),
				parameter("param,3", "value,2"));
		params = RequestParameters.encode(params);

		Map<String, List<String>> expected = Map.of(
				"param%201", List.of("value+1"),
				"param%262", List.of("value%262"),
				"param%2C3", List.of("value%2C2"));

		assertThat(params, equalTo(expected));
	}

	@Test
	void shouldReturnEmptyStringAsUrlSuffixIfNoParameters() {
		Map<String, List<String>> params = RequestParameters.of();

		String urlSuffix = RequestParameters.asUrlSuffix(params);

		assertThat(urlSuffix, equalTo(""));
	}

	@Test
	void shouldAppendWithQuestionMarkWhenUrlHasNoQuery() {
		Map<String, List<String>> params = RequestParameters.of(
				parameter("param1", "value1"),
				parameter("param2", "value2"));

		String urlSuffix = RequestParameters.asUrlSuffix(params, "http://example.com/path");

		assertThat(urlSuffix, equalTo("?param1=value1&param2=value2"));
	}

	@Test
	void shouldAppendWithAmpersandWhenUrlAlreadyHasQuery() {
		Map<String, List<String>> params = RequestParameters.of(
				parameter("b", "2"));

		String urlSuffix = RequestParameters.asUrlSuffix(params, "http://example.com/path?a=1");

		assertThat(urlSuffix, equalTo("&b=2"));
	}

	@Test
	void shouldAppendWithAmpersandWhenUrlHasMultipleExistingParams() {
		Map<String, List<String>> params = RequestParameters.of(
				parameter("c", "3"));

		String urlSuffix = RequestParameters.asUrlSuffix(params, "http://example.com/path?a=1&b=2");

		assertThat(urlSuffix, equalTo("&c=3"));
	}

	@Test
	void shouldAppendWithoutPrefixWhenUrlEndsWithQuestionMark() {
		Map<String, List<String>> params = RequestParameters.of(
				parameter("a", "1"));

		String urlSuffix = RequestParameters.asUrlSuffix(params, "http://example.com/path?");

		assertThat(urlSuffix, equalTo("a=1"));
	}

	@Test
	void shouldAppendWithoutPrefixWhenUrlEndsWithAmpersand() {
		Map<String, List<String>> params = RequestParameters.of(
				parameter("b", "2"));

		String urlSuffix = RequestParameters.asUrlSuffix(params, "http://example.com/path?a=1&");

		assertThat(urlSuffix, equalTo("b=2"));
	}

	@Test
	void shouldReturnEmptyStringAsUrlSuffixWithUrlIfNoParameters() {
		Map<String, List<String>> params = RequestParameters.of();

		String urlSuffix = RequestParameters.asUrlSuffix(params, "http://example.com/path?a=1");

		assertThat(urlSuffix, equalTo(""));
	}

	@Test
	void shouldReturnEmptyMapIfNoParametersWereSupplied() {
		Map<String, List<String>> params = RequestParameters.of();

		assertThat(params.entrySet(), hasSize(0));
	}

	@Test
	void shouldReturnEmptyMapIfParametersIsNullSupplied() {
		Map<String, List<String>> params = RequestParameters.of((ParameterFunction[]) null);

		assertThat(params.entrySet(), hasSize(0));
	}

	@ParameterizedTest
	@MethodSource("provideValuesForEmptyMapResult")
	void shouldReturnEmptyMap(final String parametersString) {
		Map<String, List<String>> params = RequestParameters.from(parametersString);

		assertThat(params.entrySet(), hasSize(0));
	}

	static Stream<Arguments> provideValuesForEmptyMapResult() {
		return Stream.of(
				Arguments.of(""),
				Arguments.of((String) null));
	}

	@Test
	void shouldDecodeParametersCorrectly() {
		Map<String, List<String>> params = RequestParameters.from("user%20name=John%20Doe");

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get("user name"), equalTo(List.of("John Doe")));
	}

	@Test
	void shouldReadParametersWithoutEquals() {
		Map<String, List<String>> params = RequestParameters.from("user");

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get("user"), equalTo(List.of("")));
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

		Map<String, List<String>> params = RequestParameters.from(testParams);

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("param1"), equalTo(List.of("value1")));
		assertThat(params.get("param2"), equalTo(List.of("value2")));
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

		Map<String, List<String>> params = RequestParameters.from(b);

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("key1"), equalTo(List.of("100")));
		assertThat(params.get("key2"), equalTo(List.of("200")));
	}

	static class C {

		private List<Integer> numbers;

		public List<Integer> getNumbers() {
			return numbers;
		}

		public void setNumbers(final List<Integer> numbers) {
			this.numbers = numbers;
		}
	}

	@Test
	void shouldConvertFromObjectWithCollectionFields() {
		C c = new C();
		c.setNumbers(List.of(1, 2, 3));

		Map<String, List<String>> params = RequestParameters.from(c);

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get("numbers"), equalTo(List.of("1", "2", "3")));
	}

	static class D {

		@QueryParam(mode = ParameterMode.JOINED)
		private List<Integer> numbers;

		public List<Integer> getNumbers() {
			return numbers;
		}

		public void setNumbers(final List<Integer> numbers) {
			this.numbers = numbers;
		}
	}

	@Test
	void shouldConvertFromObjectWithCollectionFieldsAnnotatedWithJoined() {
		D d = new D();
		d.setNumbers(List.of(1, 2, 3));

		Map<String, List<String>> params = RequestParameters.from(d);

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get("numbers"), equalTo(List.of("1,2,3")));
	}

	static class E {

		@QueryParam
		private List<Integer> numbers;

		public E() {
			// empty
		}

		public E(final List<Integer> numbers) {
			this.numbers = numbers;
		}

		public List<Integer> getNumbers() {
			return numbers;
		}

		public void setNumbers(final List<Integer> numbers) {
			this.numbers = numbers;
		}
	}

	@Test
	void shouldConvertFromObjectWithCollectionFieldsAnnotatedUsingDefaultMulti() {
		E e = new E();
		e.setNumbers(List.of(1, 2, 3));

		Map<String, List<String>> params = RequestParameters.from(e);

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get("numbers"), equalTo(List.of("1", "2", "3")));
	}

	static class F {

		private List<Integer> numbers;

		public F() {
			// empty
		}

		public F(final List<Integer> numbers) {
			this.numbers = numbers;
		}

		@QueryParam
		public List<Integer> getNumbers() {
			return numbers;
		}

		public void setNumbers(final List<Integer> numbers) {
			this.numbers = numbers;
		}
	}

	@Test
	void shouldConvertFromObjectWithCollectionFieldsAnnotatedUsingDefaultMultiOnGetter() {
		F f = new F();
		f.setNumbers(List.of(1, 2, 3));

		Map<String, List<String>> params = RequestParameters.from(f);

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get("numbers"), equalTo(List.of("1", "2", "3")));
	}

	static class G {

		@QueryParam(strategy = "multi")
		private List<Integer> numbers;

		public G() {
			// empty
		}

		public G(final List<Integer> numbers) {
			this.numbers = numbers;
		}

		public List<Integer> getNumbers() {
			return numbers;
		}

		public void setNumbers(final List<Integer> numbers) {
			this.numbers = numbers;
		}
	}

	@Test
	void shouldConvertFromObjectWithCollectionFieldsAnnotatedUsingExplicitMulti() {
		G g = new G();
		g.setNumbers(List.of(1, 2, 3));

		Map<String, List<String>> params = RequestParameters.from(g);

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get("numbers"), equalTo(List.of("1", "2", "3")));
	}

	@ParameterizedTest
	@MethodSource("provideValuesForEmptyMapFromObject")
	void shouldConvertToEmptyMapFrom(final Object object) {
		Map<String, List<String>> params = RequestParameters.from(object);

		assertThat(params.entrySet(), hasSize(0));
	}

	private static Stream<Arguments> provideValuesForEmptyMapFromObject() {
		return Stream.of(
				Arguments.of(new TestParams()),
				Arguments.of(new B()),
				Arguments.of(new Object()),
				Arguments.of((Object) null),
				Arguments.of(new E(List.of())));
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

		Map<String, List<String>> params = RequestParameters.from(inputMap);

		assertThat(params.entrySet(), hasSize(inputMap.size()));

		assertThat(params.get("param1"), equalTo(List.of("value1")));
		assertThat(params.get("param2"), equalTo(List.of("123")));
		assertThat(params.get("param3"), equalTo(List.of("a", "b", "c")));
		assertThat(params.get("param4"), equalTo(List.of("true", "false")));
		assertThat(params.get("param5"), equalTo(List.of("1", "2", "3")));
	}

	@Test
	void shouldAddParameterWithParameterRecord() {
		Map<String, List<String>> params = RequestParameters.of(
				RequestParameter.of("param1", "value1"),
				RequestParameter.of("param2", "value2"));

		assertThat(params.entrySet(), hasSize(2));
		assertThat(params.get("param1"), equalTo(List.of("value1")));
		assertThat(params.get("param2"), equalTo(List.of("value2")));
	}

	/**
	 * Tests how {@link MultiValueStrategy} (selected through {@link QueryParam}) influences the final request parameters
	 * string, i.e. what is actually sent on the wire.
	 */
	@Nested
	class MultiValueStrategyTests {

		private static final String TAGS = "tags";

		private static final List<String> LETTERS = List.of("a", "b", "c");
		private static final List<String> SPECIAL_CHARACTERS = List.of("a b", "c&d");

		static class PlainTags {

			@SuppressWarnings("unused")
			private final List<String> tags;

			PlainTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		static class DefaultMultiTags {

			@QueryParam
			private final List<String> tags;

			DefaultMultiTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		static class ExplicitMultiTags {

			@QueryParam(strategy = MultiValueStrategy.Value.MULTI)
			private final List<String> tags;

			ExplicitMultiTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		static class FormExplodeTags {

			@QueryParam(style = ParameterStyle.FORM, mode = ParameterMode.EXPLODE)
			private final List<String> tags;

			FormExplodeTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		static class CsvTags {

			@QueryParam(strategy = MultiValueStrategy.Value.CSV)
			private final List<String> tags;

			CsvTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		static class FormJoinedTags {

			@QueryParam(style = ParameterStyle.FORM, mode = ParameterMode.JOINED)
			private final List<String> tags;

			FormJoinedTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		static class SsvTags {

			@QueryParam(strategy = MultiValueStrategy.Value.SSV)
			private final List<String> tags;

			SsvTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		static class SpaceDelimitedTags {

			@QueryParam(style = ParameterStyle.SPACE_DELIMITED)
			private final List<String> tags;

			SpaceDelimitedTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		static class TsvTags {

			@QueryParam(strategy = MultiValueStrategy.Value.TSV)
			private final List<String> tags;

			TsvTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		static class PipesTags {

			@QueryParam(strategy = MultiValueStrategy.Value.PIPES)
			private final List<String> tags;

			PipesTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		static class PipeDelimitedTags {

			@QueryParam(style = ParameterStyle.PIPE_DELIMITED)
			private final List<String> tags;

			PipeDelimitedTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		static class MultiValues {

			@QueryParam
			private final Object values;

			MultiValues(final Object values) {
				this.values = values;
			}
		}

		static class CsvValues {

			@QueryParam(strategy = MultiValueStrategy.Value.CSV)
			private final Object values;

			CsvValues(final Object values) {
				this.values = values;
			}
		}

		static class MixedParams {

			@SuppressWarnings("unused")
			private String query = "x";

			@QueryParam
			private List<String> tags = List.of("a", "b");

			@QueryParam(strategy = MultiValueStrategy.Value.CSV)
			private List<Integer> ids = List.of(1, 2, 3);

			@QueryParam
			private List<String> empty = List.of();

			@QueryParam
			private List<String> missing = null;

			@SuppressWarnings("unused")
			private String nothing = null;
		}

		@FunctionalInterface
		interface ParamObjectConstructor extends Function<List<String>, Object> {
			// empty
		}

		@Test
		void shouldContainMultipleParametersWithTheSameNameWhenFieldIsAnnotatedWithExplicitMulti() {
			ExplicitMultiTags paramsObject = new ExplicitMultiTags(LETTERS);
			Map<String, List<String>> params = RequestParameters.from(paramsObject);

			String result = RequestParameters.asString(params);

			assertThat(params.entrySet(), hasSize(1));
			assertThat(params.get(TAGS), equalTo(LETTERS));
			assertThat(result, equalTo("tags=a&tags=b&tags=c"));
			assertThat(List.of(result.split(RequestParameters.SEPARATOR)), equalTo(List.of("tags=a", "tags=b", "tags=c")));
		}

		@Test
		void shouldContainMultipleParametersWithTheSameNameWhenFieldIsAnnotatedWithDefaultMulti() {
			DefaultMultiTags paramsObject = new DefaultMultiTags(LETTERS);
			Map<String, List<String>> params = RequestParameters.from(paramsObject);

			String result = RequestParameters.asString(params);

			assertThat(result, equalTo("tags=a&tags=b&tags=c"));
		}

		@Test
		void shouldContainMultipleParametersWithTheSameNameWhenFieldIsNotAnnotated() {
			PlainTags paramsObject = new PlainTags(LETTERS);
			Map<String, List<String>> params = RequestParameters.from(paramsObject);

			String result = RequestParameters.asString(params);

			assertThat(result, equalTo("tags=a&tags=b&tags=c"));
		}

		@Test
		void shouldBuildUrlSuffixWithRepeatedParametersWhenUsingMulti() {
			ExplicitMultiTags paramsObject = new ExplicitMultiTags(LETTERS);
			Map<String, List<String>> params = RequestParameters.from(paramsObject);

			String suffix = RequestParameters.asUrlSuffix(params);

			assertThat(suffix, equalTo("?tags=a&tags=b&tags=c"));
		}

		@Test
		void shouldAppendRepeatedParametersToUrlWithExistingQueryWhenUsingMulti() {
			ExplicitMultiTags paramsObject = new ExplicitMultiTags(LETTERS);
			Map<String, List<String>> params = RequestParameters.from(paramsObject);

			String suffix = RequestParameters.asUrlSuffix(params, "http://localhost/path?x=1");

			assertThat(suffix, equalTo("&tags=a&tags=b&tags=c"));
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("provideStrategySelectors")
		@SuppressWarnings("unused")
		void shouldBuildParametersStringAccordingToSelectedStrategy(final String selector, final ParamObjectConstructor factory,
				final String expected, final String expectedEncoded) {
			Object paramsObject = factory.apply(LETTERS);
			Map<String, List<String>> params = RequestParameters.from(paramsObject);

			String result = RequestParameters.asString(params);

			assertThat(result, equalTo(expected));
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("provideStrategySelectors")
		@SuppressWarnings("unused")
		void shouldBuildEncodedParametersStringAccordingToSelectedStrategy(final String selector, final ParamObjectConstructor factory,
				final String expected, final String expectedEncoded) {
			Object paramsObject = factory.apply(SPECIAL_CHARACTERS);
			Map<String, List<String>> params = RequestParameters.from(paramsObject);
			Map<String, List<String>> encoded = RequestParameters.encode(params);

			String result = RequestParameters.asString(encoded);

			assertThat(result, equalTo(expectedEncoded));
		}

		private static Stream<Arguments> provideStrategySelectors() {
			return Stream.of(
					Arguments.of(
							"no annotation (multi)",
							constructor(PlainTags::new),
							"tags=a&tags=b&tags=c",
							"tags=a+b&tags=c%26d"),
					Arguments.of(
							"@QueryParam (multi)",
							constructor(DefaultMultiTags::new),
							"tags=a&tags=b&tags=c",
							"tags=a+b&tags=c%26d"),
					Arguments.of(
							"strategy = multi",
							constructor(ExplicitMultiTags::new),
							"tags=a&tags=b&tags=c",
							"tags=a+b&tags=c%26d"),
					Arguments.of(
							"style = FORM, mode = EXPLODE (multi)",
							constructor(FormExplodeTags::new),
							"tags=a&tags=b&tags=c",
							"tags=a+b&tags=c%26d"),
					Arguments.of(
							"strategy = csv",
							constructor(CsvTags::new),
							"tags=a,b,c",
							"tags=a+b%2Cc%26d"),
					Arguments.of(
							"style = FORM, mode = JOINED (csv)",
							constructor(FormJoinedTags::new),
							"tags=a,b,c",
							"tags=a+b%2Cc%26d"),
					Arguments.of(
							"strategy = ssv",
							constructor(SsvTags::new),
							"tags=a b c",
							"tags=a+b+c%26d"),
					Arguments.of(
							"style = SPACE_DELIMITED (ssv)",
							constructor(SpaceDelimitedTags::new),
							"tags=a b c",
							"tags=a+b+c%26d"),
					Arguments.of(
							"strategy = tsv",
							constructor(TsvTags::new),
							"tags=a\tb\tc",
							"tags=a+b%09c%26d"),
					Arguments.of(
							"strategy = pipes",
							constructor(PipesTags::new),
							"tags=a|b|c",
							"tags=a+b%7Cc%26d"),
					Arguments.of(
							"style = PIPE_DELIMITED (pipes)",
							constructor(PipeDelimitedTags::new),
							"tags=a|b|c",
							"tags=a+b%7Cc%26d"));
		}

		private static ParamObjectConstructor constructor(final ParamObjectConstructor function) {
			return function;
		}

		@ParameterizedTest
		@MethodSource("provideCollectionKinds")
		void shouldContainMultipleParametersWithTheSameNameForAnyCollectionKindWhenUsingMulti(final Object values) {
			MultiValues paramsObject = new MultiValues(values);
			Map<String, List<String>> params = RequestParameters.from(paramsObject);

			String result = RequestParameters.asString(params);

			assertThat(result, equalTo("values=1&values=2&values=3"));
		}

		@ParameterizedTest
		@MethodSource("provideCollectionKinds")
		void shouldJoinValuesIntoSingleParameterForAnyCollectionKindWhenUsingCsv(final Object values) {
			CsvValues paramsObject = new CsvValues(values);
			Map<String, List<String>> params = RequestParameters.from(paramsObject);

			String result = RequestParameters.asString(params);

			assertThat(result, equalTo("values=1,2,3"));
		}

		private static Stream<Arguments> provideCollectionKinds() {
			Iterable<Integer> iterable = () -> List.of(1, 2, 3).iterator();
			return Stream.of(
					Arguments.of(List.of(1, 2, 3)),
					Arguments.of(new LinkedHashSet<>(List.of(1, 2, 3))),
					Arguments.of((Object) new String[] { "1", "2", "3" }),
					Arguments.of((Object) new Integer[] { 1, 2, 3 }),
					Arguments.of(new int[] { 1, 2, 3 }),
					Arguments.of(iterable));
		}

		@Test
		void shouldBuildCombinedParametersStringFromObjectWithMixedFields() {
			MixedParams paramsObject = new MixedParams();
			Map<String, List<String>> params = RequestParameters.from(paramsObject);

			String result = RequestParameters.asString(params);

			assertThat(List.of(result.split(RequestParameters.SEPARATOR)), containsInAnyOrder("query=x", "tags=a", "tags=b", "ids=1,2,3"));
			assertThat(result, containsString("tags=a&tags=b"));
		}

		@Test
		void shouldNotContainParametersForNullOrEmptyCollectionsOrNullValues() {
			MixedParams paramsObject = new MixedParams();
			Map<String, List<String>> params = RequestParameters.from(paramsObject);

			assertThat(params.keySet(), containsInAnyOrder("query", TAGS, "ids"));
		}

		@Test
		void shouldReturnEmptyStringWhenCollectionIsEmptyWithAnyStrategy() {
			ExplicitMultiTags multiParamsObject = new ExplicitMultiTags(List.of());
			Map<String, List<String>> multiParams = RequestParameters.from(multiParamsObject);
			String multiResult = RequestParameters.asString(multiParams);

			CsvTags csvParamsObject = new CsvTags(List.of());
			Map<String, List<String>> csvParams = RequestParameters.from(csvParamsObject);
			String csvResult = RequestParameters.asString(csvParams);

			assertThat(multiResult, equalTo(""));
			assertThat(csvResult, equalTo(""));
		}

		@Test
		void shouldParseBackRepeatedParametersIntoTheSameValuesWhenUsingMulti() {
			ExplicitMultiTags paramsObject = new ExplicitMultiTags(LETTERS);
			Map<String, List<String>> params = RequestParameters.from(paramsObject);
			String result = RequestParameters.asString(params);

			Map<String, List<String>> parsed = RequestParameters.from(result);

			assertThat(parsed.entrySet(), hasSize(1));
			assertThat(parsed.get(TAGS), equalTo(LETTERS));
		}

		@Test
		void shouldParseBackJoinedParameterIntoSingleValueWhenUsingCsv() {
			CsvTags paramsObject = new CsvTags(LETTERS);
			Map<String, List<String>> params = RequestParameters.from(paramsObject);
			String result = RequestParameters.asString(params);

			Map<String, List<String>> parsed = RequestParameters.from(result);

			assertThat(parsed.entrySet(), hasSize(1));
			assertThat(parsed.get(TAGS), equalTo(List.of("a,b,c")));
		}
	}
}
