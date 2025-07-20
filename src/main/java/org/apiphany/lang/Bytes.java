package org.apiphany.lang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
	 * @throws IOException if an I/O error occurs during concatenation
	 * @throws OutOfMemoryError if the resulting array would exceed maximum array size
	 */
	public static byte[] concatenate(final byte[]... arrays) throws IOException {
		Objects.requireNonNull(arrays, "Input arrays cannot be null");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (byte[] arr : arrays) {
			bos.write(arr != null ? arr : EMPTY);
		}
		return bos.toByteArray();
	}

	/**
	 * Reverses the order of bytes in the given array in-place.
	 *
	 * @param bytes the byte array to reverse (modified directly)
	 * @throws NullPointerException if the input array is null
	 */
	public static void reverse(final byte[] bytes) {
		Objects.requireNonNull(bytes, "Byte array cannot be null");
		for (int i = 0; i < bytes.length / 2; ++i) {
			byte tmp = bytes[i];
			bytes[i] = bytes[bytes.length - 1 - i];
			bytes[bytes.length - 1 - i] = tmp;
		}
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
