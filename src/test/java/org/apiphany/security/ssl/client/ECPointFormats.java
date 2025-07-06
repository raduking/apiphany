package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apiphany.json.JsonBuilder;

public class ECPointFormats implements Sizeable {

	private ExtensionType type = ExtensionType.EC_POINTS_FORMAT;

	private Int16 size = new Int16((short) 0x0002);

	private Int8 listSize = new Int8((byte) 0x01);

	private Int8 format = new Int8((byte) 0x00);

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeShort(type.value());
		dos.write(size.toByteArray());
		dos.write(listSize.toByteArray());
		dos.write(format.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return type.size() + size.size() + listSize.size() + format.size();
	}

	public ExtensionType getType() {
		return type;
	}

	public Int16 getSize() {
		return size;
	}

	public Int8 getListSize() {
		return listSize;
	}

	public Int8 getFormat() {
		return format;
	}
}
