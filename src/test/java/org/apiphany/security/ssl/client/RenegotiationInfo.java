package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.Int16;
import org.apiphany.io.Int8;
import org.apiphany.json.JsonBuilder;

public class RenegotiationInfo implements TLSExtension {

	private final ExtensionType type;

	private final Int16 size;

	private final Int8 length;

	public RenegotiationInfo(final ExtensionType type, final Int16 size, final Int8 length) {
		this.type = type;
		this.size = size;
		this.length = length;
	}

	public RenegotiationInfo(final ExtensionType type, final short size, final byte length) {
		this(type, Int16.of(size), Int8.of(length));
	}

	public RenegotiationInfo() {
		this(ExtensionType.RENEGOTIATION_INFO, (short) 0x0001, (byte) 0x00);
	}

	public static RenegotiationInfo from(final InputStream is) throws IOException {
		Int16 int16 = Int16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static RenegotiationInfo from(final InputStream is, final ExtensionType type) throws IOException {
		Int16 size = Int16.from(is);
		Int8 length = Int8.from(is);

		return new RenegotiationInfo(type, size, length);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(size.toByteArray());
		buffer.put(length.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return type.sizeOf() + size.sizeOf() + length.sizeOf();
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public Int16 getSize() {
		return size;
	}

	public Int8 getLength() {
		return length;
	}
}
