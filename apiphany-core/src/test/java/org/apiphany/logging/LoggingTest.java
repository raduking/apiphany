package org.apiphany.logging;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apiphany.lang.Strings;
import org.apiphany.security.MessageDigestAlgorithm;
import org.apiphany.test.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link Logging}.
 *
 * @author Radu Sebastian LAZIN
 */
class LoggingTest {

	@Test
	void shouldThrowExceptionOnInstantiatingIncludeDefault() {
		UnsupportedOperationException exception = Assertions.assertDefaultConstructorThrows(Logging.Include.Default.class);

		assertThat(exception.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Nested
	class IncludeTests {

		@Test
		void shouldReturnLengthForString() {
			String result = Logging.Include.LENGTH.getValue("abc");

			assertThat(result, equalTo("3"));
		}

		@Test
		void shouldReturnLengthForBytes() {
			String result = Logging.Include.LENGTH.getValue(new byte[] { 1, 2, 3 });

			assertThat(result, equalTo("3"));
		}

		@Test
		void shouldReturnUnavailableLengthForNonStringAndNonBytes() {
			String result = Logging.Include.LENGTH.getValue(new Object());

			assertThat(result, equalTo(Logging.Include.Default.UNAVAILABLE));
		}

		@Test
		void shouldReturnHashForString() {
			String input = "abc";
			String result = Logging.Include.HASH.getValue(input);

			assertThat(result, equalTo(MessageDigestAlgorithm.SHA256.hash(input, Logging.Include.Default.HASH_BYTES)));
		}

		@Test
		void shouldReturnHashForBytes() {
			byte[] input = "abc".getBytes(StandardCharsets.UTF_8);
			String result = Logging.Include.HASH.getValue(input);

			assertThat(result, equalTo(MessageDigestAlgorithm.SHA256.hash(input, Logging.Include.Default.HASH_BYTES)));
		}

		@Test
		void shouldReturnIdentityHashForObject() {
			Object input = new Object();
			String result = Logging.Include.HASH.getValue(input);

			assertThat(result, equalTo(Integer.toHexString(System.identityHashCode(input))));
		}

		@Test
		void shouldReturnPreviewForString() {
			String input = "abc";
			String result = Logging.Include.PREVIEW.getValue(input);

			assertThat(result, equalTo(Strings.preview(input, Logging.Include.Default.PREVIEW_LENGTH)));
		}

		@Test
		void shouldReturnPreviewForBytes() {
			byte[] input = "abc".getBytes(StandardCharsets.UTF_8);
			String result = Logging.Include.PREVIEW.getValue(input);

			assertThat(result, equalTo(Strings.preview(input, Logging.Include.Default.PREVIEW_LENGTH)));
		}

		@Test
		void shouldReturnUnavailablePreviewForUnsupportedType() {
			String result = Logging.Include.PREVIEW.getValue(new Object());

			assertThat(result, equalTo(Logging.Include.Default.UNAVAILABLE));
		}

		@Test
		void shouldBuildIncludeListFromNonNullArgumentsOnly() {
			List<Logging.Include> result = Logging.Include.of(Logging.Include.LENGTH, null, Logging.Include.HASH);

			assertThat(result, equalTo(List.of(Logging.Include.LENGTH, Logging.Include.HASH)));
		}

		@Test
		void shouldThrowExceptionWhenIncludeArgsAreNull() {
			NullPointerException exception = assertThrows(NullPointerException.class,
					() -> Logging.Include.of((Logging.Include[]) null));

			assertThat(exception.getMessage(), equalTo("args must not be null"));
		}

		@Test
		void shouldReturnIncludeWhenConditionIsTrue() {
			Logging.Include result = Logging.Include.when(() -> true, Logging.Include.PREVIEW);

			assertThat(result, equalTo(Logging.Include.PREVIEW));
		}

		@Test
		void shouldReturnNullWhenConditionIsFalse() {
			Logging.Include result = Logging.Include.when(() -> false, Logging.Include.PREVIEW);

			assertThat(result, nullValue());
		}
	}

	@Nested
	class DescribeInputTests {

		@Test
		void shouldDescribeNullInput() {
			String result = Logging.describeInput(null, LoggingFormat.CUSTOM, Logging.Include.LENGTH, Logging.Include.HASH);

			assertThat(result, equalTo("null"));
		}

		@Test
		void shouldDescribeStringWithLengthAndHash() {
			String input = "abc";
			String hash = MessageDigestAlgorithm.SHA256.hash(input, Logging.Include.Default.HASH_BYTES);

			String result = Logging.describeInput(input, LoggingFormat.CUSTOM, Logging.Include.LENGTH, Logging.Include.HASH);

			assertThat(result, equalTo(String.class.getTypeName() + "(length=3, hash=" + hash + ")"));
		}

		@Test
		void shouldDescribeBytesWithLengthHashAndPreview() {
			byte[] input = "abc".getBytes(StandardCharsets.UTF_8);
			String hash = MessageDigestAlgorithm.SHA256.hash(input, Logging.Include.Default.HASH_BYTES);
			String preview = Strings.preview(input, Logging.Include.Default.PREVIEW_LENGTH);

			String result = Logging.describeInput(input, LoggingFormat.CUSTOM,
					Logging.Include.LENGTH, Logging.Include.HASH, Logging.Include.PREVIEW);

			assertThat(result, equalTo("byte[](length=3, hash=" + hash + ", preview=" + preview + ")"));
		}

		@Test
		void shouldSkipUnavailableIncludes() {
			Object input = new Object();
			String identity = Integer.toHexString(System.identityHashCode(input));

			String result = Logging.describeInput(input, LoggingFormat.CUSTOM,
					Logging.Include.LENGTH, Logging.Include.HASH, Logging.Include.PREVIEW);

			assertThat(result, equalTo(Object.class.getTypeName() + "(hash=" + identity + ")"));
		}

		@Test
		void shouldDescribeInputWithEmptyIncludeList() {
			String result = Logging.describeInput("abc", LoggingFormat.CUSTOM, List.of());

			assertThat(result, equalTo(String.class.getTypeName() + "()"));
		}

		@Test
		void shouldIgnoreNullIncludeArguments() {
			String result = Logging.describeInput("abc", LoggingFormat.CUSTOM, Logging.Include.HASH, null);
			String hash = MessageDigestAlgorithm.SHA256.hash("abc", Logging.Include.Default.HASH_BYTES);

			assertThat(result, equalTo(String.class.getTypeName() + "(hash=" + hash + ")"));
		}

		@Test
		void shouldFallbackToCustomFormatWhenFormatIsNotCustom() {
			String input = "abc";
			String hash = MessageDigestAlgorithm.SHA256.hash(input, Logging.Include.Default.HASH_BYTES);

			String result = Logging.describeInput(input, LoggingFormat.HEX, Logging.Include.HASH);

			assertThat(result, equalTo(String.class.getTypeName() + "(hash=" + hash + ")"));
		}
	}
}
