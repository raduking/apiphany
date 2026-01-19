package org.apiphany.header;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Headers}.
 *
 * @author Radu Sebastian LAZIN
 */
class HeadersTest {

	private static final String N1 = "n1";
	private static final String N2 = "n2";

	private static final String V1 = "v1";
	private static final String V2 = "v2";
	private static final String V3 = "v3";
	private static final String V4 = "v4";

	private static final String HEADER_VALUE = "headerValue";

	private final Map<String, List<String>> headers = new HashMap<>() {
		@Serial
		private static final long serialVersionUID = 1L;
		{
			put(N1, new ArrayList<>() {
				@Serial
				private static final long serialVersionUID = 1L;
				{
					add(V1);
					add(V2);
				}
			});
		}
	};

	@Test
	void shouldAddHeadersToExistingHeaders() {
		var headersToAdd = Map.of(N2, List.of(V3, V4));

		Headers.addTo(headers, headersToAdd);

		var expected = Map.of(
				N1, List.of(V1, V2),
				N2, List.of(V3, V4));

		assertThat(headers, equalTo(expected));
	}

	@Test
	void shouldNotAddNullHeader() {
		var headersToAdd = Map.of(N2, new ArrayList<>() {
			@Serial
			private static final long serialVersionUID = 1L;
			{
				add(null);
				add(null);
			}
		});

		Headers.addTo(headers, headersToAdd);

		var expected = Map.of(
				N1, List.of(V1, V2),
				N2, List.of());

		assertThat(headers, equalTo(expected));
	}

	@Test
	void shouldNotAddAnythingIfHeaderNameIsNull() {
		var headersToAdd = new HashMap<String, List<String>>() {
			@Serial
			private static final long serialVersionUID = 1L;
			{
				put(null, List.of());
			}
		};

		Headers.addTo(headers, headersToAdd);

		var expected = Map.of(
				N1, List.of(V1, V2));

		assertThat(headers, equalTo(expected));
	}

	@Test
	void shouldReturnTrueWhenCheckingWithEmptyStringHeaderValueOnContains() {
		boolean result = Headers.contains(N1, "", headers);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueWhenCheckingExistingStringHeaderValueOnContains() {
		boolean result = Headers.contains(N1, V1, headers);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseWhenCheckingNonExistingStringHeaderValueOnContains() {
		boolean result = Headers.contains(N1, "non-existing", headers);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseWhenCheckingExistingStringHeaderValueInEmptyHeadersOnContains() {
		boolean result = Headers.contains(N1, V1, Collections.emptyMap());

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseWhenCheckingExistingHeaderInEmptyHeadersOnContains() {
		boolean result = Headers.contains(N1, Collections.emptyMap());

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueWhenCheckingExistingHeaderInHeadersOnContains() {
		boolean result = Headers.contains(N1, headers);

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueWhenCheckingExistingHeaderInHeadersIgnoringCaseOnContains() {
		boolean result = Headers.contains(N1.toUpperCase(), headers);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseWhenCheckingNonExistingHeaderInHeadersOnContains() {
		boolean result = Headers.contains("non-existing", headers);

		assertFalse(result);
	}

	@Test
	void shouldReturnHeaderWhenCaseDoesNotMatch() {
		Map<String, List<String>> mapHeaders = Map.of("some-header", List.of(HEADER_VALUE));

		List<String> headerValues = Headers.get("Some-Header", mapHeaders);

		assertThat(headerValues, hasSize(1));
		assertThat(headerValues.getFirst(), equalTo(HEADER_VALUE));
	}

	@Test
	void shouldReturnEmptyListOnGetFromEmptyHeadersMap() {
		List<String> headerValues = Headers.get("Some-Header", Map.of());

		assertThat(headerValues, notNullValue());
		assertThat(headerValues, hasSize(0));
	}

	@Test
	void shouldCreateHeadersMapWithAllHeaders() {
		Map<String, List<String>> resultHeaders = Headers.of(
				HeaderFunction.header(N1, V1),
				HeaderFunction.header(N2, V2));

		Map<String, List<String>> expected = new HashMap<>();
		expected.put(N1, List.of(V1));
		expected.put(N2, List.of(V2));

		assertThat(resultHeaders, equalTo(expected));
	}

	@Test
	void shouldCreateHeadersMapWithNoHeaders() {
		Map<String, List<String>> resultHeaders = Headers.of();

		assertThat(resultHeaders, equalTo(Collections.emptyMap()));
	}

	@Test
	void shouldCreateHeadersMapWithNullHeaders() {
		Map<String, List<String>> resultHeaders = Headers.of((HeaderFunction[]) null);

		assertThat(resultHeaders, equalTo(Collections.emptyMap()));
	}

	@Test
	void shouldCreateImmutableHeadersMapWithAllHeaders() {
		Map<String, List<String>> resultHeaders = Headers.of(
				HeaderFunction.header(N1, V1),
				HeaderFunction.header(N2, V2));

		assertThrows(UnsupportedOperationException.class, () -> {
			resultHeaders.put("new-header", List.of("new-value"));
		});
	}

	@Test
	void shouldCreateImmutableHeadersMapWithNoHeaders() {
		Map<String, List<String>> resultHeaders = Headers.of();

		assertThrows(UnsupportedOperationException.class, () -> {
			resultHeaders.put("new-header", List.of("new-value"));
		});
	}
}
