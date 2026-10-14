package org.apiphany.tests.json.contract;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Contract tests for the {@code java.time.Year} serialization and deserialization that must behave identically for
 * every JSON library implementation.
 * <p>
 * A year is serialized as an ISO string both on its own and when used as a list element, a map value or a map key.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JsonBuilderYearContract extends JsonBuilderContract {

	/**
	 * The year used by the tests.
	 */
	Year YEAR = Year.of(2026);

	@DisplayName("JSON: java.time.Year is serialized as an ISO string and read back")
	@Test
	default void shouldTransformYearToJsonAndReadItBack() {
		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(YEAR));

		assertThat(json, equalTo("\"2026\""));
		assertThat(JsonBuilder.fromJson(json, Year.class), equalTo(YEAR));
	}

	@DisplayName("JSON: java.time.Year is serialized as a list element and read back")
	@Test
	default void shouldTransformYearInListsToJsonAndReadItBack() {
		List<Year> years = List.of(YEAR);

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(years)), equalTo("[\"2026\"]"));

		List<Year> read = JsonBuilder.fromJson("[\"2026\"]", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(years));
	}

	@DisplayName("JSON: java.time.Year is serialized as a map value and read back")
	@Test
	default void shouldTransformYearInMapValuesToJsonAndReadItBack() {
		Map<String, Year> years = new LinkedHashMap<>();
		years.put("value", YEAR);

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(years)), equalTo("{\"value\":\"2026\"}"));

		Map<String, Year> read = JsonBuilder.fromJson("{\"value\":\"2026\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(years));
	}

	@DisplayName("JSON: java.time.Year is serialized as a map key and read back")
	@Test
	default void shouldTransformYearInMapKeysToJsonAndReadItBack() {
		Map<Year, String> years = new LinkedHashMap<>();
		years.put(YEAR, "value");

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(years)), equalTo("{\"2026\":\"value\"}"));

		Map<Year, String> read = JsonBuilder.fromJson("{\"2026\":\"value\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(years));
	}

	@DisplayName("JSON: java.time.Year is serialized as map keys and values together and read back")
	@Test
	default void shouldTransformYearInMapKeysAndValuesToJsonAndReadItBack() {
		Map<Year, Year> years = new LinkedHashMap<>();
		years.put(YEAR, Year.of(2027));

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(years)), equalTo("{\"2026\":\"2027\"}"));

		Map<Year, Year> read = JsonBuilder.fromJson("{\"2026\":\"2027\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(years));
	}
}
