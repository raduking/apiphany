package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class Int24 {

	private int value;

	public Int24(final int value) {
		this.value = value;
	}

	public Int24() {
		this((short) 0x0000);
	}

	public static Int24 from(final InputStream is) throws IOException {
		byte[] buffer = new byte[Bytes.Size.BITS24];
		int bytesRead = is.read(buffer);
		if (Bytes.Size.BITS24 != bytesRead) {
			throw new EOFException("Short length");
		}
		int length = ((buffer[0] & 0xFF) << 16) |
				((buffer[1] & 0xFF) << 8) |
				(buffer[2] & 0xFF);
		return new Int24(length);
	}

	public byte[] toByteArray() {
		return new byte[] {
				(byte) ((value >> 16) & 0xFF),
				(byte) ((value >> 8) & 0xFF),
				(byte) (value & 0xFF)
		};
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public int getValue() {
		return value;
	}

}
