package org.apiphany.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.GenericClass;

/**
 * Test class for {@link JsonBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonBuilderTest {

	private static final String CUSTOMER_ID1 = "cid1";
	private static final String TENANT_ID1 = "tid1";

	private JsonBuilder jsonBuilder = new JsonBuilder();

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
	void shouldThrowExceptionOnToPropertiesMap() {
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> jsonBuilder.toPropertiesMap(null, null));

		assertThat(e.getMessage(), equalTo(JsonBuilder.ErrorMessage.JSON_LIBRARY_NOT_FOUND));
	}

	@Test
	void shouldThrowExceptionOnFromPropertiesMap() {
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> jsonBuilder.fromPropertiesMap(null, null, null));

		assertThat(e.getMessage(), equalTo(JsonBuilder.ErrorMessage.JSON_LIBRARY_NOT_FOUND));
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
	void shouldThrowExceptionWhenReadingJsonObjectWithAnUnsupportedType() {
		Object o = new Object();
		UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
				() -> JsonBuilder.fromJson(o, A.class));

		assertThat(e.getMessage(), equalTo("Unsupported JSON input type: " + Object.class));
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
}
