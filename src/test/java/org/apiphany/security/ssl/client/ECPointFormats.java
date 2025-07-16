package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;

public class ECPointFormats implements TLSExtension {

	private ExtensionType type;

	private Int16 length;

	private Int8 listSize;

	private List<Int8> formats;

	public ECPointFormats(final ExtensionType type, final Int16 length, final Int8 listSize, final List<Int8> formats) {
		this.type = type;
		this.length = length;
		this.listSize = listSize;
		this.formats = formats;
	}

	public ECPointFormats() {
		this(ExtensionType.EC_POINTS_FORMAT, new Int16((short) 0x0002), new Int8((byte) 0x01), List.of(new Int8((byte) 0x00)));
	}

	public static ECPointFormats from(final InputStream is) throws IOException {
		Int16 int16 = Int16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static ECPointFormats from(final InputStream is, final ExtensionType type) throws IOException {
		Int16 length = Int16.from(is);
		Int8 listSize = Int8.from(is);
		List<Int8> formats = new ArrayList<>();
		for (int i = 0; i < listSize.getValue(); ++i) {
			Int8 format = Int8.from(is);
			formats.add(format);
		}

		return new ECPointFormats(type, length, listSize, formats);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(listSize.toByteArray());
		for (Int8 format : formats) {
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
		return type.sizeOf() + length.sizeOf() + listSize.sizeOf() + formats.size();
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public Int16 getLength() {
		return length;
	}

	public Int8 getListSize() {
		return listSize;
	}

	public List<Int8> getFormats() {
		return formats;
	}
}
