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
		return sb.toString();
	}

	public static String hex(final byte b) {
		return String.format("%02X%s", b, " ");
	}

	public static String hex(final byte b, final String separator) {
		return String.format("%02X%s", b, separator);
	}
}
