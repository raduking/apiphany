package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import org.apiphany.security.ssl.DeterministicSecureRandom;

import com.fasterxml.jackson.annotation.JsonValue;

public class ExchangeRandom extends BinaryData {

	public static final int BYTES = 32;

	public ExchangeRandom(final byte[] random) {
		super(random);
	}

	public ExchangeRandom() {
		this(new byte[BYTES]);
	}

	public static ExchangeRandom from(final InputStream is) throws IOException {
		BinaryData binaryData = BinaryData.from(is, BYTES);
		return new ExchangeRandom(binaryData.getBytes());
	}

	public static byte[] generateRandom(final SecureRandom secureRandom) {
		byte[] random = new byte[BYTES];
		secureRandom.nextBytes(random);
		return random;
	}

	public static byte[] generateRandom() {
		return generateRandom(new SecureRandom());
	}

	public static byte[] generateLinear() {
		return generateRandom(new DeterministicSecureRandom());
	}

	public static ExchangeRandom linear() {
		return new ExchangeRandom(generateLinear());
	}

	@Override
	@JsonValue
	public String toHexString() {
		return super.toHexString();
	}

	public byte[] getRandom() {
		return getBytes();
	}
}

