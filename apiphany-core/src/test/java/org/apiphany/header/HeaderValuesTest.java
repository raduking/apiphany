package org.apiphany.header;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apiphany.http.HttpHeaderValues;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link HeaderValues}.
 *
 * @author Radu Sebastian LAZIN
 */
class HeaderValuesTest {

	private static final String HEADER_VALUE1 = "headerValue1";
	private static final String HEADER_VALUE2 = "headerValue2";
	private static final String HEADER_NAME1 = "headerName1";

	private static Stream<Arguments> provideHeadersObjectsForChainWithoutHandlers() {
		return Stream.of(
				Arguments.of(Map.of(HEADER_NAME1, List.of(HEADER_VALUE1))),
				Arguments.of(new Object()),
				Arguments.of(Collections.emptyMap()));
	}

	@ParameterizedTest
	@MethodSource("provideHeadersObjectsForChainWithoutHandlers")
	void shouldReturnEmptyListFromHeadersWhenNoElementsAreAddedToTheChain(final Object headers) {
		HeaderValues chain = new HeaderValues();

		List<String> values = chain.get(HEADER_NAME1, headers);

		assertThat(values, hasSize(0));
	}

	@ParameterizedTest
	@MethodSource("provideHeadersObjectsForChainWithoutHandlers")
	void shouldReturnFalseOnContainsWhenNoElementsAreAddedToTheChain(final Object headers) {
		HeaderValues chain = new HeaderValues();

		boolean contains = chain.contains(HEADER_NAME1, HEADER_VALUE1, headers);

		assertFalse(contains);
	}

	@Test
	void shouldReturnListFromMapHeadersChainContainsMapHeaderValues() {
		HeaderValues chain = new HeaderValues().addFirst(new MapHeaderValues());

		var headers = Map.of(HEADER_NAME1, List.of(HEADER_VALUE1));

		List<String> values = chain.get(HEADER_NAME1, headers);

		assertThat(values, hasSize(1));
		assertThat(values.getFirst(), equalTo(HEADER_VALUE1));
	}

	@Test
	void shouldReturnListFromMapHeadersWithMoreElementsChainContainsMapHeaderValues() {
		HeaderValues chain = new HeaderValues().addFirst(new MapHeaderValues());

		var headerValues = List.of(HEADER_VALUE1, HEADER_VALUE2);
		var headers = Map.of(
				HEADER_NAME1, headerValues,
				"headerName2", List.of("v3", "v4"));

		List<String> values = chain.get(HEADER_NAME1, headers);

		assertThat(values, equalTo(headerValues));
	}

	@ParameterizedTest
	@MethodSource("provideHeadersObjectsForChainWithoutHandlers")
	void shouldReturnEmptyListFromHeadersWhenChainIsNotEmptyButNoHeaderValuesCanHandle(final Object headers) {
		HeaderValues chain = new HeaderValues().addFirst(new HttpHeaderValues());

		List<String> values = chain.get(HEADER_NAME1, headers);

		assertThat(values, hasSize(0));
	}
}
