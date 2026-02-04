package org.apiphany.tests.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link JsonBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonBuilderTest {

	static class TestDto {

		private final String name;
		private final int value;

		public TestDto(final String name, final int value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public int getValue() {
			return value;
		}
	}

	@Test
	void shouldCallToString() {
		TestDto dto = new TestDto("example", 42);

		String json = JsonBuilder.toJson(dto);

		String expectedJson = "{ \"hash\":\"" + dto.getClass().getName() + "@" + Integer.toHexString(dto.hashCode()) + "\" }";

		assertThat(json, equalTo(expectedJson));
	}
}
