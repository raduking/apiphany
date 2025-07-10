package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class PublicKeyRSA implements TLSObject {

	private Int16 length;

	private BinaryData bytes;

	public PublicKeyRSA(final Int16 length, final BinaryData bytes) {
		this.length = length;
		this.bytes = bytes;
	}

	public PublicKeyRSA(final short length, final byte[] bytes) {
		this(new Int16(length), new BinaryData(bytes));
	}

	public PublicKeyRSA(final byte[] bytes) {
		this((short) bytes.length, bytes);
	}

	public static PublicKeyRSA from(final InputStream is) throws IOException {
		Int16 length = Int16.from(is);
		BinaryData bytes = BinaryData.from(is, length.getValue());

		return new PublicKeyRSA(length, bytes);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(length.toByteArray());
			dos.write(bytes.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public Int16 getLength() {
		return length;
	}

	public BinaryData getBytes() {
		return bytes;
	}

	@Override
	public int size() {
		return length.size() + bytes.size();
	}

}
