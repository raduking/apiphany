package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

public class ClientFinishedEncrypted {

	private RecordHeader recordHeader;

	private BinaryData encryptedData;

	public ClientFinishedEncrypted(final RecordHeader recordHeader, final BinaryData encryptedData) {
		this.recordHeader = recordHeader;
		this.encryptedData = encryptedData;
	}

	public ClientFinishedEncrypted(final RecordHeaderType type, final SSLProtocol sslProtocol, final short bytes, final byte[] payload) {
		this(new RecordHeader(type, Version.of(sslProtocol), new Int16(bytes)), new BinaryData(payload));
	}

	public ClientFinishedEncrypted(final RecordHeaderType type, final SSLProtocol sslProtocol, final byte[] payload) {
		this(type, sslProtocol, (short) payload.length, payload);
	}

	public ClientFinishedEncrypted(final byte[] payload) {
		this(RecordHeaderType.HANDSHAKE, SSLProtocol.TLS_1_2, payload);
	}

	public static ClientFinishedEncrypted from(final InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		BinaryData payload = BinaryData.from(is, recordHeader.getLength().getValue());

		return new ClientFinishedEncrypted(recordHeader, payload);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(recordHeader.toByteArray());
		dos.write(encryptedData.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public RecordHeader getRecordHeader() {
		return recordHeader;
	}

	public BinaryData getEncryptedData() {
		return encryptedData;
	}
}
