package org.apiphany.tests.json.contract;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Contract tests for the {@code java.time.Month} serialization and deserialization that must behave identically for
 * every JSON library implementation.
 * <p>
 * A month is serialized by its name and can be read back from its name (ignoring the case) or from its 1-based number,
 * both on its own and when used as a list element, a map value or a map key.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JsonBuilderMonthContract extends JsonBuilderContract {

	/**
	 * The month used by the tests.
	 */
	Month MONTH = Month.FEBRUARY;

	@DisplayName("JSON: java.time.Month is serialized by name and read back")
	@Test
	default void shouldTransformMonthToJsonAndReadItBack() {
		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(MONTH));

		assertThat(json, equalTo("\"FEBRUARY\""));
		assertThat(JsonBuilder.fromJson(json, Month.class), equalTo(MONTH));
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

	@DisplayName("JSON: java.time.Month is serialized as a list element and read back")
	@Test
	default void shouldTransformMonthInListsToJsonAndReadItBack() {
		List<Month> months = List.of(MONTH);

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(months)), equalTo("[\"FEBRUARY\"]"));

		List<Month> read = JsonBuilder.fromJson("[\"FEBRUARY\"]", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(months));
	}

	@DisplayName("JSON: java.time.Month is serialized as a map value and read back")
	@Test
	default void shouldTransformMonthInMapValuesToJsonAndReadItBack() {
		Map<String, Month> months = new LinkedHashMap<>();
		months.put("value", MONTH);

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(months)), equalTo("{\"value\":\"FEBRUARY\"}"));

		Map<String, Month> read = JsonBuilder.fromJson("{\"value\":\"FEBRUARY\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(months));
	}

	@DisplayName("JSON: java.time.Month is serialized as a map key and read back")
	@Test
	default void shouldTransformMonthInMapKeysToJsonAndReadItBack() {
		Map<Month, String> months = new LinkedHashMap<>();
		months.put(MONTH, "value");

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(months)), equalTo("{\"FEBRUARY\":\"value\"}"));

		Map<Month, String> read = JsonBuilder.fromJson("{\"FEBRUARY\":\"value\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(months));
	}

	@DisplayName("JSON: java.time.Month is serialized as map keys and values together and read back")
	@Test
	default void shouldTransformMonthInMapKeysAndValuesToJsonAndReadItBack() {
		Map<Month, Month> months = new LinkedHashMap<>();
		months.put(MONTH, Month.MARCH);

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(months)), equalTo("{\"FEBRUARY\":\"MARCH\"}"));

		Map<Month, Month> read = JsonBuilder.fromJson("{\"FEBRUARY\":\"MARCH\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(months));
	}
}
