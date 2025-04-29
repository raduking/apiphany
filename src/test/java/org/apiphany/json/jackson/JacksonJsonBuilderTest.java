package org.apiphany.json.jackson;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.morphix.lang.function.Consumers;

/**
 * Test class for {@link JacksonJsonBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class JacksonJsonBuilderTest {

	private JacksonJsonBuilder jsonBuilder = new JacksonJsonBuilder();

	@Test
	void shouldBuildFromPropertiesMapWhenAllKeysAreKebabCase() {
		Map<String, Object> props = Map.of(
				"elements", Map.of(
						"customer-one", Map.of("customer-id", "cid1", "tenant-id", "tid1"),
						"customer-two", Map.of("customer-id", "cid2", "tenant-id", "tid2")
				)
		);

		A result = jsonBuilder.fromPropertiesMap(props, A.class, Consumers.noConsumer());

		assertThat(result.getElements().get("customer-one").customerId, equalTo("cid1"));
		assertThat(result.getElements().get("customer-one").tenantId, equalTo("tid1"));
		assertThat(result.getElements().get("customer-two").customerId, equalTo("cid2"));
		assertThat(result.getElements().get("customer-two").tenantId, equalTo("tid2"));
	}

	@Test
	void shouldBuildFromPropertiesMapByKeepingNonKebabStringKeysForMaps() {
		Map<String, Object> props = Map.of(
				"elements", Map.of(
						"customerOne", Map.of("customer-id", "cid1", "tenant-id", "tid1"),
						"customerTwo", Map.of("customer-id", "cid2", "tenant-id", "tid2")
				)
		);

		A result = jsonBuilder.fromPropertiesMap(props, A.class, Consumers.noConsumer());

		assertThat(result.getElements().get("customerOne").customerId, equalTo("cid1"));
		assertThat(result.getElements().get("customerOne").tenantId, equalTo("tid1"));
		assertThat(result.getElements().get("customerTwo").customerId, equalTo("cid2"));
		assertThat(result.getElements().get("customerTwo").tenantId, equalTo("tid2"));
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
