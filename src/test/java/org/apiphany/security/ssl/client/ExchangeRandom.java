package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import org.apiphany.json.JsonBuilder;

import com.fasterxml.jackson.annotation.JsonValue;

public class ExchangeRandom implements Sizeable {

	public static final int BYTES = 32;

	private final byte[] random;

	public ExchangeRandom(final byte[] random) {
		if (BYTES != random.length) {
			throw new IllegalArgumentException("Invalid buffer size: " + random.length);
		}
		this.random = random.clone();
	}

	public ExchangeRandom() {
		this(new byte[BYTES]);
	}

	public static ExchangeRandom from(final InputStream is) throws IOException {
		ExchangeRandom handshakeRandom = new ExchangeRandom();
		int bytesRead = is.read(handshakeRandom.random);
		if (BYTES != bytesRead) {
			throw new EOFException("Error reading " + BYTES + " bytes");
		}
		return handshakeRandom;
	}

	public byte[] toByteArray() {
		return random.clone();
	}

	public static byte[] generateRandom() {
		SecureRandom secureRandom = new SecureRandom();
		byte[] random = new byte[BYTES];
		secureRandom.nextBytes(random);
		return random;
	}

	public static byte[] generateLinear() {
		byte[] random = new byte[BYTES];
		for (byte b = 0; b < BYTES; ++b) {
			random[b] = b;
		}
		return random;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@JsonValue
	public String toHexString() {
		return Bytes.hexString(random, "");
	}

	@Override
	public int size() {
		return BYTES;
	}

	public byte[] getRandom() {
		return random;
	}
}

