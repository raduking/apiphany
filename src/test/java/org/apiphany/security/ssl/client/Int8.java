package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class Int8 {

	public static final int BYTES = 1;

	private byte value;

	public Int8(final byte value) {
		this.value = value;
	}

	public Int8() {
		this((byte) 0x00);
	}

	public static Int8 from(final InputStream is) throws IOException {
		byte[] buffer = new byte[BYTES];
		int bytesRead = is.read(buffer);
		if (BYTES != bytesRead) {
			throw new EOFException("Error reading " + BYTES + " bytes");
		}

		return new Int8(buffer[0]);
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

	public void setValue(byte value) {
		this.value = value;
	}

	public int size() {
		return BYTES;
	}
}
