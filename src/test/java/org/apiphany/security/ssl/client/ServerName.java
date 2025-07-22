package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.tls.TLSObject;

public class ServerName implements TLSObject {

	private final UInt16 size;

	private final UInt8 type;

	private final UInt16 length;

	private final BytesWrapper name;

	public ServerName(final UInt16 size, final UInt8 type, final UInt16 length, final BytesWrapper name) {
		this.size = size;
		this.type = type;
		this.length = length;
		this.name = name;
	}

	public ServerName(final String name) {
		this(
				UInt16.of((short) (UInt8.BYTES + UInt16.BYTES + name.length())),
				UInt8.ZERO,
				UInt16.of((short) name.length()),
				new BytesWrapper(name.getBytes(StandardCharsets.US_ASCII))
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
		UInt16 size = UInt16.from(is);
		UInt8 type = UInt8.from(is);
		UInt16 length = UInt16.from(is);
		BytesWrapper name = BytesWrapper.from(is, length.getValue());

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

	public UInt16 getSize() {
		return size;
	}

	public UInt8 getType() {
		return type;
	}

	public UInt16 getLength() {
		return length;
	}

	public BytesWrapper getName() {
		return name;
	}

	public String getNameASCII() {
		return new String(name.toByteArray(), StandardCharsets.US_ASCII);
	}
}
