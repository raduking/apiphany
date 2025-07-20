package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.Int16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.tls.TLSObject;

public class Signature implements TLSObject {

	private final Int16 reserved;

	private final Int16 length;

	private final BytesWrapper value;

	public Signature(final Int16 reserved, final Int16 length, final BytesWrapper value) {
		this.reserved = reserved;
		this.length = length;
		this.value = value;
	}

	public Signature(final Int16 reserved, final Int16 length, final byte[] bytes) {
		this(reserved, length, new BytesWrapper(bytes));
	}

	public Signature(final short length, final byte[] bytes) {
		this(Int16.ZERO, Int16.of(length), bytes);
	}

	public static Signature from(final InputStream is) throws IOException {
		Int16 reserved = Int16.from(is);
		Int16 length = Int16.from(is);
		BytesWrapper value = BytesWrapper.from(is, length.getValue());

		return new Signature(reserved, length, value);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(reserved.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(value.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return reserved.sizeOf() + length.sizeOf() + value.sizeOf();
	}

	public Int16 getReserved() {
		return reserved;
	}

	public Int16 getLength() {
		return length;
	}

	public BytesWrapper getValue() {
		return value;
	}
}
