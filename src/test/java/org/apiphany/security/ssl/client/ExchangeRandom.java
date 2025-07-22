package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import org.apiphany.io.BytesWrapper;
import org.apiphany.security.ssl.DeterministicSecureRandom;
import org.apiphany.security.tls.TLSObject;

import com.fasterxml.jackson.annotation.JsonValue;

public class ExchangeRandom extends BytesWrapper implements TLSObject {

	public static final int BYTES = 32;

	public ExchangeRandom(final byte[] random) {
		super(random);
	}

	public ExchangeRandom() {
		this(new byte[BYTES]);
	}

	public static ExchangeRandom from(final InputStream is) throws IOException {
		BytesWrapper binaryData = BytesWrapper.from(is, BYTES);
		return new ExchangeRandom(binaryData.toByteArray());
	}

	public static byte[] generateRandom(final SecureRandom secureRandom, int bytes) {
		byte[] random = new byte[bytes];
		secureRandom.nextBytes(random);
		return random;
	}

	public static byte[] generateRandom(final SecureRandom secureRandom) {
		return generateRandom(secureRandom, BYTES);
	}

	public static byte[] generateRandom() {
		return generateRandom(BYTES);
	}

	public static byte[] generateRandom(int bytes) {
		return generateRandom(new SecureRandom(), bytes);
	}

	public static byte[] generateLinear() {
		return generateLinear(BYTES);
	}

	public static byte[] generateLinear(int bytes) {
		return generateRandom(new DeterministicSecureRandom(), bytes);
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
		return toByteArray();
	}
}

