package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class RenegotiationInfo implements TLSExtension {

	private ExtensionType type;

	private Int16 size;

	private Int8 length;

	public RenegotiationInfo(final ExtensionType type, final Int16 size, final Int8 length) {
		this.type = type;
		this.size = size;
		this.length = length;
	}

	public RenegotiationInfo(final ExtensionType type, final short size, final byte length) {
		this(type, new Int16(size), new Int8(length));
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
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.writeShort(type.value());
			dos.write(size.toByteArray());
			dos.write(length.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return type.size() + size.size() + length.size();
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
