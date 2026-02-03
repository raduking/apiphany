package org.apiphany.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

import org.apiphany.io.IOStreams;
import org.apiphany.io.ResourceLocation;
import org.morphix.lang.function.Consumers;

/**
 * {@link String} utility methods.
 * <p>
 * This class prefers to return {@code null} instead of throwing exceptions when an error occurs, so it is the caller's
 * responsibility to handle the errors by checking for null values and using the provided onError handlers.
 * <p>
 * This design choice is made to provide a more functional programming style and to allow the caller to decide how to
 * handle errors, whether by logging, throwing custom exceptions, or using default values and to avoid the need for
 * try-catch blocks in the calling code, thus promoting cleaner and more readable code. The other major reason is
 * performance, as throwing exceptions can be costly in terms of performance, especially in cases where errors are
 * expected to occur frequently. By returning null, we can avoid the overhead associated with exception handling and
 * allow the caller to handle errors in a more efficient way.
 * <p>
 * However, it is important to note that this approach may lead to null pointer exceptions if the caller does not
 * properly handle the null values, so it is crucial to always check for null returns when using these methods and to
 * use the onError handlers to manage exceptions appropriately.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Strings {

	/**
	 * End Of Line, system-dependent line separator.
	 */
	String EOL = System.lineSeparator();

	/**
	 * Default character set used when working with strings {@link StandardCharsets#UTF_8}.
	 */
	Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/**
	 * Returns the call of {@link Object#toString()} on the given parameter if the parameter is not null, null otherwise.
	 *
	 * @param o object
	 * @return the string representation of the object
	 */
	static String safeToString(final Object o) {
		return Objects.toString(o, null);
	}

	/**
	 * Returns the parameter if it is not null, empty string otherwise.
	 *
	 * @param s a string
	 * @return the parameter if it is not null, empty string otherwise
	 */
	static String safe(final String s) {
		return null != s ? s : "";
	}

	/**
	 * Checks if a CharSequence is empty ("") or null.
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is empty or null
	 */
	static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.isEmpty();
	}

	/**
	 * Checks if a CharSequence is not empty ("") or null.
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is not empty or null
	 */
	static boolean isNotEmpty(final CharSequence cs) {
		return !isEmpty(cs);
	}

	/**
	 * Checks if a CharSequence is whitespace, empty ("") or null.
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is whitespace, empty or null
	 */
	static boolean isBlank(final CharSequence cs) {
		if (isEmpty(cs)) {
			return true;
		}
		int length = cs.length();
		for (int i = 0; i < length; ++i) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a CharSequence is not whitespace, empty ("") or null.
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is not whitespace, empty or null
	 */
	static boolean isNotBlank(final CharSequence cs) {
		return !isBlank(cs);
	}

	/**
	 * Transforms a string from Lower Camel case to Snake case. The Lower Camel case is the Java convention for naming
	 * methods. Example: <code>"someCoolName"</code> will be <code>"some_cool_name"</code>.
	 *
	 * @param str string to transform
	 * @return snake case string
	 */
	static String fromLowerCamelToSnakeCase(final String str) {
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1_$2";
		return str.replaceAll(regex, replacement).toLowerCase();
	}

	/**
	 * Alias for {@link #fromLowerCamelToSnakeCase(String)}.
	 *
	 * @param str string to transform
	 * @return kebab case string
	 */
	static String fromCamelToSnakeCase(final String str) {
		return fromLowerCamelToSnakeCase(str);
	}

	/**
	 * Transforms a string from Lower Camel case to Kebab case. The Lower Camel case is the Java convention for naming
	 * methods. Example: <code>"someCoolName"</code> will be <code>"some-cool-name"</code>.
	 *
	 * @param str string to transform
	 * @return kebab case string
	 */
	static String fromLowerCamelToKebabCase(final String str) {
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1-$2";
		return str.replaceAll(regex, replacement).toLowerCase();
	}

	/**
	 * Alias for {@link #fromLowerCamelToKebabCase(String)}.
	 *
	 * @param str string to transform
	 * @return kebab case string
	 */
	static String fromCamelToKebabCase(final String str) {
		return fromLowerCamelToKebabCase(str);
	}

	/**
	 * Transforms a string from Kebab case to Lower Camel case. Example: <code>"some-cool-name"</code> will become
	 * <code>"someCoolName"</code>.
	 *
	 * @param str string to transform
	 * @return lower camel case string
	 */
	static String fromKebabToLowerCamelCase(final String str) {
		StringBuilder result = new StringBuilder();
		boolean upperNext = false;
		for (char c : str.toCharArray()) {
			if (c == '-') {
				upperNext = true;
			} else {
				result.append(upperNext ? Character.toUpperCase(c) : c);
				upperNext = false;
			}
		}
		return result.toString();
	}

	/**
	 * Alias for {@link #fromKebabToLowerCamelCase(String)}.
	 *
	 * @param str string to transform
	 * @return lower camel case string
	 */
	static String fromKebabToCamelCase(final String str) {
		return fromKebabToLowerCamelCase(str);
	}

	/**
	 * Transforms an input stream to a string. If the input stream cannot be converted to string with the given parameters,
	 * the result will be {@code null}.
	 *
	 * @param inputStream the input stream to read from
	 * @param encoding character encoding to use when reading the input stream
	 * @param maxSize maximum size in bytes to read from the input stream
	 * @param bufferSize buffer size to use when reading the input stream
	 * @return the input stream as string
	 * @throws IOException if an I/O error occurs
	 * @throws IllegalArgumentException if maxSize or bufferSize are not strictly positive
	 * @throws NullPointerException if inputStream or encoding is null
	 */
	static String toStringOrThrow(final InputStream inputStream, final Charset encoding, final int maxSize, final int bufferSize) throws IOException {
		Require.that(maxSize > 0, "Maximum size must be strictly positive");
		Require.that(bufferSize > 0, "Buffer size must be strictly positive");

		final StringBuilder out = new StringBuilder();
		try (Reader in = new InputStreamReader(inputStream, encoding)) {
			final char[] buffer = new char[bufferSize];
			long totalRead = 0;
			int s = 0;
			while ((s = in.read(buffer, 0, buffer.length)) >= 0) {
				out.append(buffer, 0, s);
				totalRead += s;
				if (totalRead > maxSize) {
					throw new IOException("Input stream exceeds maximum size of " + maxSize + " bytes");
				}
			}
		}
		return out.toString();
	}

	/**
	 * Transforms an input stream to a string. If the input stream cannot be converted to string with the given parameters,
	 * the result will be {@code null}.
	 *
	 * @param inputStream the input stream to read from
	 * @param encoding character encoding to use when reading the input stream
	 * @param maxSize maximum size in bytes to read from the input stream
	 * @param bufferSize buffer size to use when reading the input stream
	 * @param onError on error handler, must not be null
	 * @return the input stream as string
	 */
	static String toString(final InputStream inputStream, final Charset encoding, final int maxSize, final int bufferSize,
			final Consumer<Exception> onError) {
		try {
			Objects.requireNonNull(onError, "On error handler cannot be null");
			return toStringOrThrow(inputStream, encoding, maxSize, bufferSize);
		} catch (Exception e) {
			onError.accept(e);
			return null;
		}
	}

	/**
	 * Transforms an input stream to a string. If the input stream cannot be converted to string with the given parameters,
	 * the result will be {@code null}. This method is not intended for large streams and is also limited to read up to
	 * {@code Integer.MAX_VALUE} bytes from the input stream.
	 *
	 * @param inputStream input stream
	 * @param encoding character encoding
	 * @param bufferSize buffer size
	 * @param onError on error handler
	 * @return the input stream as string
	 */
	static String toString(final InputStream inputStream, final Charset encoding, final int bufferSize, final Consumer<Exception> onError) {
		return toString(inputStream, encoding, Integer.MAX_VALUE, bufferSize, onError);
	}

	/**
	 * Transforms an input stream to a string. If the input stream cannot be converted to string with the given parameters,
	 * the result will be null. This method is not intended for large streams and is also limited to read up to
	 * {@code Integer.MAX_VALUE} bytes from the input stream.
	 *
	 * @param inputStream input stream
	 * @param encoding character encoding
	 * @param bufferSize buffer size
	 * @return the input stream as string
	 */
	static String toString(final InputStream inputStream, final Charset encoding, final int bufferSize) {
		return toString(inputStream, encoding, bufferSize, Consumers.consumeNothing());
	}

	/**
	 * Transforms an input stream to a string. If the input stream cannot be converted to string with the given parameters,
	 * the result will be null. This method is not intended for large streams and is also limited to read up to
	 * {@code Integer.MAX_VALUE} bytes from the input stream.
	 *
	 * @param inputStream input stream
	 * @param encoding character encoding
	 * @return the input stream as string
	 */
	static String toString(final InputStream inputStream, final Charset encoding) {
		return toString(inputStream, encoding, IOStreams.DEFAULT_BUFFER_SIZE, Consumers.consumeNothing());
	}

	/**
	 * Returns a string from a file or {@code null} if any error occurred.
	 * <p>
	 * If the given path is an absolute path, it is considered a file system path otherwise, it is considered a classpath
	 * resource.
	 *
	 * @param path path to the file, must not be null
	 * @param encoding the file encoding
	 * @param bufferSize the size of the buffer while reading the file
	 * @param onError on error handler
	 * @return the file content as string
	 */
	static String fromFile(final String path, final Charset encoding, final int bufferSize, final Consumer<Exception> onError) {
		try {
			Objects.requireNonNull(path, "File path cannot be null");
			Objects.requireNonNull(onError, "onError handler cannot be null");

			try (InputStream inputStream = ResourceLocation.ofPath(path).open(path)) {
				return toStringOrThrow(inputStream, encoding, Integer.MAX_VALUE, bufferSize);
			}
		} catch (Exception e) {
			onError.accept(e);
			return null;
		}
	}

	/**
	 * Returns a string from a file or {@code null} if any error occurred.
	 * <p>
	 * If the given path is an absolute path, it is considered a file system path otherwise, it is considered a classpath
	 * resource.
	 *
	 * @param path path to the file
	 * @param encoding the file encoding
	 * @param bufferSize the size of the buffer while reading the file
	 * @return the file content as string
	 */
	static String fromFile(final String path, final Charset encoding, final int bufferSize) {
		return fromFile(path, encoding, bufferSize, Consumers.consumeNothing());
	}

	/**
	 * Returns a string from a file or {@code null} if any error occurred. It assumes that the encoding in
	 * {@link StandardCharsets#UTF_8} and uses a default buffer size of 4096 bytes. Use this method only if the file to be
	 * read respects these conditions.
	 * <p>
	 * If the given path is an absolute path, it is considered a file system path otherwise, it is considered a classpath
	 * resource.
	 *
	 * @param path path to the file
	 * @return the file content as string
	 */
	static String fromFile(final String path) {
		return fromFile(path, DEFAULT_CHARSET, IOStreams.DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Returns a string from a file or {@code null} if any error occurred. It assumes that the encoding in
	 * {@link StandardCharsets#UTF_8} and uses a default buffer size of 4096 bytes. Use this method only if the file to be
	 * read respects these conditions.
	 * <p>
	 * If the given path is an absolute path, it is considered a file system path otherwise, it is considered a classpath
	 * resource.
	 *
	 * @param path path to the file
	 * @param onError on error handler
	 * @return the file content as string
	 */
	static String fromFile(final String path, final Consumer<Exception> onError) {
		return fromFile(path, DEFAULT_CHARSET, IOStreams.DEFAULT_BUFFER_SIZE, onError);
	}

	/**
	 * Envelopes a string with the given envelope.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * Strings.envelope("==", "example"); // will return: "==example=="
	 * </pre>
	 *
	 * @param envelope string to put in front and back of the string
	 * @param s string to envelope
	 * @return enveloped string
	 */
	static String envelope(final String envelope, final String s) {
		return String.join("", envelope, s, envelope);
	}

	/**
	 * Returns the input string with all whitespace like spaces, tabs, new lines, etc. removed.
	 *
	 * @param input input string
	 * @return the input string with all whitespace removed
	 */
	static String removeAllWhitespace(final String input) {
		if (isEmpty(input)) {
			return input;
		}
		return input.replaceAll("\\s", "");
	}

	/**
	 * Strips the given character from the start and end of the input string. If the string is null, returns null. If empty,
	 * returns empty.
	 *
	 * @param input the input string to strip
	 * @param ch the character to strip
	 * @return stripped string from the start and end if it contains the given character
	 */
	static String stripChar(final String input, final char ch) {
		if (isEmpty(input)) {
			return input;
		}
		int start = 0;
		int end = input.length();
		while (start < end && input.charAt(start) == ch) {
			++start;
		}
		while (end > start && input.charAt(end - 1) == ch) {
			--end;
		}
		return input.substring(start, end);
	}
}
