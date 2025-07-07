package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

public class ChangeCipherSpec {

	public static final int SIZE = 6;

	private RecordHeader recordHeader;

	private Int8 payload;

	public ChangeCipherSpec(final RecordHeader recordHeader, final Int8 payload) {
		this.recordHeader = recordHeader;
		this.payload = payload;
	}

	public ChangeCipherSpec(final RecordHeaderType type, final SSLProtocol sslProtocol, final short bytes, final byte payload) {
		this(new RecordHeader(type, Version.of(sslProtocol), new Int16(bytes)), new Int8(payload));
	}

	public ChangeCipherSpec(final RecordHeaderType type, final SSLProtocol sslProtocol) {
		this(type, sslProtocol, (short) 0x0001, (byte) 0x01);
	}

	public ChangeCipherSpec() {
		this(RecordHeaderType.CHANGE_CIPHER_SPEC, SSLProtocol.TLS_1_2);
	}

	public static ChangeCipherSpec from(final InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		Int8 payload = Int8.from(is);

		return new ChangeCipherSpec(recordHeader, payload);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(recordHeader.toByteArray());
		dos.write(payload.toByteArray());

		return bos.toByteArray();
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
