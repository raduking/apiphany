package org.apiphany.lang;

import java.util.Objects;
import java.util.function.Supplier;

import org.apiphany.io.BinaryRepresentable;
import org.morphix.reflection.Constructors;

/**
 * Utility class that helps with conversions between hexadecimal Strings and bytes or byte arrays. This class is very
 * helpful for debugging and printing out byte arrays when working with raw bytes.
 *
 * @author Radu Sebastian LAZIN
 */
public class Hex {

	/**
	 * Property name to set the verbose flag when calling {@link #dump(byte[])}.
	 */
	public static final String VERBOSE_PROPERTY_NAME = "hex.dump.verbose";

	/**
	 * Verbose dump flag.
	 */
	private static final boolean VERBOSE = Objects.equals("true", System.getProperty(VERBOSE_PROPERTY_NAME));

	/**
	 * Hexadecimal characters.
	 */
	private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

	/**
	 * Hide constructor.
	 */
	private Hex() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Converts a byte array to a hexadecimal string representation.
	 *
	 * @param bytes the byte array to convert
	 * @return hexadecimal string representation of the byte array
	 */
	public static String string(final byte[] bytes) {
		return string(bytes, "");
	}

	/**
	 * Returns a {@link Supplier} that supplies the hexadecimal string representation of the given byte array. Useful for
	 * lazy evaluation in SLF4J 2.x logging:
	 *
	 * <pre>{@code
	 * LOGGER.debug("keyBlock: {}", Hex.stringSupplier(bytes));
	 * }</pre>
	 *
	 * The supplier is only evaluated if the log level is enabled.
	 *
	 * @param bytes the byte array to convert
	 * @return a Supplier that lazily converts the byte array to hex
	 */
	public static Supplier<String> stringSupplier(final byte[] bytes) {
		return () -> string(bytes);
	}

	/**
	 * Converts a byte array to a hexadecimal string representation with each value separated by space {@code " "}.
	 *
	 * @param bytes the byte array to convert
	 * @return hexadecimal spaced string representation of the byte array
	 */
	public static String stringSpaced(final byte[] bytes) {
		return string(bytes, " ");
	}

	/**
	 * Converts a byte array to a hexadecimal string representation with custom separator.
	 *
	 * @param bytes the byte array to convert
	 * @param separator the separator to use between hex values
	 * @return hexadecimal string representation of the byte array
	 */
	public static String string(final byte[] bytes, final String separator) {
		if (bytes == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(string(b, separator));
		}
		return sb.toString().trim();
	}

	/**
	 * Converts a single byte to a hexadecimal string representation with custom separator.
	 *
	 * @param b the byte to convert
	 * @param separator the separator to append after the hex value
	 * @return hexadecimal string representation of the byte
	 */
	public static String string(final byte b, final String separator) {
		char high = HEX_CHARS[(b >> 4) & 0x0F];
		char low = HEX_CHARS[b & 0x0F];
		return "" + high + low + separator;
	}

	/**
	 * Converts an integer value to a hexadecimal string representation with specified width.
	 *
	 * @param value the integer value to convert
	 * @param width the width of the resulting hexadecimal string
	 * @return hexadecimal string representation of the integer value
	 */
	public static String string(final int value, final int width) {
		int currentValue = value;
		char[] hexChars = new char[width];
		for (int i = width - 1; i >= 0; --i) {
			hexChars[i] = HEX_CHARS[currentValue & 0x0F];
			currentValue >>>= 4;
		}
		return new String(hexChars);
	}

	/**
	 * Dumps a byte array in hexadecimal format with optional verbose output. Verbose output includes offset, hex values,
	 * and ASCII representation.
	 * <p>
	 * Sonar will complain about the cognitive complexity, well, tough luck, breaking up this method doesn't make sense at
	 * the moment.
	 *
	 * @param bytes the byte array to dump
	 * @param verbose whether to produce verbose output
	 * @return formatted hexadecimal dump of the byte array
	 */
	public static String dump(final byte[] bytes, final boolean verbose) { // NOSONAR
		if (bytes == null) {
			return "null";
		}
		int width = 16;
		int half = width / 2 - 1;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < bytes.length; i += width) {
			if (verbose) {
				sb.append(string(i, 4)).append(": ");
			}
			for (int j = 0; j < width; ++j) {
				if (i + j < bytes.length) {
					boolean addSeparator = verbose || (j < width - 1 && i + j < bytes.length - 1);
					sb.append(string(bytes[i + j], addSeparator ? " " : ""));
				} else {
					if (verbose) {
						sb.append("   ");
					} else {
						break;
					}
				}
				if (j == half) {
					sb.append(" ");
					if (verbose) {
						sb.append(" ");
					}
				}
			}
			if (verbose) {
				sb.append(" ");
				for (int j = 0; j < width; j++) {
					if (i + j < bytes.length) {
						char c = (char) (bytes[i + j] & 0xFF);
						sb.append((c >= 32 && c < 127) ? c : '.');
					}
				}
			}
			sb.append(Strings.EOL);
		}
		return sb.toString();
	}

	/**
	 * Dumps a byte array in hexadecimal format with verbose output (includes offset, hex values, and ASCII representation
	 * if the {@link #VERBOSE_PROPERTY_NAME} is set to true).
	 *
	 * @param bytes the byte array to dump
	 * @return formatted hexadecimal dump of the byte array
	 */
	public static String dump(final byte[] bytes) {
		return dump(bytes, VERBOSE);
	}

	/**
	 * Dumps a binary representable object in hexadecimal format with verbose output (includes offset, hex values, and ASCII
	 * representation if the {@link #VERBOSE_PROPERTY_NAME} is set to true).
	 *
	 * @param binaryRepresentable the binary representable object
	 * @return formatted hexadecimal dump of the binary representable object
	 */
	public static String dump(final BinaryRepresentable binaryRepresentable) {
		return dump(binaryRepresentable.toByteArray());
	}
}
