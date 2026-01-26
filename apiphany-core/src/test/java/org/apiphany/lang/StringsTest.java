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
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

	private static final String SIMULATED_ERROR = "Simulated error";
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

	@Test
	void shouldSafelyCallToStringOnAnObject() {
		String result = Strings.safeToString(TEST_INTEGER);

		assertThat(result, equalTo(TEST_INTEGER_STRING));
	}

	@Test
	void shouldReturnNullWhenToStringCalledOnAnNullObject() {
		String result = Strings.safeToString(null);

		assertThat(result, nullValue());
	}

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
		Consumer<Exception> onError = e -> {
			runnable.run();
			assertThat(e, instanceOf(IOException.class));
			assertThat(e.getMessage(), equalTo(SIMULATED_ERROR));
		};
		@SuppressWarnings("resource")
		InputStream throwingStream = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException(SIMULATED_ERROR);
			}
		};

		String result = Strings.toString(throwingStream, StandardCharsets.UTF_8, 10, onError);

		assertThat(result, nullValue());
		verify(runnable).run();
	}

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
			assertThat(e, instanceOf(NoSuchFileException.class));
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
			assertThat(e, instanceOf(NoSuchFileException.class));
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
