package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class Int16 {

	private short value;

	public Int16(final short length) {
		this.value = length;
	}

	public Int16() {
		this((short) 0x0000);
	}

	public static Int16 from(final InputStream is) throws IOException {
		byte[] shortBuffer = new byte[Bytes.Size.SHORT];
		int bytesRead = is.read(shortBuffer);
		if (Bytes.Size.SHORT != bytesRead) {
			throw new EOFException("Short length");
		}
		short length = Bytes.toShort(shortBuffer);

		return new Int16(length);
	}

	public byte[] toByteArray() {
		return Bytes.from(value);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public short getValue() {
		return value;
	}

}
