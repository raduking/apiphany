package org.apiphany.tests.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for the Java 8 date/time serialization and deserialization that must behave identically for every JSON
 * library implementation.
 * <p>
 * The {@code java.time} values are serialized as ISO strings, and the classic {@code java.util.Date}/
 * {@code java.util.Calendar} values use a numeric zero offset ({@code +00:00}).
 * <p>
 * The remaining JDK types ({@code java.time.Year}, {@code java.time.Month}, {@code java.sql.Date},
 * {@code java.util.Locale} and {@code java.util.Optional}) are covered by {@link JsonBuilderJdkTypesContract}.
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

	@DisplayName("JSON: Instant is serialized using the ISO format and read back")
	@Test
	default void shouldTransformInstantToJsonAndReadItBack() {
		Instant instant = Instant.parse("2026-02-06T12:30:11Z");

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(instant));

		assertThat(json, equalTo("\"2026-02-06T12:30:11Z\""));
		assertThat(JsonBuilder.fromJson(json, Instant.class), equalTo(instant));
	}

	@DisplayName("JSON: LocalTime is serialized using the ISO format and read back")
	@Test
	default void shouldTransformLocalTimeToJsonAndReadItBack() {
		LocalTime time = LocalTime.of(12, 30, 11);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(time));

		assertThat(json, equalTo("\"12:30:11\""));
		assertThat(JsonBuilder.fromJson(json, LocalTime.class), equalTo(time));
	}

	@DisplayName("JSON: OffsetTime is serialized using the ISO format and read back")
	@Test
	default void shouldTransformOffsetTimeToJsonAndReadItBack() {
		OffsetTime time = OffsetTime.of(12, 30, 11, 0, ZoneOffset.ofHours(2));

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(time));

		assertThat(json, equalTo("\"12:30:11+02:00\""));
		assertThat(JsonBuilder.fromJson(json, OffsetTime.class), equalTo(time));
	}

	@DisplayName("JSON: OffsetDateTime is serialized using the ISO format and read back preserving the instant")
	@Test
	default void shouldTransformOffsetDateTimeToJsonAndReadItBack() {
		OffsetDateTime dateTime = OffsetDateTime.of(2026, 2, 6, 12, 30, 11, 0, ZoneOffset.ofHours(2));

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dateTime));

		assertThat(json, equalTo("\"2026-02-06T12:30:11+02:00\""));

		// both libraries normalize the offset to UTC when reading back
		OffsetDateTime read = JsonBuilder.fromJson(json, OffsetDateTime.class);

		assertThat(read.toInstant(), equalTo(dateTime.toInstant()));
		assertThat(read.toString(), equalTo("2026-02-06T10:30:11Z"));
	}

	@DisplayName("JSON: ZonedDateTime is serialized using the ISO format and read back preserving the instant")
	@Test
	default void shouldTransformZonedDateTimeToJsonAndReadItBack() {
		ZonedDateTime dateTime = ZonedDateTime.of(2026, 2, 6, 12, 30, 11, 0, ZoneOffset.ofHours(2));

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dateTime));

		assertThat(json, equalTo("\"2026-02-06T12:30:11+02:00\""));

		// both libraries normalize the offset to UTC when reading back
		ZonedDateTime read = JsonBuilder.fromJson(json, ZonedDateTime.class);

		assertThat(read.toInstant(), equalTo(dateTime.toInstant()));
		assertThat(read.toString(), equalTo("2026-02-06T10:30:11Z"));
	}

	@DisplayName("JSON: YearMonth is serialized using the ISO format and read back")
	@Test
	default void shouldTransformYearMonthToJsonAndReadItBack() {
		YearMonth yearMonth = YearMonth.of(2026, 2);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(yearMonth));

		assertThat(json, equalTo("\"2026-02\""));
		assertThat(JsonBuilder.fromJson(json, YearMonth.class), equalTo(yearMonth));
	}

	@DisplayName("JSON: Duration is serialized using the ISO format and read back")
	@Test
	default void shouldTransformDurationToJsonAndReadItBack() {
		Duration duration = Duration.ofSeconds(90);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(duration));

		assertThat(json, equalTo("\"PT1M30S\""));
		assertThat(JsonBuilder.fromJson(json, Duration.class), equalTo(duration));
	}

	@DisplayName("JSON: ZonedDateTime is serialized with the offset, without the zone id, and read back preserving the instant")
	@Test
	default void shouldTransformZonedDateTimeWithRegionZoneIdToJsonAndReadItBack() {
		ZonedDateTime dateTime = ZonedDateTime.of(2026, 7, 2, 12, 30, 11, 0, ZoneId.of("Europe/Paris"));

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dateTime));

		// the zone id is not written, only the offset
		assertThat(json, equalTo("\"2026-07-02T12:30:11+02:00\""));

		ZonedDateTime read = JsonBuilder.fromJson(json, ZonedDateTime.class);

		assertThat(read.toInstant(), equalTo(dateTime.toInstant()));
	}

	@DisplayName("JSON: Instant keeps the nanosecond precision when serialized and read back")
	@Test
	default void shouldTransformInstantWithNanosecondsToJsonAndReadItBack() {
		Instant instant = Instant.parse("2026-02-06T12:30:11.123456789Z");

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(instant));

		assertThat(json, equalTo("\"2026-02-06T12:30:11.123456789Z\""));
		assertThat(JsonBuilder.fromJson(json, Instant.class), equalTo(instant));
	}

	@DisplayName("JSON: date map keys are serialized as ISO strings")
	@Test
	default void shouldSerializeDateMapKeysToJson() {
		Map<LocalDate, String> map = new LinkedHashMap<>();
		map.put(LocalDate.of(2026, 2, 6), "value");

		String json = JsonBuilder.toJson(map);

		assertThat(Strings.removeAllWhitespace(json), equalTo("{\"2026-02-06\":\"value\"}"));
	}

	@DisplayName("JSON: java.util.Date is serialized using the ISO format with a numeric zero offset and read back")
	@Test
	default void shouldTransformDateToJsonAndReadItBack() {
		Date date = new Date(1704157445000L);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(date));

		assertThat(json, equalTo("\"2024-01-02T01:04:05.000+00:00\""));
		assertThat(JsonBuilder.fromJson(json, Date.class), equalTo(date));
	}

	@DisplayName("JSON: java.util.Calendar is serialized using the ISO format with a numeric zero offset and read back")
	@Test
	default void shouldTransformCalendarToJsonAndReadItBack() {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.setTimeInMillis(1704157445000L);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(calendar));

		assertThat(json, equalTo("\"2024-01-02T01:04:05.000+00:00\""));
		assertThat(JsonBuilder.fromJson(json, Calendar.class).getTimeInMillis(), equalTo(calendar.getTimeInMillis()));
	}
}
