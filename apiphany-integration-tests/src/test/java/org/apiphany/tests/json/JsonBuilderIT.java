package org.apiphany.tests.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.lang.annotation.AsValue;
import org.apiphany.lang.annotation.Creator;
import org.apiphany.lang.annotation.FieldName;
import org.apiphany.lang.annotation.FieldOrder;
import org.apiphany.lang.annotation.Ignored;
import org.apiphany.security.Sensitive;
import org.apiphany.tests.json.JsonBuilderIT.GenericTypeTests.GenericDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Integration tests for {@link JsonBuilder}.
 * <p>
 * These tests must pass for every JSON library implementation, as they verify the basic contract of transforming
 * objects to JSON and back.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonBuilderIT {

	private static final String ID = "id1";
	private static final String NAME = "name1";

	static class SimpleDTO {

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

	@Nested
	class BasicTests {

		@Test
		void shouldTransformObjectToJsonAndReadItBack() {
			SimpleDTO a1 = new SimpleDTO(ID, NAME);

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

			SimpleDTO a2 = JsonBuilder.fromJson(json1, SimpleDTO.class);

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

			assertThat(json1, equalTo(json2));
		}

		@Test
		void shouldTransformGenericObjectToJsonAndReadItBack() {
			List<SimpleDTO> list1 = List.of(new SimpleDTO(ID, NAME));

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(list1));

			List<SimpleDTO> list2 = JsonBuilder.fromJson(json1, new GenericClass<>() {
				// empty
			});

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(list2));

			assertThat(json1, equalTo(json2));
		}
	}

	@Nested
	class GenericTypeTests {

		static class GenericDTO<T> {

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

		@Test
		void shouldTransformGenericObjectWithGenericFieldToJsonAndReadItBack() {
			GenericDTO<SimpleDTO> a1 = new GenericDTO<>(new SimpleDTO(ID, NAME));

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

			GenericDTO<SimpleDTO> a2 = JsonBuilder.fromJson(json1, new GenericClass<>() {
				// empty
			});

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

			assertThat(json1, equalTo(json2));
		}

		@Test
		void shouldTransformGenericObjectWithGenericFieldOfGenericTypeToJsonAndReadItBack() {
			GenericDTO<List<SimpleDTO>> a1 = new GenericDTO<>(List.of(new SimpleDTO(ID, NAME)));

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

			GenericDTO<List<SimpleDTO>> a2 = JsonBuilder.fromJson(json1, new GenericClass<>() {
				// empty
			});

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

			assertThat(json1, equalTo(json2));
		}
	}

	@Nested
	class ApiphanyAnnotationTests {

		static class ApiphanyDTO {

			String id;

			@Sensitive
			String password;

			public ApiphanyDTO() {
				// empty
			}

			public ApiphanyDTO(final String id, final String password) {
				this.id = id;
				this.password = password;
			}

			public String getId() {
				return id;
			}

			public void setId(final String id) {
				this.id = id;
			}

			public String getPassword() {
				return password;
			}

			public void setPassword(final String password) {
				this.password = password;
			}
		}

		@Test
		void shouldNotIncludeSensitiveFieldsInJson() {
			ApiphanyDTO dto = new ApiphanyDTO(ID, "p123");

			String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

			assertThat(json, equalTo("{\"id\":\"id1\"}"));
		}

		static class NoDefaultConstructorDTO {

			private String id;

			@Creator
			public NoDefaultConstructorDTO(final @FieldName("id") String id) {
				this.id = id;
			}

			public String getId() {
				return id;
			}

			public void setId(final String id) {
				this.id = id;
			}
		}

		@Test
		void shouldTransformObjectWithoutDefaultConstructorToJsonAndReadItBack() {
			NoDefaultConstructorDTO a1 = new NoDefaultConstructorDTO(ID);

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

			NoDefaultConstructorDTO a2 = JsonBuilder.fromJson(json1, NoDefaultConstructorDTO.class);

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

			assertThat(json1, equalTo(json2));
		}

		static class CustomFieldNameDTO {

			@FieldName("custom_id")
			private String id;

			public CustomFieldNameDTO() {
				// empty
			}

			public CustomFieldNameDTO(final String id) {
				this.id = id;
			}

			public String getId() {
				return id;
			}

			public void setId(final String id) {
				this.id = id;
			}
		}

		@Test
		void shouldUseCustomFieldNameInJson() {
			CustomFieldNameDTO dto = new CustomFieldNameDTO(ID);

			String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

			assertThat(json, equalTo("{\"custom_id\":\"id1\"}"));
		}

		static class CustomCreatorDTO {

			private String id;

			@Creator
			public CustomCreatorDTO(final @FieldName("id") String id) {
				this.id = id;
			}

			public String getId() {
				return id;
			}

			public void setId(final String id) {
				this.id = id;
			}
		}

		@Test
		void shouldUseCustomCreatorToInstantiateObject() {
			CustomCreatorDTO a1 = new CustomCreatorDTO(ID);

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

			CustomCreatorDTO a2 = JsonBuilder.fromJson(json1, CustomCreatorDTO.class);

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

			assertThat(json1, equalTo(json2));
		}

		static class IgnoredFieldDTO {

			private String id;

			@Ignored
			private String ignoredField;

			public IgnoredFieldDTO() {
				// empty
			}

			public IgnoredFieldDTO(final String id, final String ignoredField) {
				this.id = id;
				this.ignoredField = ignoredField;
			}

			public String getId() {
				return id;
			}

			public void setId(final String id) {
				this.id = id;
			}

			public String getIgnoredField() {
				return ignoredField;
			}

			public void setIgnoredField(final String ignoredField) {
				this.ignoredField = ignoredField;
			}
		}

		@Test
		void shouldNotIncludeIgnoredFieldsInJson() {
			IgnoredFieldDTO dto = new IgnoredFieldDTO(ID, "ignored");

			String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

			assertThat(json, equalTo("{\"id\":\"id1\"}"));
		}

		enum CustomEnum {

			VALUE1,
			VALUE2;

			@AsValue
			@Override
			public String toString() {
				return this == VALUE1 ? "custom_value" : "VALUE2";
			}
		}

		@Test
		void shouldUseCustomFieldNameForEnumValue() {
			CustomEnum e = CustomEnum.VALUE1;

			String json = Strings.removeAllWhitespace(JsonBuilder.toJson(e));

			assertThat(json, equalTo("\"custom_value\""));
		}

		@Test
		void shouldUseCustomToStringForEnumValue() {
			CustomEnum e = CustomEnum.VALUE2;

			String json = Strings.removeAllWhitespace(JsonBuilder.toJson(e));

			assertThat(json, equalTo("\"VALUE2\""));
		}

		static class EnumDTO {

			private CustomEnum value;

			public EnumDTO() {
				// empty
			}

			public EnumDTO(final CustomEnum value) {
				this.value = value;
			}

			public CustomEnum getValue() {
				return value;
			}

			public void setValue(final CustomEnum value) {
				this.value = value;
			}
		}

		@Test
		void shouldUseCustomToStringForEnumValueWhenNestedInObject() {
			EnumDTO dto = new EnumDTO(CustomEnum.VALUE1);

			String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

			assertThat(json, equalTo("{\"value\":\"custom_value\"}"));
		}

		@Test
		void shouldUseCustomToStringForEnumValueWhenNestedInObjectWithGenericType() {
			GenericDTO<CustomEnum> dto = new GenericDTO<>(CustomEnum.VALUE2);

			String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

			assertThat(json, equalTo("{\"value\":\"VALUE2\"}"));
		}

		@FieldOrder({ "second", "first" })
		static class FieldOrderDTO {

			private String first;

			private String second;

			public FieldOrderDTO() {
				// empty
			}

			public FieldOrderDTO(final String first, final String second) {
				this.first = first;
				this.second = second;
			}

			public String getFirst() {
				return first;
			}

			public void setFirst(final String first) {
				this.first = first;
			}

			public String getSecond() {
				return second;
			}
		}

		@Test
		void shouldUseCustomFieldOrderInJson() {
			FieldOrderDTO dto = new FieldOrderDTO("firstValue", "secondValue");

			String json = Strings.removeAllWhitespace(JsonBuilder.toJson(dto));

			assertThat(json, equalTo("{\"second\":\"secondValue\",\"first\":\"firstValue\"}"));
		}
	}

	@Nested
	class Java8TimeTests {

		static class Java8TimeDTO {

			private LocalDate date;

			public Java8TimeDTO() {
				// empty
			}

			public Java8TimeDTO(final LocalDate date) {
				this.date = date;
			}

			public java.time.LocalDate getDate() {
				return date;
			}

			public void setDate(final LocalDate date) {
				this.date = date;
			}
		}

		@Test
		void shouldTransformObjectWithJava8TimeToJsonAndReadItBack() {
			Java8TimeDTO a1 = new Java8TimeDTO(LocalDate.of(2026, 2, 6));

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

			Java8TimeDTO a2 = JsonBuilder.fromJson(json1, Java8TimeDTO.class);

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

			assertThat(json1, equalTo(json2));
			assertThat(json1, equalTo("{\"date\":\"2026-02-06\"}"));
		}

		static class LocalDateTimeDTO {

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

		@Test
		void shouldTransformObjectWithLocalDateTimeToJsonAndReadItBack() {
			LocalDateTimeDTO a1 = new LocalDateTimeDTO(LocalDateTime.of(2026, 2, 6, 12, 30, 11));

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

			LocalDateTimeDTO a2 = JsonBuilder.fromJson(json1, LocalDateTimeDTO.class);

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

			assertThat(json1, equalTo(json2));
			assertThat(json1, equalTo("{\"dateTime\":\"2026-02-06T12:30:11\"}"));
		}
	}
}
