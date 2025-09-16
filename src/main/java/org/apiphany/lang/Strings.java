package org.apiphany.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

import org.apiphany.io.IO;
import org.morphix.lang.function.Consumers;

/**
 * {@link String} utility methods.
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
	 * Transforms an input stream to a string. If the input stream cannot be converted to string with the given parameters,
	 * the result will be null.
	 *
	 * @param inputStream input stream
	 * @param encoding character encoding
	 * @param bufferSize buffer size
	 * @param onError on error handler
	 * @return the input stream as string
	 */
	static String toString(final InputStream inputStream, final Charset encoding, final int bufferSize, final Consumer<Exception> onError) {
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		try (Reader in = new InputStreamReader(inputStream, encoding)) {
			int s = 0;
			while (s >= 0) {
				s = in.read(buffer, 0, buffer.length);
				if (s >= 0) {
					out.append(buffer, 0, s);
				}
			}
		} catch (IOException e) {
			onError.accept(e);
			return null;
		}
		return out.toString();
	}

	/**
	 * Transforms an input stream to a string. If the input stream cannot be converted to string with the given parameters,
	 * the result will be null.
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
	 * the result will be null.
	 *
	 * @param inputStream input stream
	 * @param encoding character encoding
	 * @return the input stream as string
	 */
	static String toString(final InputStream inputStream, final Charset encoding) {
		return toString(inputStream, encoding, IO.DEFAULT_BUFFER_SIZE, Consumers.consumeNothing());
	}

	/**
	 * Returns a string from a file or {@code null} if any error occurred.
	 *
	 * @param path path to the file
	 * @param encoding the file encoding
	 * @param bufferSize the size of the buffer while reading the file
	 * @param onError on error handler
	 * @return the file content as string
	 */
	static String fromFile(final String path, final Charset encoding, final int bufferSize, final Consumer<Exception> onError) {
		String fileContent = null;
		try (InputStream inputStream = Strings.class.getResourceAsStream(path)) {
			fileContent = toString(inputStream, encoding, bufferSize, onError);
		} catch (Exception e) {
			onError.accept(e);
		}
		return fileContent;
	}

	/**
	 * Returns a string from a file or {@code null} if any error occurred.
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
	 *
	 * @param path path to the file
	 * @return the file content as string
	 */
	static String fromFile(final String path) {
		return fromFile(path, DEFAULT_CHARSET, IO.DEFAULT_BUFFER_SIZE);
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
	 * @param s input string
	 * @return the input string with all whitespace removed
	 */
	static String removeAllWhitespace(final String s) {
		return s.replaceAll("\\s", "");
	}

	/**
	 * Strips the given character from the start and end of the input string. If the string is null, returns null. If empty,
	 * returns empty.
	 *
	 * @param input the input string to strip
	 * @param ch the character to strip
	 * @return stripped string from the start end end if it contains the given character
	 */
	public static String stripChar(final String input, final char ch) {
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
