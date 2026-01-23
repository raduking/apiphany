package org.apiphany.header;

import static org.apiphany.header.HeaderFunction.header;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link HeaderFunction}.
 *
 * @author Radu Sebastian LAZIN
 */
class HeaderFunctionTest {

	private static final String HEADER_1 = "header1";
	private static final Object HEADER_2 = "header2";
	private static final int TEST_INT = 666;
	private static final Integer INTEGER_VALUE = TEST_INT;
	private static final String STRING_INTEGER_VALUE = String.valueOf(INTEGER_VALUE);

	@Test
	void shouldAddHeadersToAMapWithConsumerAPI() {
		HeaderFunction header = header(HEADER_1, STRING_INTEGER_VALUE);

		Map<String, List<String>> headers = new HashMap<>();
		header.accept(headers);

		assertThat(headers.entrySet(), hasSize(1));
		assertThat(headers.get(HEADER_1), equalTo(List.of(STRING_INTEGER_VALUE)));
	}

	@Test
	void shouldAddNonStringHeaderByConvertingItToString() {
		var params = Headers.of(
				header(HEADER_2, INTEGER_VALUE));

		assertThat(params.entrySet(), hasSize(1));
		assertThat(params.get(String.valueOf(HEADER_2)), equalTo(List.of(STRING_INTEGER_VALUE)));
	}
}
