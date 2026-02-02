package org.apiphany.lang;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import org.morphix.lang.function.Consumers;
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
	public static final byte[] EMPTY = new byte[] { };

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
		String cleanedHex = Strings.removeAllWhitespace(hexString);

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
	 * Returns true if the given byte array is empty, false otherwise.
	 *
	 * @param bytes the byte array to check
	 * @return true if the given byte array is empty, false otherwise
	 */
	public static boolean isEmpty(final byte[] bytes) {
		return null == bytes || EMPTY == bytes || 0 == bytes.length;
	}

	/**
	 * Returns true if the given byte array is not empty, false otherwise.
	 *
	 * @param bytes the byte array to check
	 * @return true if the given byte array is not empty, false otherwise
	 */
	public static boolean isNotEmpty(final byte[] bytes) {
		return !isEmpty(bytes);
	}

	/**
	 * Pads the given byte array on the right with the specified padding byte until its length becomes a multiple of the
	 * given block size. If already aligned, the original array reference is returned if the extendIfAligned flag is false,
	 * otherwise a new block is added padded with the given padding byte.
	 *
	 * @param bytes the byte array to pad (can be null)
	 * @param blockSize the block size (must be > 0)
	 * @param paddingByte the byte to use for padding
	 * @param extendIfAligned true if new padding block should be added if input is already aligned, false otherwise
	 * @return a new byte array padded to a block-size multiple, or the original if already aligned
	 */
	public static byte[] padRightToBlockSize(final byte[] bytes, final int blockSize, final byte paddingByte, final boolean extendIfAligned) {
		Require.notNull(bytes, "Byte array cannot be null");
		Require.that(blockSize > 0, "Block size must be greater than zero");

		final int remainder = bytes.length % blockSize;
		if (0 == remainder && !extendIfAligned) {
			return bytes;
		}
		final int targetLength = bytes.length + blockSize - remainder;

		byte[] result = new byte[targetLength];
		System.arraycopy(bytes, 0, result, 0, bytes.length);
		if (paddingByte != 0) {
			Arrays.fill(result, bytes.length, targetLength, paddingByte);
		}
		return result;
	}

	/**
	 * Pads the given byte array on the right with the specified padding byte until its length becomes a multiple of the
	 * given block size. If already aligned, the original array reference is returned.
	 *
	 * @param bytes the byte array to pad (can be null)
	 * @param blockSize the block size (must be > 0)
	 * @param paddingByte the byte to use for padding
	 * @return a new byte array padded to a block-size multiple, or the original if already aligned
	 */
	public static byte[] padRightToBlockSize(final byte[] bytes, final int blockSize, final byte paddingByte) {
		return padRightToBlockSize(bytes, blockSize, paddingByte, false);
	}

	/**
	 * Pads the given byte array on the right using {@code PKCS#7}-style padding. The padding byte value equals the number
	 * of padding bytes added (always in the range {@code 1..blockSize}).
	 * <p>
	 * If the input is already aligned to the block size, a full block of padding is added.
	 *
	 * @param bytes the byte array to pad (must not be null)
	 * @param blockSize the block size (must be > 0)
	 * @return a new byte array padded with {@code PKCS#7} padding
	 */
	public static byte[] padPKCS7(final byte[] bytes, final int blockSize) {
		final int remainder = bytes.length % blockSize;
		final int paddingLength = blockSize - remainder;
		final byte paddingByte = (byte) (paddingLength - 1);

		return padRightToBlockSize(bytes, blockSize, paddingByte, true);
	}

	/**
	 * Reads all bytes from a file located at the given path. If the path is absolute, it reads from the file system;
	 * otherwise, it attempts to read the file as a class path resource.
	 *
	 * @param path the file path (absolute or class path resource)
	 * @param onError a consumer to handle exceptions that may occur during file reading
	 * @return a byte array containing the file's contents, or null if an error occurred
	 * @throws NullPointerException if the path is null
	 */
	public static byte[] fromFile(final String path, final Consumer<Exception> onError) {
		Objects.requireNonNull(path, "File path cannot be null");
		try {
			Path fsPath = Paths.get(path);
			if (fsPath.isAbsolute()) {
				return Files.readAllBytes(fsPath);
			}
			try (InputStream inputStream = Bytes.class.getClassLoader().getResourceAsStream(path)) {
				if (null == inputStream) {
					throw new FileNotFoundException("Classpath resource not found: " + path);
				}
				return inputStream.readAllBytes();
			}
		} catch (Exception e) {
			onError.accept(e);
			return EMPTY;
		}
	}

	/**
	 * Reads all bytes from a file located at the given path. If the path is absolute, it reads from the file system;
	 * otherwise, it attempts to read the file as a class path resource.
	 *
	 * @param path the file path (absolute or class path resource)
	 * @return a byte array containing the file's contents, or an empty array if an error occurred
	 * @throws NullPointerException if the path is null
	 */
	public static byte[] fromFile(final String path) {
		return fromFile(path, Consumers.consumeNothing());
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
