package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt24;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.tls.TLSObject;

public class Certificate implements TLSObject {

	private final UInt24 length;

	private final BytesWrapper data;

	public Certificate(final UInt24 length, final BytesWrapper data) {
		this.length = length;
		this.data = data;
	}

	public Certificate(final UInt24 length, final byte[] bytes) {
		this(length, new BytesWrapper(bytes));
	}

	public Certificate(final int length, final byte[] bytes) {
		this(UInt24.of(length), bytes);
	}

	public static Certificate from(final InputStream is) throws IOException {
		UInt24 length = UInt24.from(is);
		BytesWrapper data = BytesWrapper.from(is, length.getValue());

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

	public UInt24 getLength() {
		return length;
	}

	public BytesWrapper getData() {
		return data;
	}
}
