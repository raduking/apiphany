package org.apiphany;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

	private static final String V1 = "v1";
	private static final String V2 = "v2";

	Map<String, List<String>> headers = new HashMap<>() {
		private static final long serialVersionUID = 1L;
		{
			put(N1, new ArrayList<>() {
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
	void shouldReturnFalseOnHasBodyWhenTheApiMessageDoesntHaveABody() {
		ApiMessage<String> message = new ApiMessage<>();

		assertFalse(message.hasBody());
	}

	@Test
	void shouldReturnFalseOnHasNoBodyWhenTheApiMessageHasABody() {
		ApiMessage<String> message = new ApiMessage<>(DUMMY_BODY, Collections.emptyMap());

		assertFalse(message.hasNoBody());
	}

	@Test
	void shouldReturnTrueOnHasNoBodyWhenTheApiMessageDoesntHaveABody() {
		ApiMessage<String> message = new ApiMessage<>();

		assertTrue(message.hasNoBody());
	}
}
