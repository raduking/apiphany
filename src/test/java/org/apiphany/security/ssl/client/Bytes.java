package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Bytes {

	public static void set(final short value, final byte[] bytes, final int index) {
		if (index + 1 >= bytes.length || index < 0) {
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

	public static void reverse(final byte[] bytes) {
	    for (int i = 0; i < bytes.length / 2; ++i) {
	        byte tmp = bytes[i];
	        bytes[i] = bytes[bytes.length - 1 - i];
	        bytes[bytes.length - 1 - i] = tmp;
	    }
	}

	public static byte[] fromHex(final String hexString) {
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
