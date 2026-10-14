package org.apiphany.tests.json.contract;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiMethod;
import org.apiphany.RequestMethod;
import org.apiphany.http.HttpMethod;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Contract tests for the {@link JsonBuilder} behavior that must be identical for every JSON library implementation.
 * <p>
 * This is the shared JSON contract: the tests are written once here and inherited by every JSON library integration
 * test module (Jackson 2, Jackson 3, ...), which makes cross-library parity trivial to verify.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JsonBuilderContract {

	/**
	 * Test identifier.
	 */
	String ID = "id1";

	/**
	 * Test name.
	 */
	String NAME = "name1";

	/**
	 * Test password.
	 */
	String PASSWORD = "p123";

	/**
	 * Simple transfer object used by the contract tests.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class SimpleDTO {

		private String id;

		private String name;

		public SimpleDTO() {
			// empty
		}

		public SimpleDTO(final String id, final String name) {
			this.id = id;
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}
	}

	/**
	 * Generic transfer object used by the contract tests.
	 *
	 * @param <T> type of the value
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class GenericDTO<T> {

		private T value;

		public GenericDTO() {
			// empty
		}

		public GenericDTO(final T value) {
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		public void setValue(final T value) {
			this.value = value;
		}
	}

	/**
	 * Plain enum without any annotation, used to verify the enum case insensitive deserialization contract.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	enum PlainEnum {

		ALPHA,
		BETA;
	}

	/**
	 * Enum whose {@code toString()} differs from the constant name, used to verify the enum JSON representation.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	enum LabeledEnum {

		YES,
		NO;

		@Override
		public String toString() {
			return this == YES ? "affirmative" : "negative";
		}
	}

	/**
	 * Transfer object with nullable fields.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class NullFieldsDTO {

		private String name;

		private String value;

		public NullFieldsDTO() {
			// empty
		}

		public NullFieldsDTO(final String name, final String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(final String value) {
			this.value = value;
		}
	}

	/**
	 * Transfer object holding a collection and a map with nullable content.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	class NullContentDTO {

		private List<String> list;

		private Map<String, String> map;

		public List<String> getList() {
			return list;
		}

		public void setList(final List<String> list) {
			this.list = list;
		}

		public Map<String, String> getMap() {
			return map;
		}

		public void setMap(final Map<String, String> map) {
			this.map = map;
		}
	}

	@DisplayName("JSON: an object is serialized and read back without losing information")
	@Test
	default void shouldTransformObjectToJsonAndReadItBack() {
		SimpleDTO a1 = new SimpleDTO(ID, NAME);

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

		SimpleDTO a2 = JsonBuilder.fromJson(json1, SimpleDTO.class);

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

		assertThat(json1, equalTo(json2));
	}

	@DisplayName("JSON: a generic list is serialized and read back without losing information")
	@Test
	default void shouldTransformGenericObjectToJsonAndReadItBack() {
		List<SimpleDTO> list1 = List.of(new SimpleDTO(ID, NAME));

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(list1));

		List<SimpleDTO> list2 = JsonBuilder.fromJson(json1, new GenericClass<>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(list2));

		assertThat(json1, equalTo(json2));
	}

	@DisplayName("JSON: a generic field is serialized and read back without losing information")
	@Test
	default void shouldTransformGenericObjectWithGenericFieldToJsonAndReadItBack() {
		GenericDTO<SimpleDTO> a1 = new GenericDTO<>(new SimpleDTO(ID, NAME));

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

		GenericDTO<SimpleDTO> a2 = JsonBuilder.fromJson(json1, new GenericClass<>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

		assertThat(json1, equalTo(json2));
	}

	@DisplayName("JSON: a nested generic field is serialized and read back without losing information")
	@Test
	default void shouldTransformGenericObjectWithGenericFieldOfGenericTypeToJsonAndReadItBack() {
		GenericDTO<List<SimpleDTO>> a1 = new GenericDTO<>(List.of(new SimpleDTO(ID, NAME)));

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

		GenericDTO<List<SimpleDTO>> a2 = JsonBuilder.fromJson(json1, new GenericClass<>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

		assertThat(json1, equalTo(json2));
	}

	@DisplayName("JSON: enums are deserialized ignoring the case of the JSON value")
	@Test
	default void shouldDeserializeEnumsCaseInsensitively() {
		PlainEnum lower = JsonBuilder.fromJson("\"alpha\"", PlainEnum.class);
		PlainEnum upper = JsonBuilder.fromJson("\"BETA\"", PlainEnum.class);

		assertThat(lower, equalTo(PlainEnum.ALPHA));
		assertThat(upper, equalTo(PlainEnum.BETA));
	}

	@DisplayName("JSON: enums are serialized and read with toString, including map keys")
	@Test
	default void shouldTransformEnumsUsingToString() {
		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(LabeledEnum.YES));

		assertThat(json, equalTo("\"affirmative\""));
		assertThat(JsonBuilder.fromJson("\"affirmative\"", LabeledEnum.class), equalTo(LabeledEnum.YES));
		assertThat(JsonBuilder.fromJson("\"AFFIRMATIVE\"", LabeledEnum.class), equalTo(LabeledEnum.YES));
		assertThat(JsonBuilder.fromJson("\"YES\"", LabeledEnum.class), nullValue());

		Map<LabeledEnum, String> map = new LinkedHashMap<>();
		map.put(LabeledEnum.YES, "value");
		String mapJson = Strings.removeAllWhitespace(JsonBuilder.toJson(map));

		assertThat(mapJson, equalTo("{\"affirmative\":\"value\"}"));

		Map<LabeledEnum, String> read = JsonBuilder.fromJson(mapJson, new GenericClass<>() {
			// empty
		});

		assertThat(read.get(LabeledEnum.YES), equalTo("value"));
	}

	@DisplayName("JSON: unknown fields are ignored on deserialization")
	@Test
	default void shouldIgnoreUnknownJsonFields() {
		SimpleDTO dto = JsonBuilder.fromJson("{\"id\":\"id1\",\"name\":\"name1\",\"unknown\":123}", SimpleDTO.class);

		assertThat(dto.getId(), equalTo(ID));
		assertThat(dto.getName(), equalTo(NAME));
	}

	@DisplayName("JSON: null fields are omitted on serialization")
	@Test
	default void shouldOmitNullFieldsOnSerialization() {
		NullFieldsDTO dto = new NullFieldsDTO(NAME, null);

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

		assertThat(json, equalTo("{\"name\":\"name1\"}"));
	}

	@DisplayName("JSON: null map values are omitted while null list elements are kept")
	@Test
	default void shouldSerializeNullContentConsistently() {
		List<String> list = new ArrayList<>();
		list.add(ID);
		list.add(null);

		NullContentDTO dto = new NullContentDTO();
		dto.setList(list);
		dto.setMap(Collections.singletonMap(NAME, null));

		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

		assertThat(json, equalTo("{\"list\":[\"id1\",null],\"map\":{}}"));
	}

	@DisplayName("JSON: RequestMethod values are serialized by name")
	@Test
	default void shouldSerializeRequestMethodByName() {
		String json = Strings.removeAllWhitespace(JsonBuilder.toJson(HttpMethod.GET));

		assertThat(json, equalTo("\"GET\""));
	}

	@DisplayName("JSON: RequestMethod values are deserialized by name ignoring the case")
	@Test
	default void shouldDeserializeRequestMethodByNameIgnoringCase() {
		RequestMethod method = JsonBuilder.fromJson("\"GET\"", RequestMethod.class);
		RequestMethod lower = JsonBuilder.fromJson("\"get\"", RequestMethod.class);
		RequestMethod custom = JsonBuilder.fromJson("\"CUSTOM\"", RequestMethod.class);

		assertThat(method, equalTo(HttpMethod.GET));
		assertThat(lower, equalTo(HttpMethod.GET));
		assertThat(custom, equalTo(ApiMethod.of("CUSTOM")));
	}

	@DisplayName("JSON: a JSON null RequestMethod is deserialized as null")
	@Test
	default void shouldDeserializeNullRequestMethodToNull() {
		RequestMethod method = JsonBuilder.fromJson("null", RequestMethod.class);

		assertThat(method, nullValue());
	}

	@DisplayName("JSON: a JSON empty RequestMethod is deserialized as an empty ApiMethod")
	@Test
	default void shouldDeserializeEmptyRequestMethodToEmptyApiMethod() {
		RequestMethod method = JsonBuilder.fromJson("\"\"", RequestMethod.class);

		assertThat(method, equalTo(ApiMethod.of("")));
	}

	@DisplayName("JSON: indentation can be toggled at runtime")
	@Test
	default void shouldToggleIndentationAtRuntime() {
		boolean initial = JsonBuilder.runtime().isIndentOutput();
		try {
			JsonBuilder.indentJsonOutput(false);
			String compact = JsonBuilder.toJson(new SimpleDTO(ID, NAME));

			JsonBuilder.indentJsonOutput(true);
			String indented = JsonBuilder.toJson(new SimpleDTO(ID, NAME));

			assertThat(indented, not(equalTo(compact)));
			assertThat(indented, containsString(Strings.EOL));
		} finally {
			JsonBuilder.indentJsonOutput(initial);
		}
	}
}
