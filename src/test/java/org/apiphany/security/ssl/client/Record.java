package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

public class Record {

	public static final int SIZE = 6;

	private RecordHeaderType type;

	private Version version;

	private Int16 bytes;

	private Int8 payload;

	public Record(final RecordHeaderType type, final Version version, final Int16 bytes, final Int8 payload) {
		this.type = type;
		this.version = version;
		this.bytes = bytes;
		this.payload = payload;
	}

	public Record(final RecordHeaderType type, final SSLProtocol sslProtocol, final short bytes, final byte payload) {
		this(type, Version.of(sslProtocol), new Int16(bytes), new Int8(payload));
	}

	public Record(final RecordHeaderType type, final SSLProtocol sslProtocol) {
		this(type, sslProtocol, (short) 0x0001, (byte) 0x01);
	}

	public static Record from(final InputStream is) throws IOException {
		int firstByte = is.read();
		if (-1 == firstByte) {
			throw new EOFException("Connection closed by server");
		}
		RecordHeaderType type = RecordHeaderType.fromValue((byte) firstByte);

		Version version = Version.from(is);

		Int16 bytes = Int16.from(is);

		Int8 payload = Int8.from(is);

		return new Record(type, version, bytes, payload);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeByte(getType().value());
		dos.write(version.toByteArray());
		dos.write(bytes.toByteArray());
		dos.write(payload.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public RecordHeaderType getType() {
		return type;
	}

	public Version getVersion() {
		return version;
	}

	public Int16 getBytes() {
		return bytes;
	}

	public Int8 getPayload() {
		return payload;
	}
}

