package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class HandshakeRandom {

	public static final int SIZE = 32;

	private byte[] random = new byte[SIZE];

	public static HandshakeRandom from(InputStream is) throws IOException {
		HandshakeRandom handshakeRandom = new HandshakeRandom();
		is.read(handshakeRandom.random);
		return handshakeRandom;
	}

	public byte[] toByteArray() {
		for (byte b = 0; b < SIZE; ++b) {
			random[b] = b;
		}
		return random;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public byte[] getRandom() {
		return random;
	}
}

