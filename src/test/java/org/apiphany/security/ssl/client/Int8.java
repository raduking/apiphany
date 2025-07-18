package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.BinaryRepresentable;
import org.apiphany.lang.ByteSizeable;

import com.fasterxml.jackson.annotation.JsonValue;

public class Int8 implements ByteSizeable, BinaryRepresentable {

	public static final int BYTES = 1;
	public static final Int8 ZERO = Int8.of((byte) 0x00);

	private final byte value;

	protected Int8(final byte value) {
		this.value = value;
	}

	public static Int8 of(final byte value) {
		return new Int8(value);
	}

	public static Int8 from(final InputStream is) throws IOException {
		byte[] buffer = new byte[BYTES];
		int bytesRead = is.read(buffer);
		if (BYTES != bytesRead) {
			throw new EOFException("Error reading " + BYTES + " bytes");
		}
		return Int8.of(buffer[0]);
	}

	@Override
	public byte[] toByteArray() {
		return toByteArray(value);
	}

	public static byte[] toByteArray(byte value) {
		return new byte[] {
				(byte) (value & 0xFF)
		};
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@JsonValue
	public byte getValue() {
		return value;
	}

	@Override
	public int sizeOf() {
		return BYTES;
	}
}
