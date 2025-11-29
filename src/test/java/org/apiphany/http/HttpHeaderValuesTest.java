package org.apiphany.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

import org.apiphany.header.HeaderValues;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link HttpHeaderValues}.
 *
 * @author Radu Sebastian LAZIN
 */
class HttpHeaderValuesTest {

	private static final String HEADER_NAME = "headerName";
	private static final String HEADER_VALUE = "headerValue";

	@Test
	void shouldReturnHeaderWhenCaseDoesntMatch() {
		HttpHeaders httpHeaders = HttpHeaders.of(Map.of("some-header", List.of(HEADER_VALUE)), (name, value) -> true);

		List<String> headerValues = HttpHeaderValues.get("Some-Header", httpHeaders);

		assertThat(headerValues, hasSize(1));
		assertThat(headerValues.getFirst(), equalTo(HEADER_VALUE));
	}

	@Test
	void shouldReturnEmptyListOnGetFromEmptyHeadersMap() {
		HttpHeaders httpHeaders = HttpHeaders.of(Map.of(), (name, value) -> true);

		List<String> headerValues = HttpHeaderValues.get("Some-Header", httpHeaders);

		assertThat(headerValues, notNullValue());
		assertThat(headerValues, hasSize(0));
	}

	@Test
	void shouldThrowExceptionOnNextForSimpleInstance() {
		HttpHeaderValues hhv = new HttpHeaderValues();
		IllegalStateException e = assertThrows(IllegalStateException.class, hhv::getNext);

		assertThat(e.getMessage(), equalTo("Cannot get header values, end of the chain reached."));
	}

	@Test
	void shouldThrowExceptionOnGetForSimpleInstanceAndNonMaps() {
		HttpHeaderValues hhv = new HttpHeaderValues();
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> hhv.get(HEADER_NAME, hhv));

		assertThat(e.getMessage(), equalTo("Cannot get header values, end of the chain reached."));
	}

	@Test
	void shouldReturnHeaderValueFromNextInChain() {
		HttpHeaderValues hhv = new HttpHeaderValues();
		HeaderValues next = mock(HeaderValues.class);
		doReturn(List.of(HEADER_VALUE)).when(next).get(HEADER_NAME, hhv);
		hhv.setNext(next);

		List<String> result = hhv.get(HEADER_NAME, hhv);

		assertThat(result, hasSize(1));
		assertThat(result.getFirst(), equalTo(HEADER_VALUE));
	}

	@Test
	void shouldReturnTrueOnContainsFromNextInChain() {
		HttpHeaderValues hhv = new HttpHeaderValues();
		HeaderValues next = mock(HeaderValues.class);
		doReturn(true).when(next).contains(HEADER_NAME, HEADER_VALUE, hhv);
		hhv.setNext(next);

		boolean result = hhv.contains(HEADER_NAME, HEADER_VALUE, hhv);

		assertThat(result, equalTo(true));
	}

	@Test
	void shouldReturnFalseOnContainsFromNextInChain() {
		HttpHeaderValues hhv = new HttpHeaderValues();
		HeaderValues next = mock(HeaderValues.class);
		doReturn(false).when(next).contains(HEADER_NAME, HEADER_VALUE, hhv);
		hhv.setNext(next);

		boolean result = hhv.contains(HEADER_NAME, HEADER_VALUE, hhv);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseOnContainsFromEmptyMapHeaders() {
		HttpHeaders httpHeaders = HttpHeaders.of(Map.of(), (name, value) -> true);
		HttpHeaderValues mhv = new HttpHeaderValues();

		boolean result = mhv.contains(HEADER_NAME, HEADER_VALUE, httpHeaders);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnHeaderWhenCaseDoesntMatchWithMapHeaderValuesObject() {
		HttpHeaderValues hhv = new HttpHeaderValues();
		Object httpHeaders = HttpHeaders.of(Map.of("some-header", List.of(HEADER_VALUE)), (name, value) -> true);

		List<String> headerValues = hhv.get("Some-Header", httpHeaders);

		assertThat(headerValues, hasSize(1));
		assertThat(headerValues.getFirst(), equalTo(HEADER_VALUE));
	}

	@Test
	void shouldReturnEmptyListOnGetFromEmptyMapHeadersWithMapHeaderValuesObject() {
		HttpHeaderValues hhv = new HttpHeaderValues();
		Object httpHeaders = HttpHeaders.of(Map.of(), (name, value) -> true);

		List<String> headerValues = hhv.get("Some-Header", httpHeaders);

		assertThat(headerValues, notNullValue());
		assertThat(headerValues, hasSize(0));
	}

	@Test
	void shouldReturnFalseOnContainsFromEmptyMapHeadersWithMapHeaderValuesObject() {
		HttpHeaderValues hhv = new HttpHeaderValues();
		Object mapHeaders = HttpHeaders.of(Map.of(), (name, value) -> true);

		boolean result = hhv.contains(HEADER_NAME, HEADER_VALUE, mapHeaders);

		assertThat(result, equalTo(false));
	}

	@Test
	void shouldReturnFalseWhenCheckingExistingHeaderInEmptyHeadersOnContains() {
		HttpHeaders httpHeaders = HttpHeaders.of(Map.of(), (name, value) -> true);

		boolean result = HttpHeaderValues.contains(HEADER_NAME, httpHeaders);

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueWhenCheckingExistingHeaderInHeadersOnContains() {
		HttpHeaders httpHeaders = HttpHeaders.of(Map.of(HEADER_NAME, List.of(HEADER_VALUE)), (name, value) -> true);

		boolean result = HttpHeaderValues.contains(HEADER_NAME, httpHeaders);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueWhenCheckingExistingHeaderInHeadersIgnoringCaseOnContains() {
		HttpHeaders httpHeaders = HttpHeaders.of(Map.of(HEADER_NAME, List.of(HEADER_VALUE)), (name, value) -> true);

		boolean result = HttpHeaderValues.contains(HEADER_NAME.toUpperCase(), httpHeaders);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseWhenCheckingNonExistingHeaderInHeadersOnContains() {
		HttpHeaders httpHeaders = HttpHeaders.of(Map.of(HEADER_NAME, List.of(HEADER_VALUE)), (name, value) -> true);

		boolean result = HttpHeaderValues.contains("non-existing", httpHeaders);

		assertFalse(result);
	}
}
