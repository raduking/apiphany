package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class Signature {

	private Int16 reserved;

	private Int16 length;

	private byte[] bytes;

	public Signature(final Int16 reserved, final Int16 length, final byte[] bytes) {
		this.reserved = reserved;
		this.length = length;
		this.bytes = bytes;
	}

	public Signature(final short length, final byte[] bytes) {
		this(new Int16((short) 0x0000), new Int16(length), bytes);
	}

	public static Signature from(final InputStream is) throws IOException {
		Int16 reserved = Int16.from(is);
		Int16 length = Int16.from(is);

		byte[] buffer = new byte[length.getValue()];
		int bytesRead = is.read(buffer);
		if (length.getValue() != bytesRead) {
			throw new EOFException("Error reading " + length.getValue() + " bytes");
		}

		return new Signature(reserved, length, buffer);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public Int16 getReserved() {
		return reserved;
	}

	public Int16 getLength() {
		return length;
	}

	public byte[] getBytes() {
		return bytes;
	}

}
