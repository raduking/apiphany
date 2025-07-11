package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

import com.fasterxml.jackson.annotation.JsonValue;

public class Int64 implements Sizeable, BinaryRepresentable {

	public static final int BYTES = 8;

	private long value;

	public Int64(final long value) {
		this.value = value;
	}

	public Int64() {
		this(0x0000000000000000);
	}

	public static Int64 from(final InputStream is) throws IOException {
		byte[] buffer = new byte[BYTES];
		int bytesRead = is.read(buffer);
		if (BYTES != bytesRead) {
			throw new EOFException("Error reading " + BYTES + " bytes");
		}
		long int64 = 0;
		for (int i = BYTES; i > 0; --i) {
			int64 |= ((long) buffer[BYTES - i] & 0xFF) << ((i - 1) * 8);
		}

		return new Int64(int64);
	}

	@Override
	public byte[] toByteArray() {
		return toByteArray(value);
	}

	public static byte[] toByteArray(final long value) {
		ByteBuffer buffer = ByteBuffer.allocate(BYTES);
		buffer.putLong(value);
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@JsonValue
	public long getValue() {
		return value;
	}

	public void setValue(final long value) {
		this.value = value;
	}

	@Override
	public int size() {
		return BYTES;
	}
}
