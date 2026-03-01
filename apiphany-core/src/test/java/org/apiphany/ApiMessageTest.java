package org.apiphany;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ApiMessage}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiMessageTest {

	private static final String DUMMY_BODY = "dummyBody";

	private static final String N1 = "n1";
	private static final String N2 = "n2";

	private static final String V1 = "v1";
	private static final String V2 = "v2";
	private static final String V3 = "v3";
	private static final String V4 = "v4";

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
	void shouldReturnTrueWhenCheckingWithEmptyStringHeaderValueOnContains() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, headers);
		boolean result = message.containsHeader(N1, "");

		assertTrue(result);
	}

	@Test
	void shouldReturnTrueWhenCheckingExistingStringHeaderValueOnContains() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, headers);
		boolean result = message.containsHeader(N1, V1);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseWhenCheckingPartialExistingStringHeaderValueOnContains() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, headers);
		boolean result = message.containsHeader(N1, V1.substring(0, 1));

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseWhenCheckingNonExistingStringHeaderValueOnContains() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, headers);
		boolean result = message.containsHeader(N1, "non-existing");

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseWhenCheckingExistingStringHeaderValueInEmptyHeadersOnContains() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, Collections.emptyMap());
		boolean result = message.containsHeader(N1, V1);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseWhenCheckingExistingHeaderInEmptyHeadersOnContains() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, Collections.emptyMap());
		boolean result = message.containsHeader(N1);

		assertFalse(result);
	}

	@Test
	void shouldReturnTrueOnHasBodyWhenTheApiMessageHasABody() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, Collections.emptyMap());

		assertTrue(message.hasBody());
	}

	@Test
	void shouldReturnFalseOnHasBodyWhenTheApiMessageDoesNotHaveABody() {
		ApiMessage<String> message = new ApiMessage<>();

		assertFalse(message.hasBody());
	}

	@Test
	void shouldReturnFalseOnHasNoBodyWhenTheApiMessageHasABody() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, Collections.emptyMap());

		assertFalse(message.hasNoBody());
	}

	@Test
	void shouldReturnTrueOnHasNoBodyWhenTheApiMessageDoesNotHaveABody() {
		ApiMessage<String> message = new ApiMessage<>();

		assertTrue(message.hasNoBody());
	}

	@Test
	void shouldAddHeadersToExistingHeaders() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, headers);

		var headersToAdd = Map.of(N2, List.of(V3, V4));

		message.addHeaders(headersToAdd);

		var expected = Map.of(
				N1, List.of(V1, V2),
				N2, List.of(V3, V4));

		assertThat(message.getHeaders(), equalTo(expected));
	}

	@Test
	void shouldAddHeaderToExistingHeaders() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, headers);

		message.addHeader(N2, List.of(V3, V4));

		var expected = Map.of(
				N1, List.of(V1, V2),
				N2, List.of(V3, V4));

		assertThat(message.getHeaders(), equalTo(expected));
	}

	@Test
	void shouldAddSingleValueHeaderToExistingHeaders() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, headers);

		message.addHeader(N2, V3);

		var expected = Map.of(
				N1, List.of(V1, V2),
				N2, List.of(V3));

		assertThat(message.getHeaders(), equalTo(expected));
	}
}
