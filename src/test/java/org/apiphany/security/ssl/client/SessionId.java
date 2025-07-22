package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.tls.TLSObject;

public class SessionId implements TLSObject {

	private final UInt8 length;

	private final BytesWrapper value;

	public SessionId(final UInt8 length, final BytesWrapper value) {
		this.length = length;
		this.value = value;
	}

	public SessionId(final UInt8 length, final byte[] value) {
		this(length, new BytesWrapper(value));
	}

	public SessionId(final byte length, final byte[] value) {
		this(UInt8.of(length), value);
	}

	public SessionId(final String value) {
		this((byte) value.length(), value.getBytes(StandardCharsets.US_ASCII));
	}

	public SessionId() {
		this("");
	}

	public static SessionId from(final InputStream is) throws IOException {
		UInt8 length = UInt8.from(is);
		BytesWrapper value = BytesWrapper.from(is, length.getValue());

		return new SessionId(length, value);
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

	@Override
	public int sizeOf() {
		return length.sizeOf() + value.sizeOf();
	}

	public UInt8 getLength() {
		return length;
	}

	public BytesWrapper getValue() {
		return value;
	}

}

