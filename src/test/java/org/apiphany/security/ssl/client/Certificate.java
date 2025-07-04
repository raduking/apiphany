package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class Certificate {

	private Int24 length;

	private byte[] bytes;

	public Certificate(final Int24 length, final byte[] bytes) {
		this.length = length;
		this.bytes = bytes;
	}

	public Certificate(final int length, final byte[] bytes) {
		this(new Int24(length), bytes);
	}

	public static Certificate from(final InputStream is) throws IOException {
		Int24 length = Int24.from(is);

		byte[] buffer = new byte[length.getValue()];
		is.read(buffer);

		return new Certificate(length, buffer);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(length.toByteArray());
		dos.write(bytes);

		return bos.toByteArray();
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
