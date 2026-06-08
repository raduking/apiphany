package org.apiphany.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apiphany.lang.Strings;
import org.apiphany.security.MessageDigestAlgorithm;
import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Unit tests for {@link JsonObservability}.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonObservabilityTest {

	private static final long TEST_LONG = 666L;

	@Test
	void shouldThrowExceptionWhenTryingToInstantiateErrorMessageNestedClass() {
		UnsupportedOperationException e = Assertions.assertDefaultConstructorThrows(JsonObservability.ErrorMessage.class);

		assertThat(e.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldReturnTheDebugStringForObjectsWithId() {
		B b = new B(TEST_LONG);

		String result = JsonObservability.toDebugJsonString(b);

		String expected =
				"{ \"type\":\"" + B.class.getCanonicalName() + "\", \"id\":\"" + TEST_LONG + "\", \"identity\":\"" + Strings.identityHashCode(b)
						+ "\" }";

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTheDebugString() {
		Object o = new Object();

		String result = JsonObservability.toDebugJsonString(o);

		String expected = "{ \"type\":\"" + Object.class.getCanonicalName() + "\", \"identity\":\"" + Strings.identityHashCode(o) + "\" }";

		assertThat(result, equalTo(expected));
	}

	@Test
	void shouldReturnTheDebugStringForNullObjects() {
		String o = null;

		String result = JsonObservability.toDebugJsonString(o);

		String expected = "{ \"type\":null, \"identity\":" + Strings.identityHashCode(o) + " }";

		assertThat(result, equalTo(expected));
	}

	@Nested
	class DescribeJsonInputTests {

		@Test
		void shouldDescribeNullInput() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(false);

			String result = runtime.observability().describeJsonInput((Object) null);

			assertThat(result, equalTo("null"));
		}

		@Test
		void shouldDescribeStringInputWithoutPreviewWhenDebugDisabled() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(false);
			String input = "abc";

			String result = runtime.observability().describeJsonInput(input);

			String hash = MessageDigestAlgorithm.SHA256.hash(input, 8);
			assertThat(result, equalTo(String.class.getTypeName() + "(length=3, hash=" + hash + ")"));
		}

		@Test
		void shouldDescribeStringInputWithPreviewWhenDebugEnabled() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(true);
			String input = "abc";

			String result = runtime.observability().describeJsonInput(input);

			String hash = MessageDigestAlgorithm.SHA256.hash(input, 8);
			assertThat(result, equalTo(String.class.getTypeName() + "(length=3, hash=" + hash + ", preview=abc)"));
		}

		@Test
		void shouldDescribeByteArrayInputWithoutPreviewWhenDebugDisabled() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(false);
			byte[] input = "abc".getBytes(StandardCharsets.UTF_8);

			String result = runtime.observability().describeJsonInput(input);

			String hash = MessageDigestAlgorithm.SHA256.hash(input, 8);
			assertThat(result, equalTo("byte[](length=3, hash=" + hash + ")"));
		}

		@Test
		void shouldDescribeByteArrayInputWithPreviewWhenDebugEnabled() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(true);
			byte[] input = "abc".getBytes(StandardCharsets.UTF_8);

			String result = runtime.observability().describeJsonInput(input);

			String hash = MessageDigestAlgorithm.SHA256.hash(input, 8);
			assertThat(result, equalTo("byte[](length=3, hash=" + hash + ", preview=abc)"));
		}

		@Test
		void shouldDescribeInputStreamInput() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(false);
			ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);

			String result = runtime.observability().describeJsonInput(input);

			assertThat(result, equalTo(ByteArrayInputStream.class.getTypeName()
					+ "(hash=" + Integer.toHexString(System.identityHashCode(input)) + ")"));
		}

		@Test
		void shouldDescribeObjectInput() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(false);
			Object input = new Object();

			String result = runtime.observability().describeJsonInput(input);

			assertThat(result, equalTo(Object.class.getTypeName()
					+ "(hash=" + Integer.toHexString(System.identityHashCode(input)) + ")"));
		}

		@Test
		void shouldDescribeObjectInputWithDebugStringEnabled() {
			JsonBuilder runtime = newJsonBuilderWithDebugString(true);
			Object input = new Object();

			String result = runtime.observability().describeJsonInput(input);

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
}
