package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class Certificate {

	private Int24 length;

	private byte[] bytes;

	public Certificate(Int24 length, byte[] bytes) {
		this.length = length;
		this.bytes = bytes;
	}

	public Certificate(int length, byte[] bytes) {
		this(new Int24(length), bytes);
	}

	public static Certificate from(InputStream is) throws IOException {
		Int24 length = Int24.from(is);

		byte[] buffer = new byte[length.getValue()];
		is.read(buffer);

		return new Certificate(length, buffer);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public Int24 getLength() {
		return length;
	}

	public byte[] getBytes() {
		return bytes;
	}

}
