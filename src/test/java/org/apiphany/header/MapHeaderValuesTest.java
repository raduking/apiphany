package org.apiphany.header;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collections;
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
		Map<String, List<String>> mapHeaders = Map.of("some-header", List.of(HEADER_VALUE));

		List<String> headerValues = MapHeaderValues.get("Some-Header", mapHeaders);

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

	@Test
	void shouldReturnTrueOnContainsFromNextInChain() {
		MapHeaderValues mhv = new MapHeaderValues();
		HeaderValues next = mock(HeaderValues.class);
		doReturn(true).when(next).contains(HEADER_NAME, HEADER_VALUE, mhv);
		mhv.setNext(next);

		boolean result = mhv.contains(HEADER_NAME, HEADER_VALUE, mhv);

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnFalseOnContainsFromNextInChain() {
		MapHeaderValues mhv = new MapHeaderValues();
		HeaderValues next = mock(HeaderValues.class);
		doReturn(false).when(next).contains(HEADER_NAME, HEADER_VALUE, mhv);
		mhv.setNext(next);

		boolean result = mhv.contains(HEADER_NAME, HEADER_VALUE, mhv);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseOnContainsFromEmptyMapHeaders() {
		MapHeaderValues mhv = new MapHeaderValues();

		boolean result = mhv.contains(HEADER_NAME, HEADER_VALUE, Map.of());

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnHeaderWhenCaseDoesntMatchWithMapHeaderValuesObject() {
		MapHeaderValues mhv = new MapHeaderValues();
		Object mapHeaders = Map.of("some-header", List.of(HEADER_VALUE));

		List<String> headerValues = mhv.get("Some-Header", mapHeaders);

		assertThat(headerValues, hasSize(1));
		assertThat(headerValues.getFirst(), equalTo(HEADER_VALUE));
	}

	@Test
	void shouldReturnEmptyListOnGetFromEmptyMapHeadersWithMapHeaderValuesObject() {
		MapHeaderValues mhv = new MapHeaderValues();
		Object mapHeaders = Map.of();

		List<String> headerValues = mhv.get("Some-Header", mapHeaders);

		assertThat(headerValues, notNullValue());
		assertThat(headerValues, hasSize(0));
	}

	@Test
	void shouldReturnFalseOnContainsFromEmptyMapHeadersWithMapHeaderValuesObject() {
		MapHeaderValues mhv = new MapHeaderValues();
		Object mapHeaders = Map.of();

		boolean result = mhv.contains(HEADER_NAME, HEADER_VALUE, mapHeaders);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseWhenCheckingExistingHeaderInEmptyHeadersOnContains() {
		boolean result = MapHeaderValues.contains(HEADER_NAME, Collections.emptyMap());

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueWhenCheckingExistingHeaderInHeadersOnContains() {
		Map<String, List<String>> headers = Map.of(HEADER_NAME, List.of(HEADER_VALUE));

		boolean result = MapHeaderValues.contains(HEADER_NAME, headers);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueWhenCheckingExistingHeaderInHeadersIgnoringCaseOnContains() {
		Map<String, List<String>> headers = Map.of(HEADER_NAME, List.of(HEADER_VALUE));

		boolean result = MapHeaderValues.contains(HEADER_NAME.toUpperCase(), headers);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseWhenCheckingNonExistingHeaderInHeadersOnContains() {
		Map<String, List<String>> headers = Map.of(HEADER_NAME, List.of(HEADER_VALUE));

		boolean result = MapHeaderValues.contains("non-existing", headers);

		assertFalse(result);
	}
}
