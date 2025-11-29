package org.apiphany.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apiphany.ApiMessage;
import org.apiphany.ApiMimeType;
import org.apiphany.header.HeaderValues;
import org.apiphany.header.MapHeaderValues;
import org.apiphany.http.HttpHeaderValues;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.morphix.reflection.GenericClass;

/**
 * Test class for {@link ContentConverter}.
 *
 * @author Radu Sebastian LAZIN
 */
class ContentConverterTest {

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
		TestContentConverter converter = new TestContentConverter();

		List<String> values = converter.getHeaderValues(HEADER_NAME1, headers, chain);

		assertThat(values, hasSize(0));
	}

	@Test
	void shouldReturnListFromMapHeadersChainContainsMapHeaderValues() {
		HeaderValues chain = new HeaderValues().addFirst(new MapHeaderValues());
		TestContentConverter converter = new TestContentConverter();

		var headers = Map.of(HEADER_NAME1, List.of(HEADER_VALUE1));

		List<String> values = converter.getHeaderValues(HEADER_NAME1, headers, chain);

		assertThat(values, hasSize(1));
		assertThat(values.getFirst(), equalTo(HEADER_VALUE1));
	}

	@Test
	void shouldReturnListFromMapHeadersWithMoreElementsChainContainsMapHeaderValues() {
		HeaderValues chain = new HeaderValues().addFirst(new MapHeaderValues());
		TestContentConverter converter = new TestContentConverter();

		var headerValues = List.of(HEADER_VALUE1, HEADER_VALUE2);
		var headers = Map.of(
				HEADER_NAME1, headerValues,
				"headerName2", List.of("v3", "v4"));

		List<String> values = converter.getHeaderValues(HEADER_NAME1, headers, chain);

		assertThat(values, equalTo(headerValues));
	}

	@ParameterizedTest
	@MethodSource("provideHeadersObjectsForChainWithoutHandlers")
	void shouldReturnEmptyListFromHeadersWhenChainIsNotEmptyButNoHeaderValuesCanHandle(final Object headers) {
		HeaderValues chain = new HeaderValues().addFirst(new HttpHeaderValues());
		TestContentConverter converter = new TestContentConverter();

		List<String> values = converter.getHeaderValues(HEADER_NAME1, headers, chain);

		assertThat(values, hasSize(0));
	}

	static class TestContentConverter implements ContentConverter<Object> {

		@Override
		public Object from(final Object obj, final ApiMimeType contentType, final Class<Object> dstClass) {
			return null;
		}

		@Override
		public Object from(final Object obj, final ApiMimeType contentType, final GenericClass<Object> genericDstClass) {
			return null;
		}

		@Override
		public <U, H> boolean isConvertible(final ApiMessage<U> message, final ApiMimeType contentType, final H headers, final HeaderValues headerValues) {
			return false;
		}

	}

}
