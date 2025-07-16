package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

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
		ByteBuffer buffer = ByteBuffer.allocate(size());
		buffer.put(length.toByteArray());
		buffer.put(bytes.toByteArray());
		return buffer.array();
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
