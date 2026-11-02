package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import okhttp3.Headers;

/**
 * Test class for {@link OkHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class OkHttpExchangeClientTest {

	@Test
	void shouldConvertHeadersToAnInsertionOrderedMap() {
		Headers headers = new Headers.Builder()
				.add("Z-Header", "value1")
				.add("A-Header", "value2")
				.add("z-header", "value3")
				.build();

		Map<String, List<String>> result = OkHttpExchangeClient.toHttpHeadersMap(headers);

		assertThat(new ArrayList<>(result.keySet()), equalTo(List.of("z-header", "a-header")));
		assertThat(result.get("z-header"), equalTo(List.of("value1", "value3")));
		assertThat(result.get("a-header"), equalTo(List.of("value2")));
	}
}
