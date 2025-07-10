package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;
import org.morphix.lang.function.ThrowingRunnable;

public class ChangeCipherSpec implements TLSObject {

	public static final int SIZE = 6;

	private RecordHeader recordHeader;

	private Int8 payload;

	public ChangeCipherSpec(final RecordHeader recordHeader, final Int8 payload) {
		this.recordHeader = recordHeader;
		this.payload = payload;
	}

	public ChangeCipherSpec(final RecordType type, final SSLProtocol sslProtocol, final short bytes, final byte payload) {
		this(new RecordHeader(type, Version.of(sslProtocol), new Int16(bytes)), new Int8(payload));
	}

	public ChangeCipherSpec(final RecordType type, final SSLProtocol sslProtocol) {
		this(type, sslProtocol, (short) 0x0001, (byte) 0x01);
	}

	public ChangeCipherSpec() {
		this(RecordType.CHANGE_CIPHER_SPEC, SSLProtocol.TLS_1_2);
	}

	public static ChangeCipherSpec from(final InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		Int8 payload = Int8.from(is);

		return new ChangeCipherSpec(recordHeader, payload);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(recordHeader.toByteArray());
			dos.write(payload.toByteArray());
		}).run();;
		return bos.toByteArray();
	}

	@Override
	public int size() {
		return SIZE;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public RecordHeader getRecordHeader() {
		return recordHeader;
	}

	public Int8 getPayload() {
		return payload;
	}
}
