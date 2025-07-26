package org.apiphany.security.ssl.client;

import java.security.MessageDigest;

public class HashingFunction {

	public static byte[] apply(final String algorithm, final byte[] input) throws Exception {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		return digest.digest(input);
	}
}
