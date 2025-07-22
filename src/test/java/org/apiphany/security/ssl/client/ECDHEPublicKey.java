package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;

public class ECDHEPublicKey implements TLSKeyExchange {

	private final UInt8 length;

	private final BytesWrapper value;

	public ECDHEPublicKey(final UInt8 length, final BytesWrapper value) {
		this.length = length;
		this.value = value;
	}

	public ECDHEPublicKey(final UInt8 length, final byte[] bytes) {
		this(length, new BytesWrapper(bytes));
	}

	public ECDHEPublicKey(final byte length, final byte[] bytes) {
		this(UInt8.of(length), bytes);
	}

	public ECDHEPublicKey(final byte[] bytes) {
		this((byte) bytes.length, bytes);
	}

	public static ECDHEPublicKey from(final InputStream is) throws IOException {
		UInt8 length = UInt8.from(is);
		BytesWrapper value = BytesWrapper.from(is, length.getValue());

		return new ECDHEPublicKey(length, value);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(length.toByteArray());
		buffer.put(value.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public UInt8 getLength() {
		return length;
	}

	public BytesWrapper getValue() {
		return value;
	}

	@Override
	public int sizeOf() {
		return length.sizeOf() + value.sizeOf();
	}
}
