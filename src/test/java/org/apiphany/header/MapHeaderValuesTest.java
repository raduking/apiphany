package org.apiphany.header;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link MapHeaderValues}.
 *
 * @author Radu Sebastian LAZIN
 */
class MapHeaderValuesTest {

	private static final String HEADER_VALUE = "headerValue";

	@Test
	void shouldReturnHeaderWhenCaseDoesntMatch() {
		Map<String, List<String>> headers = Map.of("some-header", List.of(HEADER_VALUE));

		List<String> headerValues = MapHeaderValues.get("Some-Header", headers);

		assertThat(headerValues, hasSize(1));
		assertThat(headerValues.getFirst(), equalTo(HEADER_VALUE));
	}

	@Test
	void shouldReturnEmptyListOnGetFromEmptyHeadersMap() {
		List<String> headerValues = MapHeaderValues.get("Some-Header", Map.of());

		assertThat(headerValues, notNullValue());
		assertThat(headerValues, hasSize(0));
	}

}
