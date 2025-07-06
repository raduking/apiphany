package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class PublicKeyECDHE {

	private Int8 length;

	private BinaryData value;

	public PublicKeyECDHE(final Int8 length, final BinaryData value) {
		this.length = length;
		this.value = value;
	}

	public PublicKeyECDHE(final Int8 length, final byte[] bytes) {
		this(length, new BinaryData(bytes));
	}

	public PublicKeyECDHE(final byte length, final byte[] bytes) {
		this(new Int8(length), bytes);
	}

	public PublicKeyECDHE(final byte[] bytes) {
		this((byte) bytes.length, bytes);
	}

	public static PublicKeyECDHE from(final InputStream is) throws IOException {
		Int8 length = Int8.from(is);
		BinaryData value = BinaryData.from(is, length.getValue());

		return new PublicKeyECDHE(length, value);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(length.toByteArray());
		dos.write(value.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public Int8 getLength() {
		return length;
	}

	public BinaryData getValue() {
		return value;
	}

	public byte size() {
		return (byte) (length.size() + value.size());
	}
}
