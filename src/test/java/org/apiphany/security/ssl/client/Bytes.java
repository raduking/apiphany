package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Bytes {

	public static void set(final short value, final byte[] bytes, final int index) {
		if (index + 1 >= bytes.length || 0 > index) {
			throw new IllegalArgumentException("Index out of bounds: " + index);
		}
		bytes[index] = (byte) ((value >> 8) & 0xFF);
		bytes[index + 1] = (byte) (value & 0xFF);
	}

	public static byte[] concatenate(final byte[]... arrays) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (byte[] arr : arrays) {
			bos.write(arr);
		}
		return bos.toByteArray();
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
			sb.append(String.format("%04X: ", i));

			for (int j = 0; j < width; j++) {
				if (i + j < bytes.length) {
					sb.append(String.format("%02X ", bytes[i + j]));
				} else {
					sb.append("   ");
				}
				if (j == 7) {
					sb.append(" ");
				}
			}
			sb.append(" ");
			for (int j = 0; j < width; j++) {
				if (i + j < bytes.length) {
					char c = (char) (bytes[i + j] & 0xFF);
					sb.append((c >= 32 && c < 127) ? c : '.');
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public static String hexDumpRaw(final byte[] bytes) {
		if (bytes == null) {
			return "null";
		}
		int width = 16;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < bytes.length; i += width) {
			for (int j = 0; j < width; j++) {
				if (i + j < bytes.length) {
					sb.append(String.format("%02X ", bytes[i + j]));
				} else {
					sb.append("   ");
				}
				if (j == 7) {
					sb.append(" ");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public static byte[] fromHexString(final String hexString) {
		String cleanedHex = hexString.replaceAll("\\s", "");
		if (0 != cleanedHex.length() % 2) {
			throw new IllegalArgumentException("Hex string must have an even number of characters");
		}

		byte[] byteArray = new byte[cleanedHex.length() / 2];
		for (int i = 0; i < byteArray.length; ++i) {
			int index = i * 2;
			String hexPair = cleanedHex.substring(index, index + 2);
			byteArray[i] = (byte) Integer.parseInt(hexPair, 16);
		}
		return byteArray;
	}
}
