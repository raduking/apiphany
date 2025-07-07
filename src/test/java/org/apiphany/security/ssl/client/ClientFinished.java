package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class ClientFinished {

	private HandshakeHeader recordHeader;

	private BinaryData encryptedData;

	public ClientFinished(final HandshakeHeader recordHeader, final BinaryData encryptedData) {
		this.recordHeader = recordHeader;
		this.encryptedData = encryptedData;
	}

	public ClientFinished(final HandshakeMessageType type, final int length, final byte[] payload) {
		this(new HandshakeHeader(type, new Int24(length)), new BinaryData(payload));
	}

	public ClientFinished(final byte[] payload) {
		this(HandshakeMessageType.FINISHED, payload.length, payload);
	}

	public static ClientFinished from(final InputStream is) throws IOException {
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		BinaryData payload = BinaryData.from(is, handshakeHeader.getLength().getValue());

		return new ClientFinished(handshakeHeader, payload);
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

	public HandshakeHeader getRecordHeader() {
		return recordHeader;
	}

	public BinaryData getEncryptedData() {
		return encryptedData;
	}
}
