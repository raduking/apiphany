package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

public class RecordHeader implements TLSObject {

	public static final int BYTES = 5;

	private RecordContentType type;

	private Version version;

	private Int16 length;

	public RecordHeader(final RecordContentType type, final Version version, final Int16 length) {
		this.type = type;
		this.version = version;
		this.length = length;
	}

	public RecordHeader(final RecordContentType type, final SSLProtocol sslProtocol, final short length) {
		this(type, Version.of(sslProtocol), new Int16(length));
	}

	public RecordHeader(final RecordContentType type, final SSLProtocol sslProtocol) {
		this(type, sslProtocol, (short) 0x0000);
	}

	public static RecordHeader from(final InputStream is) throws IOException {
		Int8 int8 = Int8.from(is);
		RecordContentType type = RecordContentType.fromValue(int8.getValue());
		Version version = Version.from(is);
		Int16 length = Int16.from(is);

		return new RecordHeader(type, version, length);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(version.toByteArray());
		buffer.put(length.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return type.sizeOf() + version.sizeOf() + length.sizeOf();
	}

	public RecordContentType getType() {
		return type;
	}

	public Version getVersion() {
		return version;
	}

	public Int16 getLength() {
		return length;
	}

}

