package org.apiphany.json.jackson;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.Fields;
import org.morphix.reflection.GenericClass;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;

/**
 * Test class for {@link JacksonJsonBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class JacksonJsonBuilderTest {

	private static final String CUSTOMER_ONE = "customerOne";
	private static final String CUSTOMER_TWO = "customerTwo";
	private static final String CUSTOMER_ID1 = "cid1";
	private static final String CUSTOMER_ID2 = "cid2";
	private static final String TENANT_ID1 = "tid1";
	private static final String TENANT_ID2 = "tid2";

	private static final String EXPECTED_EXCEPTION_MESSAGE = "Expected exception";
	private static final String SOME_INVALID_JSON_STRING = "some invalid json";
	private static final byte[] SOME_INVALID_JSON_BYTES = SOME_INVALID_JSON_STRING.getBytes();
	private static final String SOME_NAME = "someName";

	private JacksonJsonBuilder jsonBuilder = new JacksonJsonBuilder();

	@Test
	void shouldTransformObjectToJsonStringAndReadItBack() {
		A a1 = new A();
		Map<String, B> elements = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));
		a1.setElements(elements);

		Object json1 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(a1));

		A a2 = JacksonJsonBuilder.fromJson(json1, A.class);

		Object json2 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(a2));

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformObjectToJsonBytesAndReadItBack() {
		A a1 = new A();
		Map<String, B> elements = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));
		a1.setElements(elements);

		Object json1 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(a1)).getBytes();

		A a2 = JacksonJsonBuilder.fromJson(json1, A.class);

		Object json2 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(a2)).getBytes();

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldThrowExceptionWhenReadingJsonObjectWithAnUnsupportedType() {
		Object o = new Object();
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> JacksonJsonBuilder.fromJson(o, A.class));

		assertThat(e.getMessage(), equalTo("Unsupported JSON input type: " + Object.class));
	}

	@Test
	void shouldTransformGenericObjectToJsonStringAndReadItBack() {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		Object json1 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements1));

		Map<String, B> elements2 = JacksonJsonBuilder.fromJson(json1, new GenericClass<Map<String, B>>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements2));

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformGenericObjectToJsonBytesAndReadItBack() {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		Object json1 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements1)).getBytes();

		Map<String, B> elements2 = JacksonJsonBuilder.fromJson(json1, new GenericClass<Map<String, B>>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements2)).getBytes();

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldThrowExceptionWhenReadingJsonGenericObjectWithAnUnsupportedType() {
		Object o = new Object();
		var genericClass = new GenericClass<Map<String, B>>() {
			// empty
		};
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> JacksonJsonBuilder.fromJson(o, genericClass));

		assertThat(e.getMessage(), equalTo("Unsupported JSON input type: " + Object.class));
	}

	@Test
	void shouldTransformGenericObjectTypeReferenceToJsonStringAndReadItBack() {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		Object json1 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements1));

		Map<String, B> elements2 = JacksonJsonBuilder.fromJson(json1, new TypeReference<Map<String, B>>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements2));

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformGenericObjectTypeReferenceToJsonBytesAndReadItBack() {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		Object json1 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements1)).getBytes();

		Map<String, B> elements2 = JacksonJsonBuilder.fromJson(json1, new TypeReference<Map<String, B>>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements2)).getBytes();

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldThrowExceptionWhenReadingJsonTypeReferenceWithAnUnsupportedType() {
		Object o = new Object();
		var typeReference = new TypeReference<Map<String, B>>() {
			// empty
		};
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> JacksonJsonBuilder.fromJson(o, typeReference));

		assertThat(e.getMessage(), equalTo("Unsupported JSON input type: " + Object.class));
	}

	@Test
	void shouldReturnNullOnToJsonStringWhenInputIsNull() {
		Object json = jsonBuilder.toJsonString(null);

		assertThat(json, equalTo(null));
	}

	@Test
	void shouldReturnDebugStringOnToJsonStringWhenDebugStringIsEnabled() {
		JacksonJsonBuilder jacksonJsonBuilder = new JacksonJsonBuilder();
		Fields.IgnoreAccess.set(jacksonJsonBuilder, "debugString", true);

		B b = new B(CUSTOMER_ID1, TENANT_ID1);

		String expectedDebugString = "{ \"type\":\"" + B.class.getCanonicalName() + "\", \"hash\":\"" + B.class.getName() + "@"
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
	void shouldConfigureSensitivityWithExistingAnnotationIntrospector() {
		ObjectMapper objectMapper = mock(ObjectMapper.class);

		SerializationConfig serializationConfig = mock(SerializationConfig.class);
		doReturn(serializationConfig).when(objectMapper).getSerializationConfig();

		AnnotationIntrospector existingAnnotationIntrospector = mock(AnnotationIntrospector.class);
		doReturn(existingAnnotationIntrospector).when(serializationConfig).getAnnotationIntrospector();

		SensitiveAnnotationIntrospector sensitiveAnnotationIntrospector = mock(SensitiveAnnotationIntrospector.class);

		ArgumentCaptor<AnnotationIntrospectorPair> captor = ArgumentCaptor.forClass(AnnotationIntrospectorPair.class);
		doReturn(objectMapper).when(objectMapper).setAnnotationIntrospector(captor.capture());

		ObjectMapper result = JacksonJsonBuilder.configureSensitivity(objectMapper, sensitiveAnnotationIntrospector);

		assertThat(result, equalTo(objectMapper));

		AnnotationIntrospectorPair pair = captor.getValue();
		List<AnnotationIntrospector> introspectors = new ArrayList<>();
		pair.allIntrospectors(introspectors);

		verify(sensitiveAnnotationIntrospector).allIntrospectors(introspectors);
		verify(existingAnnotationIntrospector).allIntrospectors(introspectors);
	}

	@Test
	void shouldReturnToStringResultIfSerializationFails() throws JsonProcessingException {
		JacksonJsonBuilder jacksonJsonBuilder = new JacksonJsonBuilder();

		ObjectMapper objectMapper = mock(ObjectMapper.class);
		Fields.IgnoreAccess.set(jacksonJsonBuilder, "objectMapper", objectMapper);

		ObjectWriter writer = mock(ObjectWriter.class);
		doReturn(writer).when(objectMapper).writerFor(any(Class.class));

		JsonProcessingException jsonException = new JsonMappingException(null, EXPECTED_EXCEPTION_MESSAGE);
		doThrow(jsonException).when(writer).writeValueAsString(any());

		C c = new C();
		c.setName(SOME_NAME);

		String expectedString = "{ \"hash\":\"" + C.class.getName() + "@"
				+ Integer.toHexString(c.hashCode()) + "\" }";

		String result = jacksonJsonBuilder.toJsonString(c);

		assertThat(result, equalTo(expectedString));
	}

	@Test
	void shouldReturnNullWhenDeserializingStringJsonFailsWithClass() {
		A result = jsonBuilder.fromJsonString(SOME_INVALID_JSON_STRING, A.class);

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenDeserializingStringJsonFailsWithTypeReference() {
		List<A> result = jsonBuilder.fromJsonString(SOME_INVALID_JSON_STRING, new TypeReference<List<A>>() {
			// empty
		});

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenDeserializingStringJsonFailsWithGenericClass() {
		List<A> result = jsonBuilder.fromJsonString(SOME_INVALID_JSON_STRING, new GenericClass<List<A>>() {
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
		List<A> result = jsonBuilder.fromJsonBytes(SOME_INVALID_JSON_BYTES, new TypeReference<List<A>>() {
			// empty
		});

		assertThat(result, equalTo(null));
	}

	@Test
	void shouldReturnNullWhenDeserializingBytesJsonFailsWithGenericClass() {
		List<A> result = jsonBuilder.fromJsonBytes(SOME_INVALID_JSON_BYTES, new GenericClass<List<A>>() {
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
}
