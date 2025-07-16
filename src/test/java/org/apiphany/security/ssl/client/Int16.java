package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.BinaryRepresentable;
import org.apiphany.lang.ByteSizeable;

import com.fasterxml.jackson.annotation.JsonValue;

public class Int16 implements ByteSizeable, BinaryRepresentable {

	public static final int BYTES = 2;

	private short value;

	public Int16(final short length) {
		this.value = length;
	}

	public Int16() {
		this((short) 0x0000);
	}

	public static Int16 from(final InputStream is) throws IOException {
		byte[] buffer = new byte[BYTES];
		int bytesRead = is.read(buffer);
		if (BYTES != bytesRead) {
			throw new EOFException("Error reading " + BYTES + " bytes");
		}
		short int16 = (short) (
				((short) ((buffer[0] & 0xFF) << 8)) |
				((short) (buffer[1] & 0xFF))
		);

		return new Int16(int16);
	}

	@Override
	public byte[] toByteArray() {
		return toByteArray(value);
	}

	public static byte[] toByteArray(short value) {
		return new byte[] {
				(byte) ((value >> 8) & 0xFF),
				(byte) (value & 0xFF)
		};
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@JsonValue
	public short getValue() {
		return value;
	}

	public void setValue(final short value) {
		this.value = value;
	}

	@Override
	public int sizeOf() {
		return BYTES;
	}
}
