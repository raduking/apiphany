package org.apiphany.lang;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test class for {@link Strings}.
 *
 * @author Radu Sebastian LAZIN
 */
class StringsTest {

	private static final String TEST_STRING = "someString";
	private static final Integer TEST_INTEGER = 10;
	private static final String TEST_INTEGER_STRING = TEST_INTEGER.toString();
	private static final String BLANK_STRING = "     ";
	private static final String NOT_BLANK_STRING = "   a   ";

	private static final String CAMEL_SOME_COOL_NAME = "someCoolName";
	private static final String KEBAB_SOME_COOL_NAME = "some-cool-name";
	private static final String SNAKE_SOME_COOL_NAME = "some_cool_name";

	private static final String TEXT_FILE_CONTENT = """
			This is line one
			and this is line two""";

	/**
	 * Tests for:
	 * <ul>
	 * <li>{@link Strings#safeToString(Object)}</li>
	 * </ul>
	 */
	@Nested
	class SafeToStringTests {

		@Test
		void shouldSafelyCallToStringOnANullObject() {
			String result = Strings.safeToString(null);

			assertThat(result, nullValue());
		}

		@Test
		void shouldSafelyCallToStringOnAnObject() {
			String result = Strings.safeToString(TEST_INTEGER);

			assertThat(result, equalTo(TEST_INTEGER_STRING));
		}

		@Test
		void shouldSafelyCallToStringOnAStringObject() {
			String result = Strings.safeToString(TEST_STRING);

			assertThat(result, equalTo(TEST_STRING));
		}
	}

	/**
	 * Tests for:
	 * <ul>
	 * <li>{@link Strings#safe(String)}</li>
	 * </ul>
	 */
	@Nested
	class SafeTests {

		@Test
		void shouldReturnTheSameStringOnSafeWhenParameterIsNotNull() {
			String result = Strings.safe(TEST_STRING);

			assertThat(result, equalTo(TEST_STRING));
		}

		@Test
		void shouldReturnEmptyStringOnSafeWhenParameterIsNull() {
			String result = Strings.safe(null);

			assertThat(result, equalTo(""));
		}
	}

	/**
	 * Tests for:
	 * <ul>
	 * <li>{@link Strings#isEmpty(String)}</li>
	 * </ul>
	 */
	@Nested
	class IsEmptyTests {

		@Test
		void shouldReturnTrueForNullValueOnIsEmpty() {
			assertTrue(Strings.isEmpty(null));
		}

		@Test
		void shouldReturnTrueForEmptyStringOnIsEmpty() {
			assertTrue(Strings.isEmpty(""));
		}

		@Test
		void shouldReturnFalseForNonEmptyStringOnIsEmpty() {
			assertFalse(Strings.isEmpty(TEST_STRING));
		}
	}

	/**
	 * Tests for:
	 * <ul>
	 * <li>{@link Strings#isNotEmpty(String)}</li>
	 * </ul>
	 */
	@Nested
	class IsNotEmptyTests {

		@Test
		void shouldReturnFalseForNullValueOnIsNotEmpty() {
			assertFalse(Strings.isNotEmpty(null));
		}

		@Test
		void shouldReturnFalseForEmptyStringOnIsNotEmpty() {
			assertFalse(Strings.isNotEmpty(""));
		}

		@Test
		void shouldReturnTrueForNonEmptyStringOnIsNotEmpty() {
			assertTrue(Strings.isNotEmpty(TEST_STRING));
		}
	}

	/**
	 * Tests for:
	 * <ul>
	 * <li>{@link Strings#isBlank(String)}</li>
	 * </ul>
	 */
	@Nested
	class IsBlankTests {

		@Test
		void shouldReturnTrueForStringWithOnlySpacesOnIsBlank() {
			assertTrue(Strings.isBlank(BLANK_STRING));
		}

		@Test
		void shouldReturnFalseForStringWithNonSpaceCharactersOnIsBlank() {
			assertFalse(Strings.isBlank(NOT_BLANK_STRING));
		}

		@Test
		void shouldReturnTrueForNullValueOnIsBlank() {
			assertTrue(Strings.isBlank(null));
		}
	}

	/**
	 * Tests for:
	 * <ul>
	 * <li>{@link Strings#isNotBlank(String)}</li>
	 * </ul>
	 */
	@Nested
	class IsNotBlankTests {

		@Test
		void shouldReturnFalseForStringWithOnlySpacesOnIsNotBlank() {
			assertFalse(Strings.isNotBlank(BLANK_STRING));
		}

		@Test
		void shouldReturnTrueForStringWithNonSpaceCharactersOnIsNotBlank() {
			assertTrue(Strings.isNotBlank(NOT_BLANK_STRING));
		}

		@Test
		void shouldReturnFalseForNullValueOnIsNotBlank() {
			assertFalse(Strings.isNotBlank(null));
		}
	}

	/**
	 * Tests for:
	 * <ul>
	 * <li>{@link Strings#toString(InputStream, Charset)}</li>
	 * <li>{@link Strings#toString(InputStream, Charset, int)}</li>
	 * <li>{@link Strings#toString(InputStream, Charset, int, Consumer)}</li>
	 * <li>{@link Strings#toString(InputStream, Charset, int, int, Consumer)}</li>
	 * </ul>
	 */
	@Nested
	class ToStringTests {

		private static final String SIMULATED_ERROR_MESSAGE = "Boom: Simulated error";

		@Test
		void shouldReadATextFileWithFileInputStreamAndSpecifiedCharsetAndBuffer() throws IOException {
			try (FileInputStream fileInputStream = new FileInputStream("src/test/resources/text-file.txt")) {
				String result = Strings.toString(fileInputStream, StandardCharsets.UTF_8, 10);

				assertThat(result, equalTo(TEXT_FILE_CONTENT));
			}
		}

		@Test
		void shouldReadATextFileWithFileInputStreamAndSpecifiedCharset() throws IOException {
			try (FileInputStream fileInputStream = new FileInputStream("src/test/resources/text-file.txt")) {
				String result = Strings.toString(fileInputStream, StandardCharsets.UTF_8);

				assertThat(result, equalTo(TEXT_FILE_CONTENT));
			}
		}

		@Test
		void shouldDelegateErrorToOnErrorConsumerWhenToStringWithInputStreamThrowsException() {
			Runnable runnable = mock(Runnable.class);
			Consumer<Exception> onError = onErrorHandler(runnable, IOException.class, SIMULATED_ERROR_MESSAGE);
			@SuppressWarnings("resource")
			InputStream throwingStream = new InputStream() {
				@Override
				public int read() throws IOException {
					throw new IOException(SIMULATED_ERROR_MESSAGE);
				}
			};

			String result = Strings.toString(throwingStream, StandardCharsets.UTF_8, 10, onError);

			assertThat(result, nullValue());
			verify(runnable).run();
		}

		@Test
		void shouldDelegateErrorToOnErrorConsumerWhenToStringWithInputStreamAndSizeThrowsException() throws IOException {
			int maxSize = 10;
			Runnable runnable = mock(Runnable.class);
			Consumer<Exception> onError = onErrorHandler(runnable, IOException.class, "Input stream exceeds maximum size of " + maxSize + " bytes");

			String result;
			try (FileInputStream fileInputStream = new FileInputStream("src/test/resources/text-file.txt")) {
				result = Strings.toString(fileInputStream, StandardCharsets.UTF_8, maxSize, 10, onError);
			}

			assertThat(result, nullValue());
			verify(runnable).run();
		}

		@Test
		void shouldDelegateErrorToOnErrorConsumerWhenMaxSizeIsNegative() throws IOException {
			Runnable runnable = mock(Runnable.class);
			Consumer<Exception> onError = onErrorHandler(runnable, IllegalArgumentException.class, "Maximum size must be strictly positive");

			String result;
			try (FileInputStream fileInputStream = new FileInputStream("src/test/resources/text-file.txt")) {
				result = Strings.toString(fileInputStream, StandardCharsets.UTF_8, -1, 10, onError);
			}

			assertThat(result, nullValue());
			verify(runnable).run();
		}

		@Test
		void shouldDelegateErrorToOnErrorConsumerWhenBufferSizeIsNegative() throws IOException {
			Runnable runnable = mock(Runnable.class);
			Consumer<Exception> onError = onErrorHandler(runnable, IllegalArgumentException.class, "Buffer size must be strictly positive");

			String result;
			try (FileInputStream fileInputStream = new FileInputStream("src/test/resources/text-file.txt")) {
				result = Strings.toString(fileInputStream, StandardCharsets.UTF_8, 10, -1, onError);
			}

			assertThat(result, nullValue());
			verify(runnable).run();
		}

		private static Consumer<Exception> onErrorHandler(final Runnable runnable,
				final Class<? extends Exception> expectedException, final String expectedMessage) {
			return e -> {
				runnable.run();
				assertThat(e, instanceOf(expectedException));
				assertThat(e.getMessage(), equalTo(expectedMessage));
			};
		}
	}

	/**
	 * Tests for:
	 * <ul>
	 * <li>{@link Strings#fromFile(String)}</li>
	 * <li>{@link Strings#fromFile(String, Charset, int)}</li>
	 * <li>{@link Strings#fromFile(String, Consumer)}</li>
	 * <li>{@link Strings#fromFile(String, Charset, int, Consumer)}</li>
	 * </ul>
	 */
	@Nested
	class FromFileTests {

		@Test
		void shouldReturnStringFromFile() {
			String result = Strings.fromFile("text-file.txt", StandardCharsets.UTF_8, 1000);

			assertThat(result, equalTo(TEXT_FILE_CONTENT));
		}

		@Test
		void shouldReturnStringFromAbsolutePathFile() {
			String currentDir = Paths.get("").toAbsolutePath().toString();
			String result = Strings.fromFile(currentDir + "/src/test/resources/text-file.txt");

			assertThat(result, equalTo(TEXT_FILE_CONTENT));
		}

		@Test
		void shouldDelegateErrorToOnErrorConsumerWhenFromStringThrowsExceptionWhenCalledWithNameCharsetSize() {
			Runnable runnable = mock(Runnable.class);
			Consumer<Exception> onError = e -> {
				runnable.run();
				assertThat(e, instanceOf(FileNotFoundException.class));
			};

			String result = Strings.fromFile("/unknown-file.txt", StandardCharsets.UTF_8, 10, onError);

			assertThat(result, nullValue());
			verify(runnable).run();
		}

		@Test
		void shouldDelegateErrorToOnErrorConsumerWhenFromStringThrowsExceptionWhenCalledWithName() {
			Runnable runnable = mock(Runnable.class);
			Consumer<Exception> onError = e -> {
				runnable.run();
				assertThat(e, instanceOf(FileNotFoundException.class));
			};

			String result = Strings.fromFile("/unknown-file.txt", onError);

			assertThat(result, nullValue());
			verify(runnable).run();
		}

		@Test
		void shouldDelegateErrorToOnErrorConsumerWhenFromStringThrowsExceptionWhenCalledWithClasspathPath() {
			Runnable runnable = mock(Runnable.class);
			Consumer<Exception> onError = e -> {
				runnable.run();
				assertThat(e, instanceOf(FileNotFoundException.class));
				assertThat(e.getMessage(), equalTo("Classpath resource not found: unknown-file.txt"));
			};

			String result = Strings.fromFile("unknown-file.txt", onError);

			assertThat(result, nullValue());
			verify(runnable).run();
		}

		@Test
		void shouldReturnStringFromFileWithOnlyPathParameter() {
			String result = Strings.fromFile("text-file.txt");

			assertThat(result, equalTo(TEXT_FILE_CONTENT));
		}
	}

	@Test
	void shouldEnvelopeStrings() {
		String result = Strings.envelope(TEST_INTEGER_STRING, TEST_STRING);

		assertThat(result, equalTo(TEST_INTEGER_STRING + TEST_STRING + TEST_INTEGER_STRING));
	}

	@Test
	void shouldTransformKebabToLowerCamelCaseWhenTheStringIsKebabCase() {
		String result = Strings.fromKebabToLowerCamelCase(KEBAB_SOME_COOL_NAME);

		assertThat(result, equalTo(CAMEL_SOME_COOL_NAME));
	}

	@Test
	void shouldTransformKebabToCamelCaseWhenTheStringIsKebabCase() {
		String result = Strings.fromKebabToCamelCase(KEBAB_SOME_COOL_NAME);

		assertThat(result, equalTo(CAMEL_SOME_COOL_NAME));
	}

	@Test
	void shouldTransformLowerCamelToKebabCaseWhenTheStringIsKebabCase() {
		String result = Strings.fromLowerCamelToKebabCase(CAMEL_SOME_COOL_NAME);

		assertThat(result, equalTo(KEBAB_SOME_COOL_NAME));
	}

	@Test
	void shouldTransformCamelToSnakeCaseWhenTheStringIsKebabCase() {
		String result = Strings.fromCamelToKebabCase(CAMEL_SOME_COOL_NAME);

		assertThat(result, equalTo(KEBAB_SOME_COOL_NAME));
	}

	@Test
	void shouldTransformLowerCamelToSnakeCaseWhenTheStringIsKebabCase() {
		String result = Strings.fromLowerCamelToSnakeCase(CAMEL_SOME_COOL_NAME);

		assertThat(result, equalTo(SNAKE_SOME_COOL_NAME));
	}

	@Test
	void shouldTransformCamelToKebabCaseWhenTheStringIsKebabCase() {
		String result = Strings.fromCamelToSnakeCase(CAMEL_SOME_COOL_NAME);

		assertThat(result, equalTo(SNAKE_SOME_COOL_NAME));
	}

	@ParameterizedTest
	@MethodSource("provideValuesForStripChar")
	void shouldStripCharacterFromString(final String input, final char charToStrip, final String expectedOutput) {
		String result = Strings.stripChar(input, charToStrip);

		assertThat(result, equalTo(expectedOutput));
	}

	private static Stream<Arguments> provideValuesForStripChar() {
		return Stream.of(
				Arguments.of(null, '-', null),
				Arguments.of("", '-', ""),
				Arguments.of("a", '-', "a"),
				Arguments.of("----", '-', ""),
				Arguments.of("-a-", '-', "a"),
				Arguments.of("a-a", '-', "a-a"),
				Arguments.of("---some-text---", '-', "some-text"));
	}

	@ParameterizedTest
	@MethodSource("provideValuesForRemoveWhitespaces")
	void shouldRemoveWhitespacesFromString(final String input, final String expectedOutput) {
		String result = Strings.removeAllWhitespace(input);

		assertThat(result, equalTo(expectedOutput));
	}

	private static Stream<Arguments> provideValuesForRemoveWhitespaces() {
		return Stream.of(
				Arguments.of(null, null),
				Arguments.of("", ""),
				Arguments.of("     ", ""),
				Arguments.of(" a b c ", "abc"),
				Arguments.of("some text with spaces", "sometextwithspaces"),
				Arguments.of("some\ttext\nwith\rdifferent\twhitespaces", "sometextwithdifferentwhitespaces"));
	}
}
