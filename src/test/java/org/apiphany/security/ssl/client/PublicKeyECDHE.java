package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class PublicKeyECDHE {

	private Int8 length;

	private byte[] bytes;

	public PublicKeyECDHE(final Int8 length, final byte[] bytes) {
		this.length = length;
		this.bytes = bytes;
	}

	public PublicKeyECDHE(final byte length, final byte[] bytes) {
		this(new Int8(length), bytes);
	}

	public PublicKeyECDHE(final byte[] bytes) {
		this((byte) bytes.length, bytes);
	}

	public static PublicKeyECDHE from(final InputStream is) throws IOException {
		Int8 length = Int8.from(is);

		byte[] buffer = new byte[length.getValue()];
		is.read(buffer);

		return new PublicKeyECDHE(length, buffer);
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

	public String getHexBytes() {
		return Bytes.hexString(bytes, "");
	}

	public byte size() {
		return (byte) (length.size() + bytes.length);
	}
}
