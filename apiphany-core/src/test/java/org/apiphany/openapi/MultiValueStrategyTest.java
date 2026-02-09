package org.apiphany.openapi;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link MultiValueStrategy}.
 *
 * @author Radu Sebastian LAZIN
 */
class MultiValueStrategyTest {

	private static final String MULTI = "multi";
	private static final String CSV = "csv";
	private static final String SSV = "ssv";
	private static final String TSV = "tsv";
	private static final String PIPES = "pipes";

	private static final String TEST_PARAM = "testParam";
	private static final String VALUE1 = "value1";
	private static final String VALUE2 = "value2";

	@ParameterizedTest
	@EnumSource(MultiValueStrategy.class)
	void shouldHaveNonNullStyleAndSeparator(final MultiValueStrategy encoding) {
		assertNotNull(encoding.style(), "Style should not be null");
		assertNotNull(encoding.separator(), "Separator should not be null");
	}

	@ParameterizedTest
	@MethodSource("provideAllStrategies")
	void shouldHaveCorrectStyleAndSeparator(final MultiValueStrategy encoding, final String expectedStyle, final String expectedSeparator) {
		assertEquals(expectedStyle, encoding.style(), "Style should match expected value");
		assertEquals(expectedSeparator, encoding.separator(), "Separator should match expected value");
	}

	@Test
	void shouldHaveDefaultStrategy() {
		assertEquals(MultiValueStrategy.MULTI, MultiValueStrategy.DEFAULT, "Default strategy should be MULTI");
	}

	@Test
	void shouldHaveCorrectStringRepresentations() {
		assertEquals(MULTI, MultiValueStrategy.Value.MULTI, "MULTI string representation should be 'multi'");
		assertEquals(CSV, MultiValueStrategy.Value.CSV, "CSV string representation should be 'csv'");
		assertEquals(SSV, MultiValueStrategy.Value.SSV, "SSV string representation should be 'ssv'");
		assertEquals(TSV, MultiValueStrategy.Value.TSV, "TSV string representation should be 'tsv'");
		assertEquals(PIPES, MultiValueStrategy.Value.PIPES, "PIPES string representation should be 'pipes'");
	}

	@Test
	void shouldDefaultToMultiValueStrategyOnFrom() {
		assertEquals(MultiValueStrategy.MULTI, MultiValueStrategy.from("multi"), "from('multi') should return MULTI strategy");
		assertEquals(MultiValueStrategy.MULTI, MultiValueStrategy.from("unknown"), "from('unknown') should default to MULTI strategy");
	}

	@ParameterizedTest
	@MethodSource("provideAllStrategies")
	@SuppressWarnings("unused")
	void shouldReturnCorrectStrategyFromString(final MultiValueStrategy expectedStrategy, final String style, final String separator) {
		assertEquals(expectedStrategy, MultiValueStrategy.from(style), "from('" + style + "') should return " + expectedStrategy);
	}

	@Test
	void shouldReturnDefaultStrategyWhenFromStringIsNullOrEmpty() {
		assertEquals(MultiValueStrategy.DEFAULT, MultiValueStrategy.from((String) null), "from(null) should return the default strategy");
		assertEquals(MultiValueStrategy.DEFAULT, MultiValueStrategy.from(""), "from(\"\") should return the default strategy");
	}

	@Test
	void shouldReturnDefaultStrategyFromQueryParamAnnotation() {
		QueryParam annotation = queryParamAnnotation("", ParameterStyle.FORM, ParameterMode.EXPLODE);

		MultiValueStrategy strategy = MultiValueStrategy.from(annotation);

		assertThat(strategy, equalTo(MultiValueStrategy.MULTI));
	}

	@Test
	void shouldReturnDefaultStrategyFromNullQueryParamAnnotation() {
		MultiValueStrategy strategy = MultiValueStrategy.from((QueryParam) null);

		assertThat(strategy, equalTo(MultiValueStrategy.MULTI));
	}

	@ParameterizedTest
	@MethodSource("provideAllStrategies")
	@SuppressWarnings("unused")
	void shouldReturnCorrectStrategyFromQueryParamAnnotation(final MultiValueStrategy expectedStrategy, final String style, final String separator) {
		QueryParam annotation = queryParamAnnotation(expectedStrategy.style(), null, null);

		MultiValueStrategy strategy = MultiValueStrategy.from(annotation);

		assertThat(strategy, equalTo(expectedStrategy));
	}

	@Test
	void shouldReturnCSVStrategyFromQueryParamAnnotationWithFormStyleAndJoinedMode() {
		QueryParam annotation = queryParamAnnotation("", ParameterStyle.FORM, ParameterMode.JOINED);

		MultiValueStrategy strategy = MultiValueStrategy.from(annotation);

		assertThat(strategy, equalTo(MultiValueStrategy.CSV));
	}

	@Test
	void shouldReturnSSVStrategyFromQueryParamAnnotationWithSpaceDelimitedStyle() {
		QueryParam annotation = queryParamAnnotation("", ParameterStyle.SPACE_DELIMITED, null);

		MultiValueStrategy strategy = MultiValueStrategy.from(annotation);

		assertThat(strategy, equalTo(MultiValueStrategy.SSV));
	}

	@Test
	void shouldReturnPIPESStrategyFromQueryParamAnnotationWithPipesDelimitedStyle() {
		QueryParam annotation = queryParamAnnotation("", ParameterStyle.PIPE_DELIMITED, null);

		MultiValueStrategy strategy = MultiValueStrategy.from(annotation);

		assertThat(strategy, equalTo(MultiValueStrategy.PIPES));
	}

	@Test
	void shouldThrowUnsupportedOperationExceptionForUnsupportedParameterStyle() {
		QueryParam annotation = queryParamAnnotation("", ParameterStyle.SIMPLE, null);

		UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> MultiValueStrategy.from(annotation));

		assertThat(exception.getMessage(), equalTo("Unsupported query parameter style: SIMPLE"));
	}

	@ParameterizedTest
	@MethodSource("provideNonMultiStrategies")
	@SuppressWarnings("unused")
	void shouldCorrectlyAddTheParameterToTheMap(final MultiValueStrategy strategy, final String style, final String separator) {
		String parameterName = TEST_PARAM;
		String parameterValue1 = VALUE1;
		String parameterValue2 = VALUE2;
		List<String> values = List.of(parameterValue1, parameterValue2);
		Map<String, List<String>> map = new HashMap<>();

		strategy.apply(parameterName, values).putInto(map);

		assertThat(map.size(), equalTo(1));
		assertThat(map.get(parameterName), equalTo(List.of(String.join(separator, values))));
	}

	@ParameterizedTest
	@MethodSource("provideNonMultiStrategies")
	@SuppressWarnings("unused")
	void shouldNotAddParameterToMapWhenValuesAreEmpty(final MultiValueStrategy strategy, final String style, final String separator) {
		String parameterName = TEST_PARAM;
		List<String> values = List.of();
		Map<String, List<String>> map = new HashMap<>();

		strategy.apply(parameterName, values).putInto(map);

		assertThat(map.isEmpty(), equalTo(true));
	}

	@Test
	void shouldCorrectlyAddTheParameterToTheMap() {
		String parameterName = TEST_PARAM;
		String parameterValue1 = VALUE1;
		String parameterValue2 = VALUE2;
		List<String> values = List.of(parameterValue1, parameterValue2);
		Map<String, List<String>> map = new HashMap<>();

		MultiValueStrategy.MULTI.apply(parameterName, values).putInto(map);

		assertThat(map.size(), equalTo(1));
		assertThat(map.get(parameterName), equalTo(values));
	}

	@Test
	void shouldNotAddParameterToMapWhenValuesAreEmptyWithMulti() {
		String parameterName = TEST_PARAM;
		List<String> values = List.of();
		Map<String, List<String>> map = new HashMap<>();

		MultiValueStrategy.MULTI.apply(parameterName, values).putInto(map);

		assertThat(map.isEmpty(), equalTo(true));
	}

	@Test
	void shouldThrowExceptionWhenTryingToCallValuesConstructor() {
		UnsupportedOperationException exception = assertDefaultConstructorThrows(MultiValueStrategy.Value.class);

		assertThat(exception.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	private static Stream<Arguments> provideAllStrategies() {
		return Stream.of(
				Arguments.of(MultiValueStrategy.MULTI, MULTI, ""),
				Arguments.of(MultiValueStrategy.CSV, CSV, ","),
				Arguments.of(MultiValueStrategy.SSV, SSV, " "),
				Arguments.of(MultiValueStrategy.TSV, TSV, "\t"),
				Arguments.of(MultiValueStrategy.PIPES, PIPES, "|"));
	}

	private static Stream<Arguments> provideNonMultiStrategies() {
		return Stream.of(
				Arguments.of(MultiValueStrategy.CSV, CSV, ","),
				Arguments.of(MultiValueStrategy.SSV, SSV, " "),
				Arguments.of(MultiValueStrategy.TSV, TSV, "\t"),
				Arguments.of(MultiValueStrategy.PIPES, PIPES, "|"));
	}

	private static QueryParam queryParamAnnotation(final String strategy, final ParameterStyle style, final ParameterMode mode) {
		return new QueryParam() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return QueryParam.class;
			}

			@Override
			public String strategy() {
				return strategy;
			}

			@Override
			public ParameterStyle style() {
				return style;
			}

			@Override
			public ParameterMode mode() {
				return mode;
			}
		};
	}
}
