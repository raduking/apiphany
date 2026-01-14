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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

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

	private static final String TEXT_FILE_CONTENT = """
			This is line one
			and this is line two""";

	@Test
	void shouldSafelyCallToStringOnAnObject() {
		Integer i = TEST_INTEGER;

		String result = Strings.safeToString(i);

		assertThat(result, equalTo(TEST_INTEGER_STRING));
	}

	@Test
	void shouldReturnNnullWhenToStringCallenOnAnNullObject() {
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
	void shouldReadATextFileWithFileInputStream() throws IOException {
		try (FileInputStream fileInputStream = new FileInputStream("src/test/resources/text-file.txt")) {
			String result = Strings.toString(fileInputStream, StandardCharsets.UTF_8, 10);

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
		String result = Strings.fromFile("/text-file.txt", StandardCharsets.UTF_8, 1000);

		assertThat(result, equalTo(TEXT_FILE_CONTENT));
	}

	@Test
	void shouldDelegateErrorToOnErrorConsumerWhenFromStringThrowsExceptionWhenCalledWithNameCharsetSize() {
		Runnable runnable = mock(Runnable.class);
		Consumer<Exception> onError = e -> {
			runnable.run();
			assertThat(e, instanceOf(NullPointerException.class));
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
			assertThat(e, instanceOf(NullPointerException.class));
		};

		String result = Strings.fromFile("/unknown-file.txt", onError);

		assertThat(result, nullValue());
		verify(runnable).run();
	}

	@Test
	void shouldReturnStringFromFileWithOnlyPathParameter() {
		String result = Strings.fromFile("/text-file.txt");

		assertThat(result, equalTo(TEXT_FILE_CONTENT));
	}

	@Test
	void shouldEnvelopeStrings() {
		String result = Strings.envelope(TEST_INTEGER_STRING, TEST_STRING);

		assertThat(result, equalTo(TEST_INTEGER_STRING + TEST_STRING + TEST_INTEGER_STRING));
	}

	@Test
	void shouldTransformKebabToLowerCamelCaseWhenTheStringIsKebabCase() {
		String text = KEBAB_SOME_COOL_NAME;

		String result = Strings.fromKebabToLowerCamelCase(text);

		assertThat(result, equalTo(CAMEL_SOME_COOL_NAME));
	}

	@Test
	void shouldTransformKebabToCamelCaseWhenTheStringIsKebabCase() {
		String text = KEBAB_SOME_COOL_NAME;

		String result = Strings.fromKebabToCamelCase(text);

		assertThat(result, equalTo(CAMEL_SOME_COOL_NAME));
	}

	@Test
	void shouldTransformLowerCamelToKebabCaseWhenTheStringIsKebabCase() {
		String text = CAMEL_SOME_COOL_NAME;

		String result = Strings.fromLowerCamelToKebabCase(text);

		assertThat(result, equalTo(KEBAB_SOME_COOL_NAME));
	}

	@Test
	void shouldTransformCamelToKebabCaseWhenTheStringIsKebabCase() {
		String text = CAMEL_SOME_COOL_NAME;

		String result = Strings.fromCamelToKebabCase(text);

		assertThat(result, equalTo(KEBAB_SOME_COOL_NAME));
	}
}
