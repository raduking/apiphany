package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class Int8 {

	private byte value;

	public Int8(final byte length) {
		this.value = length;
	}

	public Int8() {
		this((byte) 0x00);
	}

	public static Int8 from(final InputStream is) throws IOException {
		int length = is.read();

		return new Int8((byte) length);
	}

	public byte[] toByteArray() {
		return Bytes.from(value);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public byte getValue() {
		return value;
	}

}
