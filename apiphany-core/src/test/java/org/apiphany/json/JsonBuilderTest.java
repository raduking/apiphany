package org.apiphany.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.apiphany.lang.Strings;
import org.apiphany.lang.annotation.Creator;
import org.apiphany.lang.annotation.FieldName;
import org.apiphany.security.MessageDigestAlgorithm;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.lang.Holder;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.GenericClass;
import org.morphix.reflection.MemberAccessor;
import org.morphix.runtime.OptionalLibrary;

/**
 * Test class for {@link JsonBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonBuilderTest {

	private static final String CUSTOMER_ID1 = "cid1";
	private static final String TENANT_ID1 = "tid1";
	private static final long TEST_LONG = 42L;
	private static final String ERROR = "some error";
	private static final String ERROR_DESCRIPTION = "some error description";

	private final JsonBuilder jsonBuilder = new JsonBuilder();

	@Test
	void shouldThrowExceptionOnFromJsonStringWithClass() {
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> jsonBuilder.fromJsonString(null, String.class));

		assertThat(e.getMessage(), equalTo(JsonBuilder.ErrorMessage.JSON_LIBRARY_NOT_FOUND));
	}

	@Test
	void shouldThrowExceptionOnFromJsonStringWithGenericClass() {
		GenericClass<List<String>> type = new GenericClass<>() {
			// empty
		};
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> jsonBuilder.fromJsonString(null, type));

		assertThat(e.getMessage(), equalTo(JsonBuilder.ErrorMessage.JSON_LIBRARY_NOT_FOUND));
	}

	@Test
	void shouldReturnEmptyMapOnToPropertiesMapWhenSourceIsNull() {
		Object result = jsonBuilder.toPropertiesMap(null, null);

		assertThat(result, equalTo(Collections.emptyMap()));
	}

	@Test
	void shouldReturnEmptyMapOnToPropertiesMapWhenSourceIsObject() {
		Object result = jsonBuilder.toPropertiesMap(new Object(), null);

		assertThat(result, equalTo(Collections.emptyMap()));
	}

	@Test
	void shouldConvertObjectToPropertiesMap() {
		Map<String, Object> result = jsonBuilder.toPropertiesMap(new A(CUSTOMER_ID1, TENANT_ID1), null);

		assertThat(result.get("customer-id"), equalTo(CUSTOMER_ID1));
		assertThat(result.get("tenant-id"), equalTo(TENANT_ID1));
	}

	@Test
	void shouldReturnObjectOnFromPropertiesMapWhenMapIsNull() {
		Object result = jsonBuilder.fromPropertiesMap(null, Object.class, Consumers.noConsumer());

		assertThat(result, not(equalTo(null)));
	}

	@Test
	void shouldReturnObjectOnFromPropertiesMapWhenMapIsEmpty() {
		Object result = jsonBuilder.fromPropertiesMap(Collections.emptyMap(), Object.class, Consumers.noConsumer());

		assertThat(result, not(equalTo(null)));
	}

	@Test
	void shouldReturnObjectFromPropertiesMap() {
		A result = jsonBuilder.fromPropertiesMap(
				Map.of("customerId", CUSTOMER_ID1, "tenantId", TENANT_ID1),
				A.class, Consumers.noConsumer());

		assertThat(result.getCustomerId(), equalTo(CUSTOMER_ID1));
		assertThat(result.getTenantId(), equalTo(TENANT_ID1));
	}

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

	@Test
	void shouldTransformObjectToJsonAndReadItBack() {
		A a1 = new A(CUSTOMER_ID1, TENANT_ID1);

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(a1));

		A a2 = JsonBuilder.fromJson(json1, A.class);

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(a2));

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformGenericObjectToJsonAndReadItBack() {
		List<A> list1 = List.of(new A(CUSTOMER_ID1, TENANT_ID1));

		Object json1 = Strings.removeAllWhitespace(JsonBuilder.toJson(list1));

		List<A> list2 = JsonBuilder.fromJson(json1, new GenericClass<>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(JsonBuilder.toJson(list2));

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldThrowExceptionWhenReadingJsonObjectWithAnUnsupportedType() {
		Object o = new Object();
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> JsonBuilder.fromJson(o, A.class));

		assertThat(e.getMessage(), equalTo("Unsupported JSON input type: " + Object.class));
	}

	@Test
	void shouldThrowExceptionWhenReadingGenericJsonObjectWithAnUnsupportedType() {
		GenericClass<List<A>> type = new GenericClass<>() {
			// empty
		};
		Object o = new Object();
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> JsonBuilder.fromJson(o, type));

		assertThat(e.getMessage(), equalTo("Unsupported JSON input type: " + Object.class));
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiatePropertyNestedClass() throws Exception {
		Throwable targetException = null;
		Constructor<JsonBuilder.Property> defaultConstructor = JsonBuilder.Property.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<JsonBuilder.Property>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
			targetException = e.getTargetException();
		}
		assertInstanceOf(UnsupportedOperationException.class, targetException);
	}

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateErrorMessageNestedClass() throws Exception {
		Throwable targetException = null;
		Constructor<JsonBuilder.ErrorMessage> defaultConstructor = JsonBuilder.ErrorMessage.class.getDeclaredConstructor();
		try (MemberAccessor<Constructor<JsonBuilder.ErrorMessage>> ignored = new MemberAccessor<>(null, defaultConstructor)) {
			defaultConstructor.newInstance();
		} catch (InvocationTargetException e) {
			assertThat(e.getTargetException().getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
			targetException = e.getTargetException();
		}
		assertInstanceOf(UnsupportedOperationException.class, targetException);
	}

	static class B {

		final String id;

		public B(final Long id) {
			this.id = id.toString();
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof B that) {
				return Objects.equals(this.id, that.id);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(id);
		}
	}

	@Test
	void shouldReturnTheDebugStringForObjectsWithId() {
		B b = new B(TEST_LONG);

		String result = JsonBuilder.toDebugJsonString(b);

		String expected =
				"{ \"type\":\"" + B.class.getCanonicalName() + "\", \"id\":\"" + TEST_LONG + "\", \"identity\":\"" + Strings.identityHashCode(b)
						+ "\" }";

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTheDebugString() {
		Object o = new Object();

		String result = JsonBuilder.toDebugJsonString(o);

		String expected = "{ \"type\":\"" + Object.class.getCanonicalName() + "\", \"identity\":\"" + Strings.identityHashCode(o) + "\" }";

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTheDebugStringForNullObjects() {
		String o = null;

		String result = JsonBuilder.toDebugJsonString(o);

		String expected = "{ \"type\":null, \"identity\":" + Strings.identityHashCode(o) + " }";

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnIdentityJsonStringIfJsonLibraryIsNotPresent() {
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

	@Test
	void shouldReturnAJsonBuilderInstanceIfNoLibraryInfoIsProvided() {
		JsonBuilder instance = JsonBuilder.initializeInstance((OptionalLibrary<? extends JsonBuilder>[]) null);

		assertThat(instance.getClass(), equalTo(JsonBuilder.class));
	}

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

	@Test
	void shouldCreateObjectWithCreator() {
		String json = "{\"error\":\"" + ERROR + "\",\"error_description\":\"" + ERROR_DESCRIPTION + "\"}";

		ErrorResponse response = JsonBuilder.fromJson(json, ErrorResponse.class);

		assertThat(response.getError(), equalTo(ERROR));
		assertThat(response.getErrorDescription(), equalTo(ERROR_DESCRIPTION));
	}

	@Nested
	class DescribeJsonInputTests {

		@Test
		void shouldDescribeNullInput() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(false);

			String result = runtime.describeJsonInput((Object) null);

			assertThat(result, equalTo("null"));
		}

		@Test
		void shouldDescribeStringInputWithoutPreviewWhenDebugDisabled() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(false);
			String input = "abc";

			String result = runtime.describeJsonInput(input);

			String hash = MessageDigestAlgorithm.SHA256.hash(input, 8);
			assertThat(result, equalTo(String.class.getTypeName() + "(length=3, hash=" + hash + ")"));
		}

		@Test
		void shouldDescribeStringInputWithPreviewWhenDebugEnabled() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(true);
			String input = "abc";

			String result = runtime.describeJsonInput(input);

			String hash = MessageDigestAlgorithm.SHA256.hash(input, 8);
			assertThat(result, equalTo(String.class.getTypeName() + "(length=3, hash=" + hash + ", preview=abc)"));
		}

		@Test
		void shouldDescribeByteArrayInputWithoutPreviewWhenDebugDisabled() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(false);
			byte[] input = "abc".getBytes(StandardCharsets.UTF_8);

			String result = runtime.describeJsonInput(input);

			String hash = MessageDigestAlgorithm.SHA256.hash(input, 8);
			assertThat(result, equalTo("byte[](length=3, hash=" + hash + ")"));
		}

		@Test
		void shouldDescribeByteArrayInputWithPreviewWhenDebugEnabled() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(true);
			byte[] input = "abc".getBytes(StandardCharsets.UTF_8);

			String result = runtime.describeJsonInput(input);

			String hash = MessageDigestAlgorithm.SHA256.hash(input, 8);
			assertThat(result, equalTo("byte[](length=3, hash=" + hash + ", preview=abc)"));
		}

		@Test
		void shouldDescribeInputStreamInput() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(false);
			ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);

			String result = runtime.describeJsonInput(input);

			assertThat(result, equalTo(ByteArrayInputStream.class.getTypeName()
					+ "(hash=" + Integer.toHexString(System.identityHashCode(input)) + ")"));
		}

		@Test
		void shouldDescribeObjectInput() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(false);
			Object input = new Object();

			String result = runtime.describeJsonInput(input);

			assertThat(result, equalTo(Object.class.getTypeName()
					+ "(hash=" + Integer.toHexString(System.identityHashCode(input)) + ")"));
		}

		@Test
		void shouldDescribeObjectInputWithDebugStringEnabled() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(true);
			Object input = new Object();

			String result = runtime.describeJsonInput(input);

			assertThat(result, equalTo(Object.class.getTypeName()
					+ "(hash=" + Integer.toHexString(System.identityHashCode(input)) + ")"));
		}

		private static JsonBuilder newJsonBuilderWithDebugString(final boolean debugString) {
			String previous = System.getProperty(JsonBuilder.Property.DEBUG_STRING);
			try {
				if (debugString) {
					System.setProperty(JsonBuilder.Property.DEBUG_STRING, "true");
				} else {
					System.clearProperty(JsonBuilder.Property.DEBUG_STRING);
				}
				return new JsonBuilder();
			} finally {
				if (null == previous) {
					System.clearProperty(JsonBuilder.Property.DEBUG_STRING);
				} else {
					System.setProperty(JsonBuilder.Property.DEBUG_STRING, previous);
				}
			}
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
}
