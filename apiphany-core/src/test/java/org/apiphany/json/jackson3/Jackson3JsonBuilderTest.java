package org.apiphany.json.jackson3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apiphany.io.IOStreams;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.Fields;
import org.morphix.reflection.GenericClass;

import tools.jackson.core.type.TypeReference;

/**
 * Test class for {@link Jackson3JsonBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson3JsonBuilderTest extends Jackson3Test {

	private static final String CUSTOMER_ONE = "customerOne";
	private static final String CUSTOMER_TWO = "customerTwo";
	private static final String CUSTOMER_ID1 = "cid1";
	private static final String CUSTOMER_ID2 = "cid2";
	private static final String TENANT_ID1 = "tid1";
	private static final String TENANT_ID2 = "tid2";

	private static final String SOME_INVALID_JSON_STRING = "some invalid json";
	private static final byte[] SOME_INVALID_JSON_BYTES = SOME_INVALID_JSON_STRING.getBytes();
	private static final InputStream SOME_INVALID_JSON_INPUT_STREAM = new ByteArrayInputStream(SOME_INVALID_JSON_BYTES);

	private final Jackson3JsonBuilder jsonBuilder = new Jackson3JsonBuilder();

	@Test
	void shouldTransformObjectToJsonStringAndReadItBack() {
		A a1 = new A();
		Map<String, B> elements = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));
		a1.setElements(elements);

		Object json1 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(a1));

		A a2 = Jackson3JsonBuilder.fromJson(json1, A.class);

		Object json2 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(a2));

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformObjectToJsonBytesAndReadItBack() {
		A a1 = new A();
		Map<String, B> elements = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));
		a1.setElements(elements);

		Object json1 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(a1)).getBytes();

		A a2 = Jackson3JsonBuilder.fromJson(json1, A.class);

		Object json2 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(a2)).getBytes();

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformObjectToJsonInputStreamAndReadItBack() throws IOException {
		A a1 = new A();
		Map<String, B> elements = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));
		a1.setElements(elements);

		byte[] jsonBytes1 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(a1)).getBytes();
		Object json1 = new ByteArrayInputStream(jsonBytes1);

		A a2 = Jackson3JsonBuilder.fromJson(json1, A.class);

		byte[] jsonBytes2 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(a2)).getBytes();
		Object json2 = new ByteArrayInputStream(jsonBytes2);

		// we need a new stream because the previous one has been read and is at the end of the stream, so we cannot read it
		// again to compare the bytes.
		byte[] bytes1 = IOStreams.toByteArray(new ByteArrayInputStream(jsonBytes1));
		byte[] bytes2 = IOStreams.toByteArray((InputStream) json2);

		assertThat(bytes1, equalTo(bytes2));
	}

	@Test
	void shouldThrowExceptionWhenReadingJsonObjectWithAnUnsupportedType() {
		Object o = new Object();
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> Jackson3JsonBuilder.fromJson(o, A.class));

		assertThat(e.getMessage(), equalTo("Unsupported JSON input type: " + Object.class));
	}

	@Test
	void shouldTransformGenericObjectToJsonStringAndReadItBack() {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		Object json1 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements1));

		Map<String, B> elements2 = Jackson3JsonBuilder.fromJson(json1, new GenericClass<>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements2));

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformGenericObjectToJsonBytesAndReadItBack() {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		Object json1 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements1)).getBytes();

		Map<String, B> elements2 = Jackson3JsonBuilder.fromJson(json1, new GenericClass<>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements2)).getBytes();

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformGenericObjectToJsonInputStreamAndReadItBack() throws IOException {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		byte[] jsonBytes1 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements1)).getBytes();
		Object json1 = new ByteArrayInputStream(jsonBytes1);

		Map<String, B> elements2 = Jackson3JsonBuilder.fromJson(json1, new GenericClass<>() {
			// empty
		});

		byte[] jsonBytes2 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements2)).getBytes();
		Object json2 = new ByteArrayInputStream(jsonBytes2);

		// we need a new stream because the previous one has been read and is at the end of the stream, so we cannot read it
		// again to compare the bytes.
		byte[] bytes1 = IOStreams.toByteArray(new ByteArrayInputStream(jsonBytes1));
		byte[] bytes2 = IOStreams.toByteArray((InputStream) json2);

		assertThat(bytes1, equalTo(bytes2));
	}

	@Test
	void shouldThrowExceptionWhenReadingJsonGenericObjectWithAnUnsupportedType() {
		Object o = new Object();
		var genericClass = new GenericClass<Map<String, B>>() {
			// empty
		};
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> Jackson3JsonBuilder.fromJson(o, genericClass));

		assertThat(e.getMessage(), equalTo("Unsupported JSON input type: " + Object.class));
	}

	@Test
	void shouldTransformGenericObjectTypeReferenceToJsonStringAndReadItBack() {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		Object json1 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements1));

		Map<String, B> elements2 = Jackson3JsonBuilder.fromJson(json1, new TypeReference<>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements2));

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformGenericObjectTypeReferenceToJsonBytesAndReadItBack() {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		Object json1 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements1)).getBytes();

		Map<String, B> elements2 = Jackson3JsonBuilder.fromJson(json1, new TypeReference<>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements2)).getBytes();

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformGenericObjectTypeReferenceToJsonInputStreamAndReadItBack() throws IOException {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		byte[] jsonBytes1 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements1)).getBytes();
		Object json1 = new ByteArrayInputStream(jsonBytes1);

		Map<String, B> elements2 = Jackson3JsonBuilder.fromJson(json1, new TypeReference<>() {
			// empty
		});

		byte[] jsonBytes2 = Strings.removeAllWhitespace(Jackson3JsonBuilder.toJson(elements2)).getBytes();
		Object json2 = new ByteArrayInputStream(jsonBytes2);

		// we need a new stream because the previous one has been read and is at the end of the stream, so we cannot read it
		// again to compare the bytes.
		byte[] bytes1 = IOStreams.toByteArray(new ByteArrayInputStream(jsonBytes1));
		byte[] bytes2 = IOStreams.toByteArray((InputStream) json2);

		assertThat(bytes1, equalTo(bytes2));
	}

	@Test
	void shouldThrowExceptionWhenReadingJsonTypeReferenceWithAnUnsupportedType() {
		Object o = new Object();
		var typeReference = new TypeReference<Map<String, B>>() {
			// empty
		};
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> Jackson3JsonBuilder.fromJson(o, typeReference));

		assertThat(e.getMessage(), equalTo("Unsupported JSON input type: " + Object.class));
	}

	@Test
	void shouldReturnNullOnToJsonStringWhenInputIsNull() {
		Object json = jsonBuilder.toJsonString(null);

		assertThat(json, equalTo(null));
	}

	@Test
	void shouldReturnDebugStringOnToJsonStringWhenDebugStringIsEnabled() {
		Jackson3JsonBuilder jacksonJsonBuilder = new Jackson3JsonBuilder();
		Fields.IgnoreAccess.set(jacksonJsonBuilder, "debugString", true);

		B b = new B(CUSTOMER_ID1, TENANT_ID1);

		String expectedDebugString = "{ \"type\":\"" + B.class.getCanonicalName() + "\", \"identity\":\"" + B.class.getName() + "@"
				+ Integer.toHexString(b.hashCode()) + "\" }";

		String debugString = jacksonJsonBuilder.toJsonString(b);

		assertThat(debugString, equalTo(expectedDebugString));
	}

	@Test
	void shouldBuildFromPropertiesMapWhenAllKeysAreKebabCase() {
		Map<String, Object> props = Map.of(
				"elements", Map.of(
						"customer-one", Map.of("customer-id", CUSTOMER_ID1, "tenant-id", TENANT_ID1),
						"customer-two", Map.of("customer-id", CUSTOMER_ID2, "tenant-id", TENANT_ID2)));

		A result = jsonBuilder.fromPropertiesMap(props, A.class, Consumers.noConsumer());

		assertThat(result.getElements().get("customer-one").customerId, equalTo(CUSTOMER_ID1));
		assertThat(result.getElements().get("customer-one").tenantId, equalTo(TENANT_ID1));
		assertThat(result.getElements().get("customer-two").customerId, equalTo(CUSTOMER_ID2));
		assertThat(result.getElements().get("customer-two").tenantId, equalTo(TENANT_ID2));
	}

	@Test
	void shouldCallOnErrorConsumerWhenBuildingFromPropertiesMapWithInvalidData() {
		Map<String, Object> props = Map.of(
				"elements", Map.of(
						"customer-one", "invalid-data"));

		final List<Throwable> errors = new ArrayList<>();

		A result = jsonBuilder.fromPropertiesMap(props, A.class, errors::add);

		assertThat(result, equalTo(null));
		assertThat(errors.size(), equalTo(1));
		assertThat(errors.get(0).getMessage(), startsWith("Cannot construct instance of `" + B.class.getName() + "`"));
	}

	@Test
	void shouldBuildFromPropertiesMapByKeepingNonKebabStringKeysForMaps() {
		Map<String, Object> props = Map.of(
				"elements", Map.of(
						CUSTOMER_ONE, Map.of("customer-id", CUSTOMER_ID1, "tenant-id", TENANT_ID1),
						CUSTOMER_TWO, Map.of("customer-id", CUSTOMER_ID2, "tenant-id", TENANT_ID2)));

		A result = jsonBuilder.fromPropertiesMap(props, A.class, Consumers.noConsumer());

		assertThat(result.getElements().get(CUSTOMER_ONE).customerId, equalTo(CUSTOMER_ID1));
		assertThat(result.getElements().get(CUSTOMER_ONE).tenantId, equalTo(TENANT_ID1));
		assertThat(result.getElements().get(CUSTOMER_TWO).customerId, equalTo(CUSTOMER_ID2));
		assertThat(result.getElements().get(CUSTOMER_TWO).tenantId, equalTo(TENANT_ID2));
	}

	@Test
	void shouldReturnNullWhenDeserializingStringJsonFailsWithClass() {
		A result = jsonBuilder.fromJsonString(SOME_INVALID_JSON_STRING, A.class);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenDeserializingStringJsonFailsWithTypeReference() {
		List<A> result = jsonBuilder.fromJsonString(SOME_INVALID_JSON_STRING, new TypeReference<>() {
			// empty
		});

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenDeserializingStringJsonFailsWithGenericClass() {
		List<A> result = jsonBuilder.fromJsonString(SOME_INVALID_JSON_STRING, new GenericClass<>() {
			// empty
		});

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenDeserializingBytesJsonFailsWithClass() {
		A result = jsonBuilder.fromJsonBytes(SOME_INVALID_JSON_BYTES, A.class);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenDeserializingBytesJsonFailsWithTypeReference() {
		List<A> result = jsonBuilder.fromJsonBytes(SOME_INVALID_JSON_BYTES, new TypeReference<>() {
			// empty
		});

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenDeserializingBytesJsonFailsWithGenericClass() {
		List<A> result = jsonBuilder.fromJsonBytes(SOME_INVALID_JSON_BYTES, new GenericClass<>() {
			// empty
		});

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenDeserializingInputStreamJsonFailsWithClass() {
		A result = jsonBuilder.fromJsonInputStream(SOME_INVALID_JSON_INPUT_STREAM, A.class);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenDeserializingInputStreamJsonFailsWithTypeReference() {
		List<A> result = jsonBuilder.fromJsonInputStream(SOME_INVALID_JSON_INPUT_STREAM, new TypeReference<>() {
			// empty
		});

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenDeserializingInputStreamJsonFailsWithGenericClass() {
		List<A> result = jsonBuilder.fromJsonInputStream(SOME_INVALID_JSON_INPUT_STREAM, new GenericClass<>() {
			// empty
		});

		assertThat(result, equalTo(null));
	}

	static class A {

		private Map<String, B> elements;

		public Map<String, B> getElements() {
			return elements;
		}

		public void setElements(final Map<String, B> elements) {
			this.elements = elements;
		}

	}

	static class B {

		private String customerId;

		private String tenantId;

		public B() {
			// empty
		}

		public B(final String customerId, final String tenantId) {
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

	static class C {

		private String name;

		public C() {
			// empty
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	static class D {

		private Exception exception;

		public D() {
			// empty
		}

		public Exception getException() {
			return exception;
		}

		public void setException(final Exception exception) {
			this.exception = exception;
		}
	}
}
