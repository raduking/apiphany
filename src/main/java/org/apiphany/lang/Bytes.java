package org.apiphany.lang;

import java.util.Objects;

import org.morphix.reflection.Constructors;

/**
 * Utility class for byte array operations including concatenation, hex conversion, and manipulation.
 * <p>
 * This class provides thread-safe operations for:
 * <ul>
 * <li>Byte array concatenation</li>
 * <li>In-place byte order reversal</li>
 * <li>Hexadecimal string conversion</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public final class Bytes {

	/**
	 * Empty byte array constant.
	 */
	public static final byte[] EMPTY = new byte[] {};

	/**
	 * Concatenates multiple byte arrays into a single array.
	 *
	 * @param arrays the byte arrays to concatenate (null arrays are treated as empty)
	 * @return a new byte array containing all input bytes in order
	 * @throws OutOfMemoryError if the resulting array would exceed maximum array size
	 */
	public static byte[] concatenate(final byte[]... arrays) {
		Objects.requireNonNull(arrays, "Input arrays cannot be null");
		int totalLength = 0;
		for (byte[] arr : arrays) {
			if (arr != null) {
				totalLength += arr.length;
			}
		}
		byte[] result = new byte[totalLength];
		int offset = 0;
		for (byte[] arr : arrays) {
			if (arr != null) {
				System.arraycopy(arr, 0, result, offset, arr.length);
				offset += arr.length;
			}
		}
		return result;
	}

	/**
	 * Reverses the order of bytes in the given array in-place and returns it.
	 *
	 * @param bytes the byte array to reverse (modified directly)
	 * @return the same reference as the input array
	 * @throws NullPointerException if the input array is null
	 */
	public static byte[] reverse(final byte[] bytes) {
		Objects.requireNonNull(bytes, "Byte array cannot be null");
		for (int i = 0; i < bytes.length / 2; ++i) {
			byte tmp = bytes[i];
			bytes[i] = bytes[bytes.length - 1 - i];
			bytes[bytes.length - 1 - i] = tmp;
		}
		return bytes;
	}

	/**
	 * Converts a hexadecimal string to a byte array.
	 *
	 * @param hexString the string to convert (may contain whitespace)
	 * @return new byte array containing the decoded bytes
	 * @throws IllegalArgumentException if:
	 *     <ul>
	 *     <li>The string contains non-hex characters</li>
	 *     <li>The string has an odd number of characters (after whitespace removal)</li>
	 *     </ul>
	 * @throws NullPointerException if the input string is null
	 */
	public static byte[] fromHex(final String hexString) {
		Objects.requireNonNull(hexString, "Hex string cannot be null");
		String cleanedHex = hexString.replaceAll("\\s", "");

		if (cleanedHex.length() % 2 != 0) {
			throw new IllegalArgumentException("Hex string must have an even number of characters (after whitespace removal)");
		}

		byte[] byteArray = new byte[cleanedHex.length() / 2];
		for (int i = 0; i < byteArray.length; ++i) {
			int index = i * 2;
			String hexPair = cleanedHex.substring(index, index + 2);
			byteArray[i] = (byte) Integer.parseInt(hexPair, 16);
		}
		return byteArray;
	}

	/**
	 * Private constructor to prevent instantiation.
	 *
	 * @throws UnsupportedOperationException if called via reflection
	 */
	private Bytes() {
		throw Constructors.unsupportedOperationException();
	}
}
