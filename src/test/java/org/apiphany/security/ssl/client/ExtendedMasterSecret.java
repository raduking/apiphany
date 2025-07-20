package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.Int16;
import org.apiphany.json.JsonBuilder;

public class ExtendedMasterSecret implements TLSExtension {

	private final ExtensionType type;

	private final Int16 length;

	public ExtendedMasterSecret(final ExtensionType type, final Int16 length) {
		this.type = type;
		this.length = length;
	}

	public ExtendedMasterSecret() {
		this(ExtensionType.EXTENDED_MASTER_SECRET, Int16.ZERO);
	}

	public static ExtendedMasterSecret from(final InputStream is) throws IOException {
		Int16 int16 = Int16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static ExtendedMasterSecret from(final InputStream is, final ExtensionType type) throws IOException {
		Int16 length = Int16.from(is);

		return new ExtendedMasterSecret(type, length);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return type.sizeOf() + length.sizeOf();
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public Int16 getLength() {
		return length;
	}
}

