package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

public class Certificate implements TLSObject {

	private final Int24 length;

	private final BinaryData data;

	public Certificate(final Int24 length, final BinaryData data) {
		this.length = length;
		this.data = data;
	}

	public Certificate(final Int24 length, final byte[] bytes) {
		this(length, new BinaryData(bytes));
	}

	public Certificate(final int length, final byte[] bytes) {
		this(new Int24(length), bytes);
	}

	public static Certificate from(final InputStream is) throws IOException {
		Int24 length = Int24.from(is);
		BinaryData data = BinaryData.from(is, length.getValue());

		return new Certificate(length, data);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(length.toByteArray());
		buffer.put(data.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return length.sizeOf() + data.sizeOf();
	}

	public Int24 getLength() {
		return length;
	}

	public BinaryData getData() {
		return data;
	}
}
