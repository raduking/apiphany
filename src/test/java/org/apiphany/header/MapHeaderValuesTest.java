package org.apiphany.header;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link MapHeaderValues}.
 *
 * @author Radu Sebastian LAZIN
 */
class MapHeaderValuesTest {

	private static final String HEADER_NAME = "headerName";
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

	@Test
	void shouldThrowExceptionOnNextForSimpleInstance() {
		MapHeaderValues mhv = new MapHeaderValues();
		IllegalStateException e = assertThrows(IllegalStateException.class, mhv::getNext);

		assertThat(e.getMessage(), equalTo("Cannot get header values, end of the chain reached."));
	}

	@Test
	void shouldThrowExceptionOnGetForSimpleInstanceAndNonMaps() {
		MapHeaderValues mhv = new MapHeaderValues();
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> mhv.get(HEADER_NAME, mhv));

		assertThat(e.getMessage(), equalTo("Cannot get header values, end of the chain reached."));
	}

	@Test
	void shouldReturnHeaderValueFromNextInChain() {
		MapHeaderValues mhv = new MapHeaderValues();
		HeaderValues next = mock(HeaderValues.class);
		doReturn(List.of(HEADER_VALUE)).when(next).get(HEADER_NAME, mhv);
		mhv.setNext(next);

		List<String> result = mhv.get(HEADER_NAME, mhv);

		assertThat(result, hasSize(1));
		assertThat(result.getFirst(), equalTo(HEADER_VALUE));
	}
}
