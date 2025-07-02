package org.apiphany.security.ssl.client;

public class Bytes {

	public interface Size {

		public static final int SHORT = 2;
		public static final int BITS24 = 3;
	}

	public static short toShort(byte[] bytes) {
		if (2 != bytes.length) {
			throw new IllegalArgumentException("Can only convert 2 bytes to short, actual bytes: " + bytes.length);
		}
		return (short) (((short) (bytes[0] << 8)) + bytes[1]);
	}

	public static byte[] from(short value) {
		return new byte[] {
				(byte) (value >> 8),
				(byte) (value & (short) 0x00FF)
		};
	}

	public static byte[] from(byte value) {
		return new byte[] {
				value
		};
	}

	public static String hexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X ", b));
		}
		return sb.toString();
	}
}
