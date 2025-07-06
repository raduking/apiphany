package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

import com.fasterxml.jackson.annotation.JsonValue;

public class BinaryData implements Sizeable {

	private byte[] bytes;

	public BinaryData(final byte[] bytes) {
		this.bytes = bytes.clone();
	}

	public BinaryData(final int size) {
		this(new byte[size]);
	}

	public static BinaryData from(final InputStream is, final int size) throws IOException {
		byte[] bytes = new byte[size];
		is.read(bytes);

		return new BinaryData(bytes);
	}

	public byte[] toByteArray() {
		return getBytes().clone();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@JsonValue
	public String toHexString() {
		return Bytes.hexString(bytes, "");
	}

	public byte[] getBytes() {
		return bytes;
	}

	@Override
	public int size() {
		return bytes.length;
	}

}
