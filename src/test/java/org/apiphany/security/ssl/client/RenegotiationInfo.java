package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;

public class RenegotiationInfo implements TLSExtension {

	private final ExtensionType type;

	private final UInt16 size;

	private final UInt8 length;

	public RenegotiationInfo(final ExtensionType type, final UInt16 size, final UInt8 length) {
		this.type = type;
		this.size = size;
		this.length = length;
	}

	public RenegotiationInfo(final ExtensionType type, final short size, final byte length) {
		this(type, UInt16.of(size), UInt8.of(length));
	}

	public RenegotiationInfo() {
		this(ExtensionType.RENEGOTIATION_INFO, (short) 0x0001, (byte) 0x00);
	}

	public static RenegotiationInfo from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static RenegotiationInfo from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 size = UInt16.from(is);
		UInt8 length = UInt8.from(is);

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

	public UInt16 getSize() {
		return size;
	}

	public UInt8 getLength() {
		return length;
	}
}
