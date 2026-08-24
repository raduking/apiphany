package org.apiphany.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link JsonObjects}.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonObjectsTest {

	@Nested
	class IsStructuredTests {

		@Test
		void shouldReturnFalseForCharSequence() {
			assertThat(JsonObjects.isStructured("string"), equalTo(false));
			assertThat(JsonObjects.isStructured(String.valueOf("value")), equalTo(false));
		}

		@Test
		void shouldReturnFalseForNumber() {
			assertThat(JsonObjects.isStructured(1), equalTo(false));
			assertThat(JsonObjects.isStructured(1L), equalTo(false));
			assertThat(JsonObjects.isStructured(1.0), equalTo(false));
			assertThat(JsonObjects.isStructured(1.0f), equalTo(false));
			assertThat(JsonObjects.isStructured((short) 1), equalTo(false));
			assertThat(JsonObjects.isStructured((byte) 1), equalTo(false));
		}

		@Test
		void shouldReturnFalseForBoolean() {
			assertThat(JsonObjects.isStructured(true), equalTo(false));
			assertThat(JsonObjects.isStructured(false), equalTo(false));
		}

		@Test
		void shouldReturnFalseForCharacter() {
			assertThat(JsonObjects.isStructured('c'), equalTo(false));
		}

		@Test
		void shouldReturnFalseForEnum() {
			assertThat(JsonObjects.isStructured(TestEnum.VALUE1), equalTo(false));
		}

		@Test
		void shouldReturnFalseForTemporalAccessor() {
			assertThat(JsonObjects.isStructured(LocalDate.now()), equalTo(false));
			assertThat(JsonObjects.isStructured(LocalDateTime.now()), equalTo(false));
		}

		@Test
		void shouldReturnFalseForDate() {
			assertThat(JsonObjects.isStructured(new Date()), equalTo(false));
		}

		@Test
		void shouldReturnFalseForCalendar() {
			assertThat(JsonObjects.isStructured(Calendar.getInstance()), equalTo(false));
		}

		@Test
		void shouldReturnFalseForUUID() {
			assertThat(JsonObjects.isStructured(UUID.randomUUID()), equalTo(false));
		}

		@Test
		void shouldReturnTrueForPojo() {
			assertThat(JsonObjects.isStructured(new SimplePojo()), equalTo(true));
		}

		@Test
		void shouldReturnTrueForMap() {
			assertThat(JsonObjects.isStructured(Map.of("key", "value")), equalTo(true));
		}

		@Test
		void shouldReturnTrueForList() {
			assertThat(JsonObjects.isStructured(List.of("a", "b")), equalTo(true));
		}

		@Test
		void shouldReturnTrueForHashMap() {
			assertThat(JsonObjects.isStructured(new HashMap<>()), equalTo(true));
		}

		@Test
		void shouldReturnTrueForArray() {
			assertThat(JsonObjects.isStructured(new String[] { "a", "b" }), equalTo(true));
		}
	}

	@Nested
	class InitializationTests {

		@Test
		void shouldThrowExceptionWhenTryingToInstantiate() {
			UnsupportedOperationException e = Assertions.assertDefaultConstructorThrows(JsonObjects.class);

			assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
		}
	}

	enum TestEnum {
		VALUE1,
		VALUE2
	}

	static class SimplePojo {
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(final String value) {
			this.value = value;
		}
	}
}
