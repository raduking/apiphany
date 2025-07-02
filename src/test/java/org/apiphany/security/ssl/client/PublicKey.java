package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class PublicKey {

	private Int8 length;

	private byte[] bytes;

	public PublicKey(Int8 length, byte[] bytes) {
		this.length = length;
		this.bytes = bytes;
	}

	public PublicKey(byte length, byte[] bytes) {
		this(new Int8(length), bytes);
	}

	public PublicKey(byte[] bytes) {
		this((byte) bytes.length, bytes);
	}

	public static PublicKey from(InputStream is) throws IOException {
		Int8 length = Int8.from(is);

		byte[] buffer = new byte[length.getValue()];
		is.read(buffer);

		return new PublicKey(length, buffer);
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

	public Int8 getLength() {
		return length;
	}

	public byte[] getBytes() {
		return bytes;
	}

}
