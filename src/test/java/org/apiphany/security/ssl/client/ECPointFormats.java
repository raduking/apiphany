package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;

public class ECPointFormats implements TLSExtension {

	private final ExtensionType type;

	private final UInt16 length;

	private final UInt8 formatsSize;

	private final List<UInt8> formats;

	public ECPointFormats(final ExtensionType type, final UInt16 length, final UInt8 formatsSize, final List<UInt8> formats) {
		this.type = type;
		this.length = length;
		this.formatsSize = formatsSize;
		this.formats = formats;
	}

	public ECPointFormats() {
		this(ExtensionType.EC_POINTS_FORMAT, UInt16.of((short) 0x0002), UInt8.of((byte) 0x01), List.of(UInt8.ZERO));
	}

	public static ECPointFormats from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static ECPointFormats from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);
		UInt8 listSize = UInt8.from(is);
		List<UInt8> formats = new ArrayList<>();
		for (int i = 0; i < listSize.getValue(); ++i) {
			UInt8 format = UInt8.from(is);
			formats.add(format);
		}

		return new ECPointFormats(type, length, listSize, formats);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(formatsSize.toByteArray());
		for (UInt8 format : formats) {
			buffer.put(format.toByteArray());
		}
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return type.sizeOf() + length.sizeOf() + formatsSize.sizeOf() + formats.size();
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public UInt16 getLength() {
		return length;
	}

	public UInt8 getFormatsSize() {
		return formatsSize;
	}

	public List<UInt8> getFormats() {
		return formats;
	}
}
