package org.apiphany.tests.contract.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Contract tests for the {@code java.sql.Date} serialization and deserialization that must behave identically for every
 * JSON library implementation.
 * <p>
 * A {@code java.sql.Date} is serialized as a date-only ISO string both on its own and when used as a list element, a
 * map value or a map key. Round-tripped values are compared by their string form, because a deserialized date is
 * anchored at UTC midnight while a value created from epoch milliseconds is anchored at local midnight.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JsonBuilderSqlDateContract extends JsonBuilderContract {

	/**
	 * The date used by the tests.
	 */
	Date DATE = new Date(1704157445000L);

	@DisplayName("JSON: java.sql.Date is serialized as a date-only ISO string and read back")
	@Test
	default void shouldTransformSqlDateToJsonAndReadItBack() {
		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(DATE));

		assertThat(json, equalTo("\"" + DATE + "\""));
		assertThat(JsonBuilder.fromJson(json, Date.class).toString(), equalTo(DATE.toString()));
	}

	@DisplayName("JSON: java.sql.Date map keys are serialized as date-only ISO strings")
	@Test
	default void shouldSerializeSqlDateMapKeysToJson() {
		Map<Date, String> map = new LinkedHashMap<>();
		map.put(DATE, "value");

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(map)), equalTo("{\"" + DATE + "\":\"value\"}"));
	}

	@DisplayName("JSON: java.sql.Date is serialized as a list element and read back")
	@Test
	default void shouldTransformSqlDateInListsToJsonAndReadItBack() {
		List<Date> dates = List.of(DATE);

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(dates)), equalTo("[\"" + DATE + "\"]"));

		List<Date> read = JsonBuilder.fromJson("[\"" + DATE + "\"]", new GenericClass<>() {
			// empty
		});

		assertThat(read.size(), equalTo(1));
		assertThat(read.get(0).toString(), equalTo(DATE.toString()));
	}

	@DisplayName("JSON: java.sql.Date is serialized as a map value and read back")
	@Test
	default void shouldTransformSqlDateInMapValuesToJsonAndReadItBack() {
		Map<String, Date> dates = new LinkedHashMap<>();
		dates.put("value", DATE);

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(dates)), equalTo("{\"value\":\"" + DATE + "\"}"));

		Map<String, Date> read = JsonBuilder.fromJson("{\"value\":\"" + DATE + "\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read.get("value").toString(), equalTo(DATE.toString()));
	}

	@DisplayName("JSON: java.sql.Date is serialized as a map key and read back")
	@Test
	default void shouldTransformSqlDateInMapKeysToJsonAndReadItBack() {
		Map<Date, String> dates = new LinkedHashMap<>();
		dates.put(DATE, "value");

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(dates)), equalTo("{\"" + DATE + "\":\"value\"}"));

		Map<Date, String> read = JsonBuilder.fromJson("{\"" + DATE + "\":\"value\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read.size(), equalTo(1));
		assertThat(read.keySet().iterator().next().toString(), equalTo(DATE.toString()));
		assertThat(read.values().iterator().next(), equalTo("value"));
	}

	@DisplayName("JSON: java.sql.Date is serialized as map keys and values together and read back")
	@Test
	default void shouldTransformSqlDateInMapKeysAndValuesToJsonAndReadItBack() {
		Map<Date, Date> dates = new LinkedHashMap<>();
		dates.put(DATE, DATE);

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(dates)),
				equalTo("{\"" + DATE + "\":\"" + DATE + "\"}"));

		Map<Date, Date> read = JsonBuilder.fromJson("{\"" + DATE + "\":\"" + DATE + "\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read.size(), equalTo(1));
		Map.Entry<Date, Date> entry = read.entrySet().iterator().next();
		assertThat(entry.getKey().toString(), equalTo(DATE.toString()));
		assertThat(entry.getValue().toString(), equalTo(DATE.toString()));
	}
}
