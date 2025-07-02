package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apiphany.json.JsonBuilder;

public class SessionId {

	private Int8 length;

	private byte[] value;

	public SessionId(Int8 length, byte[] value) {
		this.length = length;
		this.value = value;
	}

	public SessionId(byte length, byte[] value) {
		this(new Int8(length), value);
	}

	public SessionId(String value) {
		this((byte) value.length(), value.getBytes(StandardCharsets.US_ASCII));
	}

	public SessionId() {
		this("");
	}

	public static SessionId from(InputStream is) throws IOException {
		Int8 length = Int8.from(is);
		byte[] value = new byte[length.getValue()];
		is.read(value);

		return new SessionId(length, value);
	}

	public byte[] toByteArray() {
		byte size = length.getValue();
		byte[] result = new byte[size + 1];
		result[0] = size;
		for (int i = 0; i < size; ++i) {
			result[i + 1] = value[i];
		}
		return result;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public Int8 getLength() {
		return length;
	}

	public byte[] getValue() {
		return value;
	}
}

