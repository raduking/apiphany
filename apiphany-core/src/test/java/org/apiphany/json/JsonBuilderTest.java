package org.apiphany.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.apiphany.lang.Holder;
import org.apiphany.lang.LibraryDescriptor;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.morphix.convert.function.SimpleConverter;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.GenericClass;
import org.morphix.reflection.MemberAccessor;

/**
 * Test class for {@link JsonBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonBuilderTest {

	private static final String CUSTOMER_ID1 = "cid1";
	private static final String TENANT_ID1 = "tid1";
	private static final long TEST_LONG = 42L;

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

		assertThat(result.get("customerId"), equalTo(CUSTOMER_ID1));
		assertThat(result.get("tenantId"), equalTo(TENANT_ID1));
	}

	@Test
	void shouldReturnNullOnFromPropertiesMapWhenMapIsNull() {
		Object result = jsonBuilder.fromPropertiesMap(null, null, Consumers.noConsumer());

		assertThat(result, equalTo(null));
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
		Holder<String> holder = Holder.noValue();
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

		String result = JsonBuilder.toDebugString(b);

		String expected =
				"{ \"type\":\"" + B.class.getCanonicalName() + "\", \"id\":\"" + TEST_LONG + "\", \"identity\":\"" + JsonBuilder.identityHashCode(b)
						+ "\" }";

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTheDebugString() {
		Object o = new Object();

		String result = JsonBuilder.toDebugString(o);

		String expected = "{ \"type\":\"" + Object.class.getCanonicalName() + "\", \"identity\":\"" + JsonBuilder.identityHashCode(o) + "\" }";

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTheDebugStringForNullObjects() {
		String o = null;

		String result = JsonBuilder.toDebugString(o);

		String expected = "{ \"type\":null, \"identity\":" + JsonBuilder.identityHashCode(o) + " }";

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnIdentityJsonStringIfJsonLibraryIsNotPresent() {
		Object o = new Object();

		boolean indentOutput = jsonBuilder.isIndentOutput();
		String indent = indentOutput ? jsonBuilder.eol() : " ";
		String tab = indentOutput ? "\t" : "";
		String expected = "{" + indent + tab + "\"identity\":\"" + JsonBuilder.identityHashCode(o) + "\"" + indent + "}";

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
		String expected = "{" + indent + tab + "\"identity\":\"" + JsonBuilder.identityHashCode(o) + "\"" + indent + "}";

		String result = JsonBuilder.toIdentityJson(o);

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnAJsonBuilderInstanceIfNoLibraryInfoIsProvided() {
		JsonBuilder instance = JsonBuilder.initializeInstance((LibraryDescriptor<? extends JsonBuilder>[]) null);

		assertThat(instance.getClass(), equalTo(JsonBuilder.class));
	}

	@Test
	void shouldRunTheGivenSupplierOnTheGivenJsonBuilderInstance() {
		JsonBuilder jsonBuilder1 = new JsonBuilder();
		jsonBuilder1.indentOutput(false);

		JsonBuilder jsonBuilder2 = new JsonBuilder();
		jsonBuilder2.indentOutput(true);

		Object object = new Object();

		String expected1 = "{ \"identity\":\"" + JsonBuilder.identityHashCode(object) + "\" }";
		String expected2 =
				"{" + System.lineSeparator() + "\t\"identity\":\"" + JsonBuilder.identityHashCode(object) + "\"" + System.lineSeparator() + "}";

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
				"{" + System.lineSeparator() + "\t\"identity\":\"" + JsonBuilder.identityHashCode(object) + "\"" + System.lineSeparator() + "}";

		String result = JsonBuilder.with(jsonBuilder1, () -> {
			// recursive call to with
			return JsonBuilder.with(jsonBuilder2, () -> JsonBuilder.toJson(object));
		});

		assertThat(result, equalTo(expected));
	}
}
