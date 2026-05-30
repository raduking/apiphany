package org.apiphany.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apiphany.security.MessageDigestAlgorithm;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JsonObservability}.
 *
 * @author Radu Sebastian LAZIN
 */
class JsonObservabilityTest {

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
}
