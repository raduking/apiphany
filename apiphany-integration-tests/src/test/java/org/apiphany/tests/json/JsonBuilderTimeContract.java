package org.apiphany.tests.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for the Java 8 date/time serialization and deserialization that must behave identically for every JSON
 * library implementation.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JsonBuilderTimeContract extends JsonBuilderContract {

	/**
	 * Transfer object holding a local date.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class Java8TimeDTO {

		private LocalDate date;

		public Java8TimeDTO() {
			// empty
		}

		public Java8TimeDTO(final LocalDate date) {
			this.date = date;
		}

		public LocalDate getDate() {
			return date;
		}

		public void setDate(final LocalDate date) {
			this.date = date;
		}
	}

	@DisplayName("JSON: LocalDate is serialized using the ISO format and read back")
	@Test
	default void shouldTransformObjectWithJava8TimeToJsonAndReadItBack() {
		Java8TimeDTO a1 = new Java8TimeDTO(LocalDate.of(2026, 2, 6));

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

		Java8TimeDTO a2 = JsonBuilder.fromJson(json1, Java8TimeDTO.class);

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

		assertThat(json1, equalTo(json2));
		assertThat(json1, equalTo("{\"date\":\"2026-02-06\"}"));
	}

	/**
	 * Transfer object holding a local date time.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class LocalDateTimeDTO {

		private LocalDateTime dateTime;

		public LocalDateTimeDTO() {
			// empty
		}

		public LocalDateTimeDTO(final LocalDateTime dateTime) {
			this.dateTime = dateTime;
		}

		public LocalDateTime getDateTime() {
			return dateTime;
		}

		public void setDateTime(final LocalDateTime dateTime) {
			this.dateTime = dateTime;
		}
	}

	@DisplayName("JSON: LocalDateTime is serialized using the ISO format and read back")
	@Test
	default void shouldTransformObjectWithLocalDateTimeToJsonAndReadItBack() {
		LocalDateTimeDTO a1 = new LocalDateTimeDTO(LocalDateTime.of(2026, 2, 6, 12, 30, 11));

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

		LocalDateTimeDTO a2 = JsonBuilder.fromJson(json1, LocalDateTimeDTO.class);

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

		assertThat(json1, equalTo(json2));
		assertThat(json1, equalTo("{\"dateTime\":\"2026-02-06T12:30:11\"}"));
	}
}
