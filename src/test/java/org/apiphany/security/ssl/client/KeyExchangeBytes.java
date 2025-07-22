package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.io.BytesWrapper;
import org.apiphany.json.JsonBuilder;

public class KeyExchangeBytes implements TLSKeyExchange {

	private final BytesWrapper bytes;

	public KeyExchangeBytes(final BytesWrapper bytes) {
		this.bytes = bytes;
	}

	public KeyExchangeBytes(final byte[] bytes) {
		this(new BytesWrapper(bytes));
	}

	public static KeyExchangeBytes from(final InputStream is, int length) throws IOException {
		BytesWrapper bytes = BytesWrapper.from(is, length);

		return new KeyExchangeBytes(bytes);
	}

	@Override
	public byte[] toByteArray() {
		return bytes.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public BytesWrapper getBytes() {
		return bytes;
	}

	@Override
	public int sizeOf() {
		return bytes.sizeOf();
	}

}
