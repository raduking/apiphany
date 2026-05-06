package org.apiphany.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ClientProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClientPropertiesTest {

	private static final String ROOT = "ROOT";

	@Test
	void shouldHaveRootAsRootProperty() {
		assertThat(ClientProperties.CUSTOM_PROPERTIES_PREFIX_FIELD_NAME, equalTo(ROOT));
	}

	@Test
	void shouldReadClientPropertiesFromJson() {
		String clientPropertiesJson = Strings.fromFile("client/client-properties.json");
		String json = Strings.removeAllWhitespace(clientPropertiesJson);

		ClientProperties clientProperties1 = JsonBuilder.fromJson(json, ClientProperties.class);

		assertThat(clientProperties1, notNullValue());

		String result = clientProperties1.toString();

		ClientProperties clientProperties2 = JsonBuilder.fromJson(result, ClientProperties.class);
		String expected = clientProperties2.toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReadNestedClientProperties() {
		String clientPropertiesJson = Strings.fromFile("client/client-properties.json");
		String json = Strings.removeAllWhitespace(clientPropertiesJson);

		ClientProperties clientProperties = JsonBuilder.fromJson(json, ClientProperties.class);
		ClientProperties nestedClientProperties1 = clientProperties.getClientProperties("", "some-client", ClientProperties.class);

		assertThat(nestedClientProperties1, notNullValue());

		String result = nestedClientProperties1.toString();

		ClientProperties nestedClientProperties2 = JsonBuilder.fromJson(result, ClientProperties.class);
		String expected = nestedClientProperties2.toString();

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldSetEnabledFlagToTrue() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setEnabled(true);

		assertThat(clientProperties.isEnabled(), equalTo(true));
		assertThat(clientProperties.isDisabled(), equalTo(false));
	}

	@Test
	void shouldSetEnabledFlagToFalse() {
		ClientProperties clientProperties = new ClientProperties();
		clientProperties.setEnabled(false);

		assertThat(clientProperties.isEnabled(), equalTo(false));
		assertThat(clientProperties.isDisabled(), equalTo(true));
	}

	@Nested
	class NestedClientPropertiesTests {

		@Test
		void shouldReturnObjectForNonExistingNestedClientProperties() {
			ClientProperties clientProperties = new ClientProperties();

			ClientProperties nestedClientProperties =
					clientProperties.getClientProperties("non-existing-root", "non-existing-client", ClientProperties.class);

			assertThat(nestedClientProperties, not(equalTo(null)));
		}

		@Test
		void shouldReturnNestedClientPropertiesForExistingNestedClientPropertiesWithCorrectType() {
			ClientProperties clientProperties = new ClientProperties();
			ClientProperties expected = new ClientProperties();
			expected.setEnabled(true);
			clientProperties.setClientProperties("some-root", "some-client", expected);

			ClientProperties actual = clientProperties.getClientProperties("some-root", "some-client", ClientProperties.class);

			assertThat(actual.getClass(), equalTo(expected.getClass()));
			assertThat(actual.isEnabled(), equalTo(expected.isEnabled()));
			assertThat(actual.toString(), equalTo(expected.toString()));
		}

		@Test
		void shouldThrowExceptionWhenTryingToSetClientPropertiesWithEmptyPaths() {
			ClientProperties clientProperties = new ClientProperties();
			ClientProperties nested = new ClientProperties();
			IllegalStateException e =
					assertThrows(IllegalStateException.class, () -> clientProperties.setClientProperties("", "", nested));

			assertThat(e.getMessage(), equalTo("Error writing properties for path: '.'"));
		}
	}

	@Nested
	class CustomPropertiesTests {

		private static final String SOME_NAME = "some-name";

		@Test
		void shouldReturnNullForNonExistingCustomPropertiesRoot() {
			ClientProperties clientProperties = new ClientProperties();

			CustomProperties customProperties = clientProperties.getCustomProperties("non-existing-root", CustomProperties.class);

			assertThat(customProperties, equalTo(null));
		}

		@Test
		void shouldReturnNullForExistingCustomPropertiesRootWithWrongType() {
			ClientProperties clientProperties = new ClientProperties();
			CustomProperties customProperties = new CustomProperties();
			customProperties.setKey1("value1");
			clientProperties.setCustomProperties(SOME_NAME, customProperties);

			String result = clientProperties.getCustomProperties(SOME_NAME, String.class);

			assertThat(result, equalTo(null));
		}

		@Test
		void shouldReturnNullAndCallOnErrorForExistingCustomPropertiesRootWithWrongType() {
			ClientProperties clientProperties = new ClientProperties();
			CustomProperties customProperties = new CustomProperties();
			customProperties.setKey1("value1");
			clientProperties.setCustomProperties(SOME_NAME, customProperties);

			AtomicInteger onErrorCalled = new AtomicInteger(0);
			Consumer<Exception> onError = e -> {
				onErrorCalled.incrementAndGet();
				assertThat(e, notNullValue());
			};

			String result = clientProperties.getCustomProperties(SOME_NAME, String.class, onError);

			assertThat(result, equalTo(null));
			assertThat(onErrorCalled.get(), equalTo(1));
		}

		@Test
		void shouldReturnCustomPropertiesForExistingCustomPropertiesWithCorrectType() {
			ClientProperties clientProperties = new ClientProperties();
			CustomProperties expected = new CustomProperties();
			expected.setKey1("value1");
			expected.setKey2("value2");
			clientProperties.setCustomProperties(SOME_NAME, expected);

			CustomProperties actual = clientProperties.getCustomProperties(SOME_NAME, CustomProperties.class);

			assertThat(actual.getClass(), equalTo(expected.getClass()));
			assertThat(actual.getKey1(), equalTo(expected.getKey1()));
			assertThat(actual.getKey2(), equalTo(expected.getKey2()));
			assertThat(actual.toString(), equalTo(expected.toString()));
		}
	}

	@Nested
	class EqualsAndHashCodeTests {

		@Test
		void shouldBeEqualToItself() {
			ClientProperties clientProperties = new ClientProperties();

			assertThat(clientProperties, equalTo(clientProperties));
		}

		@Test
		void shouldNotBeEqualToNull() {
			ClientProperties clientProperties = new ClientProperties();

			assertThat(clientProperties, not(equalTo(null)));
		}

		@Test
		void shouldNotBeEqualToClientPropertiesWithDifferentCustomPropertiesRoots() {
			ClientProperties clientProperties1 = new ClientProperties();
			ClientProperties clientProperties2 = new ClientProperties();

			CustomProperties customProperties1 = new CustomProperties();
			CustomProperties customProperties2 = new CustomProperties();

			clientProperties1.setCustomProperties("key1", customProperties1);
			clientProperties2.setCustomProperties("key2", customProperties2);

			assertThat(clientProperties1, not(equalTo(clientProperties2)));
		}

		@Test
		void shouldNotBeEqualToClientPropertiesWithDifferentCustomProperties() {
			ClientProperties clientProperties1 = new ClientProperties();
			ClientProperties clientProperties2 = new ClientProperties();

			CustomProperties customProperties1 = new CustomProperties();
			customProperties1.setKey1("value1");
			CustomProperties customProperties2 = new CustomProperties();
			customProperties2.setKey1("value2");

			clientProperties1.setCustomProperties("key", customProperties1);
			clientProperties2.setCustomProperties("key", customProperties2);

			assertThat(clientProperties1, not(equalTo(clientProperties2)));
		}

		@Test
		void shouldBeEqualToClientPropertiesWithSameCustomProperties() {
			ClientProperties clientProperties1 = new ClientProperties();
			ClientProperties clientProperties2 = new ClientProperties();

			CustomProperties customProperties1 = new CustomProperties();
			customProperties1.setKey1("value1");
			customProperties1.setKey2("value2");
			CustomProperties customProperties2 = new CustomProperties();
			customProperties2.setKey1("value1");
			customProperties2.setKey2("value2");

			clientProperties1.setCustomProperties("key", customProperties1);
			clientProperties2.setCustomProperties("key", customProperties2);

			assertThat(clientProperties1, equalTo(clientProperties2));
		}

		@Test
		void shouldHaveSameHashCodeForClientPropertiesWithSameCustomProperties() {
			ClientProperties clientProperties1 = new ClientProperties();
			ClientProperties clientProperties2 = new ClientProperties();

			CustomProperties customProperties1 = new CustomProperties();
			customProperties1.setKey1("value1");
			CustomProperties customProperties2 = new CustomProperties();
			customProperties2.setKey1("value1");

			clientProperties1.setCustomProperties("key", customProperties1);
			clientProperties2.setCustomProperties("key", customProperties2);

			assertThat(clientProperties1.hashCode(), equalTo(clientProperties2.hashCode()));
		}
	}

	static class CustomProperties {

		private String key1;
		private String key2;

		@Override
		public String toString() {
			return JsonBuilder.toJson(this);
		}

		public String getKey1() {
			return key1;
		}

		public void setKey1(final String key1) {
			this.key1 = key1;
		}

		public String getKey2() {
			return key2;
		}

		public void setKey2(final String key2) {
			this.key2 = key2;
		}
	}
}
