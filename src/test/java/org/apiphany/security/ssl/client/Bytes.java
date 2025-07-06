package org.apiphany.security.ssl.client;

public class Bytes {

	public static short toShort(final byte[] bytes) {
		if (2 != bytes.length) {
			throw new IllegalArgumentException("Can only convert 2 bytes to short, actual bytes: " + bytes.length);
		}
		return (short) (((short) (bytes[0] << 8)) + bytes[1]);
	}

	public static byte[] from(final short value) {
		return new byte[] {
				(byte) (value >> 8),
				(byte) (value & (short) 0x00FF)
		};
	}

	public static byte[] from(final byte value) {
		return new byte[] {
				value
		};
	}

	public static void set(final short value, final byte[] bytes, final int index) {
		if (index + 1 >= bytes.length || 0 > index) {
			throw new IllegalArgumentException("Index out of bounds: " + index);
		}
		bytes[index] = (byte) ((value >> 8) & 0xFF);
		bytes[index + 1] = (byte) (value & 0xFF);
	}

	public static String hexString(final byte[] bytes) {
		return hexString(bytes, " ");
	}

	public static String hexString(final byte[] bytes, final String separator) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(hex(b, separator));
		}
		return sb.toString().trim();
	}

	public static String hex(final byte b) {
		return String.format("%02X%s", b, " ");
	}

	public static String hex(final byte b, final String separator) {
		return String.format("%02X%s", b, separator);
	}

	public static String hexDump(final byte[] bytes) {
		if (bytes == null) {
			return "null";
		}

		int width = 16;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < bytes.length; i += width) {
			// Print offset
			sb.append(String.format("%04X: ", i));

			// Print hex values
			for (int j = 0; j < width; j++) {
				if (i + j < bytes.length) {
					sb.append(String.format("%02X ", bytes[i + j]));
				} else {
					sb.append("   ");
				}

				// Add extra space after 8 bytes
				if (j == 7) {
					sb.append(" ");
				}
			}

			// Print ASCII representation
			sb.append(" ");
			for (int j = 0; j < width; j++) {
				if (i + j < bytes.length) {
					char c = (char) (bytes[i + j] & 0xFF); // Convert to unsigned
					sb.append((c >= 32 && c < 127) ? c : '.');
				}
			}
			sb.append("\n");
		}

		return sb.toString();
	}
}
