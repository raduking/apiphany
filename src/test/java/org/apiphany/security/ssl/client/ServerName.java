package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apiphany.json.JsonBuilder;

public class ServerName implements TLSObject {

	private final Int16 size;

	private final Int8 type;

	private final Int16 length;

	private final BinaryData name;

	public ServerName(final Int16 size, final Int8 type, final Int16 length, final BinaryData name) {
		this.size = size;
		this.type = type;
		this.length = length;
		this.name = name;
	}

	public ServerName(final String name) {
		this(
				new Int16((short) (Int8.BYTES + Int16.BYTES + name.length())),
				new Int8((byte) 0x00),
				new Int16((short) name.length()),
				new BinaryData(name.getBytes(StandardCharsets.US_ASCII))
		);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(size.toByteArray());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(name.toByteArray());
		return buffer.array();
	}

	public static ServerName from(final InputStream is) throws IOException {
		Int16 size = Int16.from(is);
		Int8 type = Int8.from(is);
		Int16 length = Int16.from(is);
		BinaryData name = BinaryData.from(is, length.getValue());

		return new ServerName(size, type, length, name);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return size.sizeOf() + type.sizeOf() + length.sizeOf() + name.sizeOf();
	}

	public Int16 getSize() {
		return size;
	}

	public Int8 getType() {
		return type;
	}

	public Int16 getLength() {
		return length;
	}

	public BinaryData getName() {
		return name;
	}

	public String getNameASCII() {
		return new String(name.getBytes(), StandardCharsets.US_ASCII);
	}
}
