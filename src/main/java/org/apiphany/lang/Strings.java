package org.apiphany.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

import org.morphix.lang.thread.Threads;

/**
 * {@link String} utility methods.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Strings {

	/**
	 * End Of Line, system dependent line separator.
	 */
	static final String EOL = System.lineSeparator();

	/**
	 * Returns the call of {@link Object#toString()} on the given parameter if the parameter is not null, null otherwise.
	 *
	 * @param o object
	 * @return the string representation of the object
	 */
	public static String safeToString(final Object o) {
		return Objects.toString(o, null);
	}

	/**
	 * Returns the parameter if it is not null, empty string otherwise.
	 *
	 * @param s a string
	 * @return the parameter if it is not null, empty string otherwise
	 */
	public static String safe(final String s) {
		return null != s ? s : "";
	}

	/**
	 * Checks if a CharSequence is empty ("") or null.
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is empty or null
	 */
	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	/**
	 * Checks if a CharSequence is not empty ("") or null.
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is not empty or null
	 */
	public static boolean isNotEmpty(final CharSequence cs) {
		return !isEmpty(cs);
	}

	/**
	 * Transforms a string from Lower Camel case to Kebab case. Lower Camel case is the Java convention for naming methods.
	 * Example: <code>"someCoolName"</code> will be <code>"some-cool-name"</code>.
	 *
	 * @param str string to transform
	 * @return kebab case string
	 */
	public static String fromLowerCamelToKebabCase(final String str) {
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1-$2";
		return str.replaceAll(regex, replacement).toLowerCase();
	}

	/**
	 * Transforms an input stream to a string. If the input stream cannot be converted to string with the given parameters
	 * the result will be null.
	 *
	 * @param inputStream input stream
	 * @param encoding character encoding
	 * @param bufferSize buffer size
	 * @param onError on error handler
	 * @return string
	 */
	public static String toString(final InputStream inputStream, final Charset encoding, final int bufferSize, final Consumer<Exception> onError) {
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		try (Reader in = new InputStreamReader(inputStream, encoding.name())) {
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
	 * Transforms an input stream to a string. If the input stream cannot be converted to string with the given parameters
	 * the result will be null.
	 *
	 * @param inputStream input stream
	 * @param encoding character encoding
	 * @param bufferSize buffer size
	 * @return string
	 */
	public static String toString(final InputStream inputStream, final Charset encoding, final int bufferSize) {
		return toString(inputStream, encoding, bufferSize, Threads.consumeNothing());
	}

	/**
	 * Returns a string from a file or {@code null} if any error occured.
	 *
	 * @param path path to the file
	 * @param onError on error handler
	 * @return the file content as string
	 */
	public static String fromFile(final String path, final Consumer<Exception> onError) {
		String fileContent = null;
		try (InputStream inputStream = Strings.class.getResourceAsStream(path)) {
			fileContent = toString(inputStream, StandardCharsets.UTF_8, 100, onError);
		} catch (IOException e) {
			onError.accept(e);
		}
		return fileContent;
	}

	/**
	 * Returns a string from a file or {@code null} if any error occured.
	 *
	 * @param path path to the file
	 * @return the file content as string
	 */
	public static String fromFile(final String path) {
		return fromFile(path, Threads.consumeNothing());
	}
}
