package org.apiphany.lang;

import java.util.Objects;

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

}
