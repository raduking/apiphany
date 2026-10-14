package org.apiphany.tests.json.contract;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Contract tests for the {@code java.util.Locale} serialization and deserialization that must behave identically for
 * every JSON library implementation.
 * <p>
 * A locale is serialized as a BCP-47 language tag both on its own and when used as a list element, a map value or a map
 * key.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JsonBuilderLocaleContract extends JsonBuilderContract {

	/**
	 * The locale used by the tests.
	 */
	Locale LOCALE = Locale.forLanguageTag("en-US");

	@DisplayName("JSON: java.util.Locale is serialized as a BCP-47 language tag and read back")
	@Test
	default void shouldTransformLocaleToJsonAndReadItBack() {
		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(LOCALE));

		assertThat(json, equalTo("\"en-US\""));
		assertThat(JsonBuilder.fromJson(json, Locale.class), equalTo(LOCALE));
	}

	@DisplayName("JSON: java.util.Locale map keys are serialized as BCP-47 language tags")
	@Test
	default void shouldSerializeLocaleMapKeysToJson() {
		Map<Locale, String> map = new LinkedHashMap<>();
		map.put(LOCALE, "value");

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(map)), equalTo("{\"en-US\":\"value\"}"));
	}

	@DisplayName("JSON: java.util.Locale is serialized as a list element and read back")
	@Test
	default void shouldTransformLocaleInListsToJsonAndReadItBack() {
		List<Locale> locales = List.of(LOCALE);

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(locales)), equalTo("[\"en-US\"]"));

		List<Locale> read = JsonBuilder.fromJson("[\"en-US\"]", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(locales));
	}

	@DisplayName("JSON: java.util.Locale is serialized as a map value and read back")
	@Test
	default void shouldTransformLocaleInMapValuesToJsonAndReadItBack() {
		Map<String, Locale> locales = new LinkedHashMap<>();
		locales.put("value", LOCALE);

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(locales)), equalTo("{\"value\":\"en-US\"}"));

		Map<String, Locale> read = JsonBuilder.fromJson("{\"value\":\"en-US\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(locales));
	}

	@DisplayName("JSON: java.util.Locale is serialized as a map key and read back")
	@Test
	default void shouldTransformLocaleInMapKeysToJsonAndReadItBack() {
		Map<Locale, String> locales = new LinkedHashMap<>();
		locales.put(LOCALE, "value");

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(locales)), equalTo("{\"en-US\":\"value\"}"));

		Map<Locale, String> read = JsonBuilder.fromJson("{\"en-US\":\"value\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(locales));
	}

	@DisplayName("JSON: java.util.Locale is serialized as map keys and values together and read back")
	@Test
	default void shouldTransformLocaleInMapKeysAndValuesToJsonAndReadItBack() {
		Map<Locale, Locale> locales = new LinkedHashMap<>();
		locales.put(LOCALE, Locale.forLanguageTag("en-GB"));

		assertThat(Strings.removeAllWhitespace(JsonBuilder.toJson(locales)), equalTo("{\"en-US\":\"en-GB\"}"));

		Map<Locale, Locale> read = JsonBuilder.fromJson("{\"en-US\":\"en-GB\"}", new GenericClass<>() {
			// empty
		});

		assertThat(read, equalTo(locales));
	}
}
