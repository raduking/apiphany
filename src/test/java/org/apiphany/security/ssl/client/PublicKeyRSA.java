package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class PublicKeyRSA {

	private Int16 length;

	private byte[] bytes;

	public PublicKeyRSA(final Int16 length, final byte[] bytes) {
		this.length = length;
		this.bytes = bytes;
	}

	public PublicKeyRSA(final short length, final byte[] bytes) {
		this(new Int16(length), bytes);
	}

	public PublicKeyRSA(final byte[] bytes) {
		this((short) bytes.length, bytes);
	}

	public static PublicKeyRSA from(final InputStream is) throws IOException {
		Int16 length = Int16.from(is);

		byte[] buffer = new byte[length.getValue()];
		is.read(buffer);

		return new PublicKeyRSA(length, buffer);
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

	public Int16 getLength() {
		return length;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public String getHexBytes() {
		return Bytes.hexString(bytes, "");
	}

	public short size() {
		return (short) (length.size() + bytes.length);
	}

}
