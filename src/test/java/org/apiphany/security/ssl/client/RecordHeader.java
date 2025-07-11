package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;
import org.morphix.lang.function.ThrowingRunnable;

public class RecordHeader implements TLSObject {

	public static final int BYTES = 5;

	private RecordType type;

	private Version version;

	private Int16 length;

	public RecordHeader(final RecordType type, final Version version, final Int16 length) {
		this.type = type;
		this.version = version;
		this.length = length;
	}

	public RecordHeader(final RecordType type, final SSLProtocol sslProtocol, final short length) {
		this(type, Version.of(sslProtocol), new Int16(length));
	}

	public RecordHeader(final RecordType type, final SSLProtocol sslProtocol) {
		this(type, sslProtocol, (short) 0x0000);
	}

	public static RecordHeader from(final InputStream is) throws IOException {
		Int8 int8 = Int8.from(is);
		RecordType type = RecordType.fromValue(int8.getValue());
		Version version = Version.from(is);
		Int16 length = Int16.from(is);

		return new RecordHeader(type, version, length);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.writeByte(getType().value());
			dos.write(version.toByteArray());
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
		return type.size() + version.size() + length.size();
	}

	public RecordType getType() {
		return type;
	}

	public Version getVersion() {
		return version;
	}

	public Int16 getLength() {
		return length;
	}

}

