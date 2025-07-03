package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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

	public static byte[] prf(final byte[] secret, final String label, final byte[] seed, final int length) throws Exception {
		byte[] labelSeed = concatenate(label.getBytes(StandardCharsets.US_ASCII), seed);
		return pHash(secret, labelSeed, length);
	}

	private static byte[] pHash(final byte[] secret, final byte[] seed, final int length) throws Exception {
		Mac hmac = Mac.getInstance("HmacSHA256");
		SecretKeySpec keySpec = new SecretKeySpec(secret, "HmacSHA256");
		hmac.init(keySpec);

		byte[] seedBytes = seed;
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		while (result.size() < length) {
			seedBytes = hmac.doFinal(seedBytes);
			byte[] output = hmac.doFinal(concatenate(seedBytes, seed));
			result.write(output);
		}
		return Arrays.copyOf(result.toByteArray(), length);
	}

	private static byte[] concatenate(final byte[]... arrays) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (byte[] arr : arrays) {
			bos.write(arr);
		}
		return bos.toByteArray();
	}

	public static byte[] deriveMasterSecret(final byte[] preMasterSecret, final byte[] clientRandom, final byte[] serverRandom) throws Exception {
		return prf(preMasterSecret, "master secret", concatenate(clientRandom, serverRandom), 48);
	}

	public static byte[] deriveKeyBlock(final byte[] masterSecret, final byte[] serverRandom, final byte[] clientRandom, final int length) throws Exception {
		return prf(masterSecret, "key expansion", concatenate(serverRandom, clientRandom), length);
	}

	public static String hexString(final byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X ", b));
		}
		return sb.toString();
	}
}
