package org.apiphany.tests.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.sql.Date;
import java.time.Month;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract tests for the serialization and deserialization of JDK types that must behave identically for every JSON
 * library implementation: {@code java.time.Year}, {@code java.time.Month}, {@code java.sql.Date},
 * {@code java.util.Locale} and {@code java.util.Optional} (including its primitive variants).
 * <p>
 * The {@code java.time} values are serialized as ISO strings, {@link Month} and {@link Locale} use their names/language
 * tags, {@link Date} uses a date-only ISO string and {@code Optional} values are serialized as the contained value or a
 * JSON {@code null} when empty.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JsonBuilderJdkTypesContract extends JsonBuilderContract {

	/**
	 * Transfer object holding optional values.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class OptionalDTO {

		private Optional<String> value;

		private OptionalInt count;

		private OptionalLong total;

		private OptionalDouble ratio;

		public Optional<String> getValue() {
			return value;
		}

		public void setValue(final Optional<String> value) {
			this.value = value;
		}

		public OptionalInt getCount() {
			return count;
		}

		public void setCount(final OptionalInt count) {
			this.count = count;
		}

		public OptionalLong getTotal() {
			return total;
		}

		public void setTotal(final OptionalLong total) {
			this.total = total;
		}

		public OptionalDouble getRatio() {
			return ratio;
		}

		public void setRatio(final OptionalDouble ratio) {
			this.ratio = ratio;
		}
	}

	@DisplayName("JSON: java.time.Year is serialized as an ISO string and read back")
	@Test
	default void shouldTransformYearToJsonAndReadItBack() {
		Year year = Year.of(2026);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(year));

		assertThat(json, equalTo("\"2026\""));
		assertThat(JsonBuilder.fromJson(json, Year.class), equalTo(year));
	}

	@DisplayName("JSON: java.time.Month is serialized by name and read back")
	@Test
	default void shouldTransformMonthToJsonAndReadItBack() {
		Month month = Month.FEBRUARY;

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(month));

		assertThat(json, equalTo("\"FEBRUARY\""));
		assertThat(JsonBuilder.fromJson(json, Month.class), equalTo(month));
	}

	@DisplayName("JSON: java.time.Month is read back from its name ignoring the case and from its 1-based number")
	@Test
	default void shouldDeserializeMonthFromNameIgnoringCaseAndFromNumber() {
		assertThat(JsonBuilder.fromJson("\"FEBRUARY\"", Month.class), equalTo(Month.FEBRUARY));
		assertThat(JsonBuilder.fromJson("\"february\"", Month.class), equalTo(Month.FEBRUARY));
		assertThat(JsonBuilder.fromJson("2", Month.class), equalTo(Month.FEBRUARY));
		assertThat(JsonBuilder.fromJson("\"2\"", Month.class), equalTo(Month.FEBRUARY));
		assertThat(JsonBuilder.fromJson("null", Month.class), nullValue());
	}

	@DisplayName("JSON: java.sql.Date is serialized as a date-only ISO string and read back")
	@Test
	default void shouldTransformSqlDateToJsonAndReadItBack() {
		Date date = new Date(1704157445000L);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(date));

		assertThat(json, equalTo("\"" + date + "\""));
		assertThat(JsonBuilder.fromJson(json, Date.class).toString(), equalTo(date.toString()));
	}

	@DisplayName("JSON: java.util.Locale is serialized as a BCP-47 language tag and read back")
	@Test
	default void shouldTransformLocaleToJsonAndReadItBack() {
		Locale locale = Locale.forLanguageTag("en-US");

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(locale));

		assertThat(json, equalTo("\"en-US\""));
		assertThat(JsonBuilder.fromJson(json, Locale.class), equalTo(locale));
	}

	@DisplayName("JSON: java.util.Optional values are serialized as the contained value or null and read back")
	@Test
	default void shouldTransformOptionalToJsonAndReadItBack() {
		OptionalDTO present = new OptionalDTO();
		present.setValue(Optional.of("x"));
		present.setCount(OptionalInt.of(7));
		present.setTotal(OptionalLong.of(8L));
		present.setRatio(OptionalDouble.of(9.5));

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(present));

		assertThat(json, equalTo("{\"value\":\"x\",\"count\":7,\"total\":8,\"ratio\":9.5}"));

		OptionalDTO read = JsonBuilder.fromJson(json, OptionalDTO.class);

		assertThat(read.getValue(), equalTo(Optional.of("x")));
		assertThat(read.getCount(), equalTo(OptionalInt.of(7)));
		assertThat(read.getTotal(), equalTo(OptionalLong.of(8L)));
		assertThat(read.getRatio(), equalTo(OptionalDouble.of(9.5)));
	}

	@DisplayName("JSON: empty java.util.Optional values are serialized as null and read back as empty")
	@Test
	default void shouldTransformEmptyOptionalToJsonAndReadItBack() {
		OptionalDTO empty = new OptionalDTO();
		empty.setValue(Optional.empty());
		empty.setCount(OptionalInt.empty());
		empty.setTotal(OptionalLong.empty());
		empty.setRatio(OptionalDouble.empty());

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(empty));

		assertThat(json, equalTo("{\"value\":null,\"count\":null,\"total\":null,\"ratio\":null}"));

		OptionalDTO read = JsonBuilder.fromJson(json, OptionalDTO.class);

		assertThat(read.getValue(), equalTo(Optional.empty()));
		assertThat(read.getCount(), equalTo(OptionalInt.empty()));
		assertThat(read.getTotal(), equalTo(OptionalLong.empty()));
		assertThat(read.getRatio(), equalTo(OptionalDouble.empty()));
	}

	@DisplayName("JSON: java.util.Locale map keys are serialized as BCP-47 language tags")
	@Test
	default void shouldSerializeLocaleMapKeysToJson() {
		Map<Locale, String> map = new LinkedHashMap<>();
		map.put(Locale.forLanguageTag("en-US"), "value");

		String json = JsonBuilder.toJson(map);

		assertThat(Strings.removeAllWhitespace(json), equalTo("{\"en-US\":\"value\"}"));
	}

	@DisplayName("JSON: java.sql.Date map keys are serialized as date-only ISO strings")
	@Test
	default void shouldSerializeSqlDateMapKeysToJson() {
		Date date = new Date(1704157445000L);
		Map<Date, String> map = new LinkedHashMap<>();
		map.put(date, "value");

		String json = JsonBuilder.toJson(map);

		assertThat(Strings.removeAllWhitespace(json), equalTo("{\"" + date + "\":\"value\"}"));
	}
}
