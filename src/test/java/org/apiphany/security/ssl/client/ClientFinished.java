package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class ClientFinished implements TLSObject {

	private HandshakeHeader recordHeader;

	private BinaryData encryptedData;

	public ClientFinished(final HandshakeHeader recordHeader, final BinaryData encryptedData) {
		this.recordHeader = recordHeader;
		this.encryptedData = encryptedData;
	}

	public ClientFinished(final HandshakeType type, final int length, final byte[] payload) {
		this(new HandshakeHeader(type, new Int24(length)), new BinaryData(payload));
	}

	public ClientFinished(final byte[] payload) {
		this(HandshakeType.FINISHED, payload.length, payload);
	}

	public static ClientFinished from(final InputStream is) throws IOException {
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		BinaryData payload = BinaryData.from(is, handshakeHeader.getLength().getValue());

		return new ClientFinished(handshakeHeader, payload);
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

	public HandshakeHeader getRecordHeader() {
		return recordHeader;
	}

	public BinaryData getEncryptedData() {
		return encryptedData;
	}
}
