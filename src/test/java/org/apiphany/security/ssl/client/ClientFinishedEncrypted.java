package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;
import org.morphix.lang.function.ThrowingRunnable;

public class ClientFinishedEncrypted implements TLSObject {

	private RecordHeader recordHeader;

	private BinaryData encryptedData;

	public ClientFinishedEncrypted(final RecordHeader recordHeader, final BinaryData encryptedData) {
		this.recordHeader = recordHeader;
		this.encryptedData = encryptedData;
	}

	public ClientFinishedEncrypted(final RecordType type, final SSLProtocol sslProtocol, final short bytes, final byte[] payload) {
		this(new RecordHeader(type, Version.of(sslProtocol), new Int16(bytes)), new BinaryData(payload));
	}

	public ClientFinishedEncrypted(final RecordType type, final SSLProtocol sslProtocol, final byte[] payload) {
		this(type, sslProtocol, (short) payload.length, payload);
	}

	public ClientFinishedEncrypted(final byte[] payload) {
		this(RecordType.HANDSHAKE, SSLProtocol.TLS_1_2, payload);
	}

	public static ClientFinishedEncrypted from(final InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		BinaryData payload = BinaryData.from(is, recordHeader.getLength().getValue());

		return new ClientFinishedEncrypted(recordHeader, payload);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(recordHeader.toByteArray());
			dos.write(encryptedData.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return recordHeader.size() + encryptedData.size();
	}

	public RecordHeader getRecordHeader() {
		return recordHeader;
	}

	public BinaryData getEncryptedData() {
		return encryptedData;
	}
}
