package org.apiphany.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apiphany.lang.Strings;
import org.apiphany.lang.annotation.Creator;
import org.apiphany.lang.annotation.FieldName;
import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.lang.Holder;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.GenericClass;
import org.morphix.runtime.OptionalLibrary;

/**
 * Test class for {@link JsonBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonBuilderTest {

	private static final String CUSTOMER_ID1 = "cid1";
	private static final String TENANT_ID1 = "tid1";
	private static final String ERROR = "some error";
	private static final String ERROR_DESCRIPTION = "some error description";

	@Nested
	class FromJsonStringTests {

		@Test
		void shouldThrowExceptionOnFromJsonStringWithClass() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
					() -> jsonBuilder.fromJsonString(null, String.class));

			assertThat(e.getMessage(), equalTo(JsonObservability.ErrorMessage.JSON_LIBRARY_NOT_FOUND));
		}

		@Test
		void shouldThrowExceptionOnFromJsonStringWithGenericClass() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			GenericClass<List<String>> type = new GenericClass<>() {
				// empty
			};
			UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
					() -> jsonBuilder.fromJsonString(null, type));

			assertThat(e.getMessage(), equalTo(JsonObservability.ErrorMessage.JSON_LIBRARY_NOT_FOUND));
		}
	}

	@Nested
	class FromJsonBytesTests {

		@Test
		void shouldThrowExceptionOnFromJsonBytesWithClass() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
					() -> jsonBuilder.fromJsonBytes(null, String.class));

			assertThat(e.getMessage(), equalTo(JsonObservability.ErrorMessage.JSON_LIBRARY_NOT_FOUND));
		}

		@Test
		void shouldThrowExceptionOnFromJsonBytesWithGenericClass() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			GenericClass<List<String>> type = new GenericClass<>() {
				// empty
			};
			UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
					() -> jsonBuilder.fromJsonBytes(null, type));

			assertThat(e.getMessage(), equalTo(JsonObservability.ErrorMessage.JSON_LIBRARY_NOT_FOUND));
		}
	}

	@Nested
	class FromJsonInputStreamTests {

		@Test
		void shouldThrowExceptionOnFromJsonInputStreamWithClass() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
					() -> jsonBuilder.fromJsonInputStream(null, String.class));

			assertThat(e.getMessage(), equalTo(JsonObservability.ErrorMessage.JSON_LIBRARY_NOT_FOUND));
		}

		@Test
		void shouldThrowExceptionOnFromJsonInputStreamWithGenericClass() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			GenericClass<List<String>> type = new GenericClass<>() {
				// empty
			};
			UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
					() -> jsonBuilder.fromJsonInputStream(null, type));

			assertThat(e.getMessage(), equalTo(JsonObservability.ErrorMessage.JSON_LIBRARY_NOT_FOUND));
		}
	}

	@Nested
	class ToPropertiesMapTests {

		@Test
		void shouldReturnEmptyMapOnToPropertiesMapWhenSourceIsNull() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			Object result = jsonBuilder.toPropertiesMap(null, null);

			assertThat(result, equalTo(Collections.emptyMap()));
		}

		@Test
		void shouldReturnEmptyMapOnToPropertiesMapWhenSourceIsObject() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			Object result = jsonBuilder.toPropertiesMap(new Object(), null);

			assertThat(result, equalTo(Collections.emptyMap()));
		}

		@Test
		void shouldConvertObjectToPropertiesMap() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			Map<String, Object> result = jsonBuilder.toPropertiesMap(new Models.A(CUSTOMER_ID1, TENANT_ID1), null);

			assertThat(result.get("customer-id"), equalTo(CUSTOMER_ID1));
			assertThat(result.get("tenant-id"), equalTo(TENANT_ID1));
		}
	}

	@Nested
	class FromPropertiesMapTests {

		@Test
		void shouldReturnObjectOnFromPropertiesMapWhenMapIsNull() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			Object result = jsonBuilder.fromPropertiesMap(null, Object.class, Consumers.noConsumer());

			assertThat(result, not(equalTo(null)));
		}

		@Test
		void shouldReturnObjectOnFromPropertiesMapWhenMapIsEmpty() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			Object result = jsonBuilder.fromPropertiesMap(Collections.emptyMap(), Object.class, Consumers.noConsumer());

			assertThat(result, not(equalTo(null)));
		}

		@Test
		void shouldReturnObjectFromPropertiesMap() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			Models.A result = jsonBuilder.fromPropertiesMap(
					Map.of("customerId", CUSTOMER_ID1, "tenantId", TENANT_ID1),
					Models.A.class, Consumers.noConsumer());

			assertThat(result.getCustomerId(), equalTo(CUSTOMER_ID1));
			assertThat(result.getTenantId(), equalTo(TENANT_ID1));
		}
	}

	@Nested
	class ConvertMapTests {

		@Test
		void shouldReturnFallbackValueIfConversionFailsOnFromPropertiesMap() {
			String fallback = "fallback";
			String source = "source";
			SimpleConverter<String, String> converter = s -> {
				throw new RuntimeException("Conversion failed");
			};
			Holder<String> holder = Holder.empty();
			Consumer<Exception> errorConsumer = e -> {
				holder.setValue(e.getMessage());
			};
			String result = JsonBuilder.convert(source, converter, () -> fallback, errorConsumer);

			assertThat(result, equalTo(fallback));
			assertThat(holder.getValue(), equalTo("Conversion failed"));
		}

		@Test
		void shouldReturnFallbackValueIfConversionFailsOnFromPropertiesMapEvenIfOnErrorIsNull() {
			String fallback = "fallback";
			String source = "source";
			SimpleConverter<String, String> converter = s -> {
				throw new RuntimeException("Conversion failed");
			};
			String result = JsonBuilder.convert(source, converter, () -> fallback, null);

			assertThat(result, equalTo(fallback));
		}
	}

	@Nested
	class FromJsonTests {

		@Test
		void shouldTransformObjectToJsonStringAndReadItBack() {
			Models.A a1 = new Models.A(CUSTOMER_ID1, TENANT_ID1);

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

			Models.A a2 = JsonBuilder.fromJson(json1, Models.A.class);

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

			assertThat(json1, equalTo(json2));
		}

		@Test
		void shouldTransformObjectToJsonBytesAndReadItBack() {
			Models.A a1 = new Models.A(CUSTOMER_ID1, TENANT_ID1);

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1)).getBytes(StandardCharsets.UTF_8);

			Models.A a2 = JsonBuilder.fromJson(json1, Models.A.class);

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2)).getBytes(StandardCharsets.UTF_8);

			assertThat(json1, equalTo(json2));
		}

		@Test
		void shouldTransformObjectToJsonInputStreamAndReadItBack() {
			Models.A a1 = new Models.A(CUSTOMER_ID1, TENANT_ID1);

			byte[] json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1)).getBytes(StandardCharsets.UTF_8);

			Models.A a2 = JsonBuilder.fromJson(new ByteArrayInputStream(json1), Models.A.class);

			byte[] json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2)).getBytes(StandardCharsets.UTF_8);

			assertThat(json1, equalTo(json2));
		}

		@Test
		void shouldTransformGenericObjectToJsonStringAndReadItBack() {
			List<Models.A> list1 = List.of(new Models.A(CUSTOMER_ID1, TENANT_ID1));

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(list1));

			List<Models.A> list2 = JsonBuilder.fromJson(json1, new GenericClass<>() {
				// empty
			});

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(list2));

			assertThat(json1, equalTo(json2));
		}

		@Test
		void shouldTransformGenericObjectToJsonBytesStringAndReadItBack() {
			List<Models.A> list1 = List.of(new Models.A(CUSTOMER_ID1, TENANT_ID1));

			Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(list1)).getBytes(StandardCharsets.UTF_8);

			List<Models.A> list2 = JsonBuilder.fromJson(json1, new GenericClass<>() {
				// empty
			});

			Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(list2)).getBytes(StandardCharsets.UTF_8);

			assertThat(json1, equalTo(json2));
		}

		@Test
		void shouldTransformGenericObjectToJsonInputStreamAndReadItBack() {
			List<Models.A> list1 = List.of(new Models.A(CUSTOMER_ID1, TENANT_ID1));

			byte[] json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(list1)).getBytes(StandardCharsets.UTF_8);

			List<Models.A> list2 = JsonBuilder.fromJson(new ByteArrayInputStream(json1), new GenericClass<>() {
				// empty
			});

			byte[] json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(list2)).getBytes(StandardCharsets.UTF_8);

			assertThat(json1, equalTo(json2));
		}

		@Test
		void shouldThrowExceptionWhenReadingJsonObjectWithAnUnsupportedType() {
			Object o = new Object();
			UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
					() -> JsonBuilder.fromJson(o, Models.A.class));

			assertThat(e.getMessage(), equalTo("Unsupported JSON input type: " + Object.class));
		}

		@Test
		void shouldThrowExceptionWhenReadingGenericJsonObjectWithAnUnsupportedType() {
			GenericClass<List<Models.A>> type = new GenericClass<>() {
				// empty
			};
			Object o = new Object();
			UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
					() -> JsonBuilder.fromJson(o, type));

			assertThat(e.getMessage(), equalTo("Unsupported JSON input type: " + Object.class));
		}

		@Test
		void shouldCreateObjectWithCreator() {
			String json = "{\"error\":\"" + ERROR + "\",\"error_description\":\"" + ERROR_DESCRIPTION + "\"}";

			Models.ErrorResponse response = JsonBuilder.fromJson(json, Models.ErrorResponse.class);

			assertThat(response.getError(), equalTo(ERROR));
			assertThat(response.getErrorDescription(), equalTo(ERROR_DESCRIPTION));
		}
	}

	@Nested
	class InitializationTests {

		@Test
		void shouldThrowExceptionWhenTryingToInstantiatePropertyNestedClass() {
			UnsupportedOperationException e = Assertions.assertDefaultConstructorThrows(JsonBuilder.Property.class);

			assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
		}

		@Test
		void shouldReturnAJsonBuilderInstanceIfNoLibraryInfoIsProvided() {
			JsonBuilder instance = JsonBuilder.initializeInstance((OptionalLibrary<? extends JsonBuilder>[]) null);

			assertThat(instance.getClass(), equalTo(JsonBuilder.class));
		}
	}

	@Nested
	class ToIdentityJsonTests {

		@Test
		void shouldReturnIdentityJsonStringIfJsonLibraryIsNotPresent() {
			JsonBuilder jsonBuilder = new JsonBuilder();
			Object o = new Object();

			boolean indentOutput = jsonBuilder.isIndentOutput();
			String indent = indentOutput ? jsonBuilder.eol() : " ";
			String tab = indentOutput ? "\t" : "";
			String expected = "{" + indent + tab + "\"identity\":\"" + Strings.identityHashCode(o) + "\"" + indent + "}";

			String result = jsonBuilder.toJsonString(o);

			assertThat(result, equalTo(expected));
		}

		@Test
		void shouldReturnIdentityJsonStringIfJsonLibraryIsNotPresentWithRuntime() {
			Object o = new Object();

			JsonBuilder runtime = JsonBuilder.runtime();
			boolean indentOutput = runtime.isIndentOutput();
			String indent = indentOutput ? runtime.eol() : " ";
			String tab = indentOutput ? "\t" : "";
			String expected = "{" + indent + tab + "\"identity\":\"" + Strings.identityHashCode(o) + "\"" + indent + "}";

			String result = JsonBuilder.toIdentityJson(o);

			assertThat(result, equalTo(expected));
		}
	}

	@Nested
	class ToJsonTests {

		@Test
		void shouldRunTheGivenSupplierOnTheGivenJsonBuilderInstance() {
			JsonBuilder jsonBuilder1 = new JsonBuilder();
			jsonBuilder1.indentOutput(false);

			JsonBuilder jsonBuilder2 = new JsonBuilder();
			jsonBuilder2.indentOutput(true);

			Object object = new Object();

			String expected1 = "{ \"identity\":\"" + Strings.identityHashCode(object) + "\" }";
			String expected2 =
					"{" + System.lineSeparator() + "\t\"identity\":\"" + Strings.identityHashCode(object) + "\"" + System.lineSeparator() + "}";

			String result1 = JsonBuilder.with(jsonBuilder1, () -> JsonBuilder.toJson(object));
			String result2 = JsonBuilder.with(jsonBuilder2, () -> JsonBuilder.toJson(object));

			assertThat(result1, equalTo(expected1));
			assertThat(result2, equalTo(expected2));
		}

		@Test
		void shouldRunTheGivenSupplierEvenOnRecursiveCalls() {
			JsonBuilder jsonBuilder1 = new JsonBuilder();
			jsonBuilder1.indentOutput(false);

			JsonBuilder jsonBuilder2 = new JsonBuilder();
			jsonBuilder2.indentOutput(true);

			Object object = new Object();

			String expected =
					"{" + System.lineSeparator() + "\t\"identity\":\"" + Strings.identityHashCode(object) + "\"" + System.lineSeparator() + "}";

			String result = JsonBuilder.with(jsonBuilder1, () -> {
				// recursive call to with
				return JsonBuilder.with(jsonBuilder2, () -> JsonBuilder.toJson(object));
			});

			assertThat(result, equalTo(expected));
		}
	}

	@Nested
	class IndentationTests {

		@ParameterizedTest
		@ValueSource(booleans = { true, false })
		void shouldBuildIndentationEnumFromBoolean(final boolean value) {
			JsonBuilder.Indentation indentation = JsonBuilder.Indentation.fromBoolean(value);

			if (value) {
				assertThat(indentation, equalTo(JsonBuilder.Indentation.ENABLED));
			} else {
				assertThat(indentation, equalTo(JsonBuilder.Indentation.DISABLED));
			}
		}
	}

	interface Models {

		static class A {

			private String customerId;

			private String tenantId;

			public A() {
				// empty
			}

			public A(final String customerId, final String tenantId) {
				this.customerId = customerId;
				this.tenantId = tenantId;
			}

			public String getCustomerId() {
				return customerId;
			}

			public void setCustomerId(final String customerId) {
				this.customerId = customerId;
			}

			public String getTenantId() {
				return tenantId;
			}

			public void setTenantId(final String tenantId) {
				this.tenantId = tenantId;
			}
		}

		static class ErrorResponse {

			private final String error;

			private final String errorDescription;

			@Creator
			public ErrorResponse(
					@FieldName("error") final String error,
					@FieldName("error_description") final String description) {
				this.error = error;
				this.errorDescription = description;
			}

			public String getError() {
				return error;
			}

			public String getErrorDescription() {
				return errorDescription;
			}
		}
	}
}
