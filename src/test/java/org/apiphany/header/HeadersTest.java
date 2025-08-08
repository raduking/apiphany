package org.apiphany.header;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
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

	Map<String, List<String>> headers = new HashMap<>() {{
		put(N1, new ArrayList<>() {{
			add(V1);
			add(V2);
		}});
	}};

	@Test
	void shouldAddHeadersToExistingHeaders() {
		var headersToAdd = Map.of(N2, List.of(V3, V4));

		Headers.addTo(headers, headersToAdd);

		var expected = Map.of(
				N1, List.of(V1, V2),
				N2, List.of(V3, V4)
		);

		assertThat(headers, equalTo(expected));
	}

	@Test
	void shouldNotAddNullHeader() {
		var headersToAdd = Map.of(N2, new ArrayList<>() {{
			add(null);
			add(null);
		}});

		Headers.addTo(headers, headersToAdd);

		var expected = Map.of(
				N1, List.of(V1, V2),
				N2, List.of()
		);

		assertThat(headers, equalTo(expected));
	}

	@Test
	void shouldNotAddAnythingIfHeaderNameIsNull() {
		var headersToAdd = new HashMap<String, List<String>>() {{
			put(null, List.of());
		}};

		Headers.addTo(headers, headersToAdd);

		var expected = Map.of(
				N1, List.of(V1, V2)
		);

		assertThat(headers, equalTo(expected));
	}

}
