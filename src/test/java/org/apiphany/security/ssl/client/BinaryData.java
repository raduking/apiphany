package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Hex;

import com.fasterxml.jackson.annotation.JsonValue;

public class BinaryData implements TLSObject {

	protected static final byte[] EMPTY = new byte[] { };

	private byte[] bytes;

	public BinaryData(final byte[] bytes) {
		this.bytes = bytes;
	}

	public BinaryData(final int size) {
		this(new byte[size]);
	}

	public BinaryData() {
		this(EMPTY);
	}

	public static BinaryData from(final InputStream is, final int size) throws IOException {
		if (0 >= size) {
			return new BinaryData();
		}
		byte[] bytes = new byte[size];
		int bytesRead = is.read(bytes);
		if (size != bytesRead) {
			throw new EOFException("Error reading " + size + " bytes");
		}

		return new BinaryData(bytes);
	}

	@Override
	public byte[] toByteArray() {
		return getBytes();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@JsonValue
	public String toHexString() {
		return Hex.string(bytes, "");
	}

	public byte[] getBytes() {
		return bytes;
	}

	@Override
	public int sizeOf() {
		return bytes.length;
	}
}
