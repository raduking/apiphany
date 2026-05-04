package org.apiphany.security;

import java.security.SecureRandom;

import org.morphix.reflection.Constructors;

/**
 * This class provides utility methods for generating random strings. It includes a method for generating secure random
 * alphanumeric strings of a specified length.
 *
 * @author Radu Sebastian LAZIN
 */
public final class RandomStrings {

	/**
	 * A string containing all uppercase letters, lowercase letters, and digits. This string is used as the character set
	 * for generating random alphanumeric strings. When generating a random string, characters will be randomly selected
	 * from this string to ensure that the resulting string is alphanumeric.
	 */
	private static final String BASE62_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	/**
	 * A SecureRandom instance used for generating secure random numbers. This instance is used in the method for generating
	 * secure random alphanumeric strings. Using SecureRandom ensures that the generated strings are cryptographically
	 * strong and suitable for security-sensitive applications, such as generating tokens or passwords.
	 */
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	/**
	 * Private constructor.
	 */
	private RandomStrings() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Generates a secure random alphanumeric string of the specified length. The generated string will consist of uppercase
	 * letters, lowercase letters, and digits. The method uses a {@link SecureRandom} instance to ensure that the generated
	 * string is cryptographically strong and suitable for security-sensitive applications.
	 *
	 * @param length the desired length of the generated string
	 * @return a secure random alphanumeric string of the specified length
	 */
	public static String secureAlphanumeric(final int length) {
		if (length < 0) {
			throw new IllegalArgumentException("length must be positive");
		}
		StringBuilder result = new StringBuilder(length);
		int base62Length = BASE62_CHARS.length();
		for (int i = 0; i < length; ++i) {
			result.append(BASE62_CHARS.charAt(SECURE_RANDOM.nextInt(base62Length)));
		}
		return result.toString();
	}
}
