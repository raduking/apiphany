package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

import com.fasterxml.jackson.annotation.JsonValue;

public class Int16 implements Sizeable {

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
		short int16 = Bytes.toShort(buffer);

		return new Int16(int16);
	}

	public byte[] toByteArray() {
		return Bytes.from(value);
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
	public int size() {
		return BYTES;
	}
}
