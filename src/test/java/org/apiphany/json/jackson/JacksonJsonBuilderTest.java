package org.apiphany.json.jackson;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.GenericClass;

import com.fasterxml.jackson.core.type.TypeReference;

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

	private JacksonJsonBuilder jsonBuilder = new JacksonJsonBuilder();

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
	void shouldTransformObjectToJsonAndReadItBack() {
		A a1 = new A();
		Map<String, B> elements = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));
		a1.setElements(elements);

		Object json1 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(a1));

		A a2 = JacksonJsonBuilder.fromJson(json1, A.class);

		Object json2 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(a2));

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
	void shouldTransformGenericObjectToJsonAndReadItBack() {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		Object json1 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements1));

		Map<String, B> elements2 = JacksonJsonBuilder.fromJson(json1, new GenericClass<Map<String, B>>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements2));

		assertThat(json1, equalTo(json2));
	}

	@Test
	void shouldTransformGenericObjectTypeReferenceToJsonAndReadItBack() {
		Map<String, B> elements1 = Map.of(CUSTOMER_ONE, new B(CUSTOMER_ID1, TENANT_ID1));

		Object json1 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements1));

		Map<String, B> elements2 = JacksonJsonBuilder.fromJson(json1, new TypeReference<Map<String, B>>() {
			// empty
		});

		Object json2 = Strings.removeAllWhitespace(JacksonJsonBuilder.toJson(elements2));

		assertThat(json1, equalTo(json2));
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
}
