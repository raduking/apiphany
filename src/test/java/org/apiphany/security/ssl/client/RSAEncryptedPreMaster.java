package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.Int16;
import org.apiphany.json.JsonBuilder;

public class RSAEncryptedPreMaster implements TLSKeyExchange {

	private final Int16 length;

	private final BytesWrapper bytes;

	public RSAEncryptedPreMaster(final Int16 length, final BytesWrapper bytes) {
		this.length = length;
		this.bytes = bytes;
	}

	public RSAEncryptedPreMaster(final short length, final byte[] bytes) {
		this(Int16.of(length), new BytesWrapper(bytes));
	}

	public RSAEncryptedPreMaster(final byte[] bytes) {
		this((short) bytes.length, bytes);
	}

	public static RSAEncryptedPreMaster from(final InputStream is) throws IOException {
		Int16 length = Int16.from(is);
		BytesWrapper bytes = BytesWrapper.from(is, length.getValue());

		return new RSAEncryptedPreMaster(length, bytes);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
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

	public BytesWrapper getBytes() {
		return bytes;
	}

	@Override
	public int sizeOf() {
		return length.sizeOf() + bytes.sizeOf();
	}

}
