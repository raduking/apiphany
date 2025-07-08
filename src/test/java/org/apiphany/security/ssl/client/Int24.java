package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

import com.fasterxml.jackson.annotation.JsonValue;

public class Int24 implements Sizeable, BinaryRepresentable {

	public static final int BYTES = 3;

	private int value;

	public Int24(final int value) {
		this.value = value;
	}

	public Int24() {
		this((short) 0x0000);
	}

	public static Int24 from(final InputStream is) throws IOException {
		byte[] buffer = new byte[BYTES];
		int bytesRead = is.read(buffer);
		if (BYTES != bytesRead) {
			throw new EOFException("Error reading " + BYTES + " bytes");
		}
		int int24 = ((buffer[0] & 0xFF) << 16) |
				((buffer[1] & 0xFF) << 8) |
				(buffer[2] & 0xFF);

		return new Int24(int24);
	}

	@Override
	public byte[] toByteArray() {
		return toByteArray(value);
	}

	public static byte[] toByteArray(int value) {
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

	@JsonValue
	public int getValue() {
		return value;
	}

	public void setValue(final int value) {
		this.value = value;
	}

	@Override
	public int size() {
		return BYTES;
	}
}
