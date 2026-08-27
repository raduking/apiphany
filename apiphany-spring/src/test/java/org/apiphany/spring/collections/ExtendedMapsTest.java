package org.apiphany.spring.collections;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;

/**
 * Tests for {@link ExtendedMaps}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExtendedMapsTest {

	@Nested
	class EmptyMultiValueMapTests {

		@Test
		void shouldReturnEmptyMap() {
			MultiValueMap<String, String> map = ExtendedMaps.emptyMultiValueMap();

			assertThat(map, notNullValue());
			assertTrue(map.isEmpty());
		}

		@Test
		void shouldReturnSameInstanceOnMultipleCalls() {
			MultiValueMap<String, String> first = ExtendedMaps.emptyMultiValueMap();
			MultiValueMap<String, String> second = ExtendedMaps.emptyMultiValueMap();

			assertThat(first, equalTo(second));
		}
	}

	@Nested
	class MultiValueMapFromMapTests {

		@Test
		void shouldConvertMapToMultiValueMap() {
			Map<String, List<String>> source = Map.of(
					"key1", List.of("value1"),
					"key2", List.of("value2a", "value2b"));

			MultiValueMap<String, String> result = ExtendedMaps.multiValueMap(source);

			assertThat(result.get("key1"), equalTo(List.of("value1")));
			assertThat(result.get("key2"), equalTo(List.of("value2a", "value2b")));
		}

		@Test
		void shouldReturnEmptyMapForNullInput() {
			MultiValueMap<String, String> result = ExtendedMaps.multiValueMap((Map<String, List<String>>) null);

			assertTrue(result.isEmpty());
		}

		@Test
		void shouldReturnEmptyMapForEmptyInput() {
			MultiValueMap<String, String> result = ExtendedMaps.multiValueMap(Map.of());

			assertTrue(result.isEmpty());
		}
	}

	@Nested
	class MultiValueMapFromKeyValueTests {

		@Test
		void shouldCreateMultiValueMapWithSingleEntry() {
			MultiValueMap<String, String> result = ExtendedMaps.multiValueMap("key", "value");

			assertThat(result.get("key"), equalTo(List.of("value")));
		}
	}

	@Test
	void shouldPreventInstantiation() {
		assertDefaultConstructorThrows(ExtendedMaps.class);
	}
}
