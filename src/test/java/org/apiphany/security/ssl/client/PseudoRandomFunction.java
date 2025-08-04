package org.apiphany.security.ssl.client;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apiphany.lang.Bytes;
import org.apiphany.security.tls.PRFLabel;

public class PseudoRandomFunction {

	public static final String ALGORITHM_SHA384 = "HmacSHA384";

	public static byte[] apply(final byte[] secret, final PRFLabel label, final byte[] seed, final int length) throws Exception {
		return apply(secret, label, seed, length, ALGORITHM_SHA384);
	}

	public static byte[] apply(final byte[] secret, final String label, final byte[] seed, final int length) throws Exception {
		return apply(secret, label.getBytes(StandardCharsets.US_ASCII), seed, length, ALGORITHM_SHA384);
	}

	public static byte[] apply(final byte[] secret, final PRFLabel label, final byte[] seed, final int length, final String algorithm) throws Exception {
		return apply(secret, label.toByteArray(), seed, length, algorithm);
	}

	public static byte[] apply(final byte[] secret, final byte[] label, final byte[] seed, final int length, final String algorithm) throws Exception {
		byte[] labelBytes = label;
		byte[] labelSeed = new byte[labelBytes.length + seed.length];
		System.arraycopy(labelBytes, 0, labelSeed, 0, labelBytes.length);
		System.arraycopy(seed, 0, labelSeed, labelBytes.length, seed.length);

		return pHash(secret, labelSeed, length, algorithm);
	}

	public static byte[] pHash(final byte[] secret, final byte[] seed, final int length, final String algorithm) throws Exception {
		Mac mac = Mac.getInstance(algorithm);
		mac.init(new SecretKeySpec(secret, algorithm));

		byte[] a = seed;
		byte[] result = new byte[length];

		int offset = 0;
		while (offset < length) {
			a = mac.doFinal(a); // a(i) = HMAC(secret, a(i-1))
			byte[] output = mac.doFinal(Bytes.concatenate(a, seed));
			int copyLength = Math.min(output.length, length - offset);
			System.arraycopy(output, 0, result, offset, copyLength);
			offset += copyLength;
		}
		return result;
	}

}
