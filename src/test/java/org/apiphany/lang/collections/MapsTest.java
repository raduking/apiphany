package org.apiphany.lang.collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Maps}.
 *
 * @author Radu Sebastian LAZIN
 */
class MapsTest {

	private static final String KEY = "key";
	private static final String VALUE = "value";

	@Test
	void shouldReturnEmptyMapIfMapIsNullOnSafe() {
		var result = Maps.safe(null);

		assertThat(result, equalTo(Collections.emptyMap()));
	}

	@Test
	void shouldReturnTheMapIfMapIsNotNullOnSafe() {
		var map = Map.of();

		var result = Maps.safe(map);

		assertThat(result, equalTo(map));
	}

	@Test
	void shouldReturnKeyIfMapHasIt() {
		var map = Map.of(KEY, VALUE);

		Object value = Maps.getOrDefault(map, KEY, v -> v, (String) null);

		assertThat(value, equalTo(VALUE));
	}

	@Test
	void shouldReturnDefaultValueIfMapDoesntHaveIt() {
		var map = Map.of(KEY, VALUE);

		Object value = Maps.getOrDefault(map, VALUE, v -> v, KEY);

		assertThat(value, equalTo(KEY));
	}

	@Test
	void shouldReturnAMultiValueMapFromAMap() {
		Map<String, String> map = Map.of(KEY, VALUE);

		Map<String, List<String>> multiMap = Maps.multiValueMap(map);

		assertThat(multiMap.get(KEY), equalTo(List.of(VALUE)));
	}

	@Test
	void shouldReturnAMutableMultiValueMapFromNullMap() {
		Map<String, List<String>> multiMap = Maps.multiValueMap(null);

		assertThat(multiMap.entrySet(), hasSize(0));
		assertTrue(Maps.isEmpty(multiMap));

		assertDoesNotThrow(() -> multiMap.put(KEY, List.of(VALUE)));
		assertTrue(Maps.isNotEmpty(multiMap));
	}

	@Test
	void shouldReturnTrueOnIsEmptyForNullMap() {
		assertTrue(Maps.isEmpty(null));
	}

	@Test
	void shouldReturnFalseOnIsNotEmptyForNullMap() {
		assertFalse(Maps.isNotEmpty(null));
	}
}
