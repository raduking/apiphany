package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

public class ClientKeyExchange {

	private RecordHeader recordHeader;

	private HandshakeHeader handshakeHeader;

	private PublicKeyRSA publicKey;

	public ClientKeyExchange(RecordHeader recordHeader, HandshakeHeader handshakeHeader, PublicKeyRSA publicKey, boolean updateSizes) {
		this.recordHeader = recordHeader;
		this.handshakeHeader = handshakeHeader;
		this.publicKey = publicKey;
		if (updateSizes) {
			this.recordHeader.getMessageLength().setValue((short) (HandshakeHeader.SIZE + publicKey.size()));
			this.handshakeHeader.getMessageLength().setValue(publicKey.size());
		}
	}

	public ClientKeyExchange(RecordHeader recordHeader, HandshakeHeader handshakeHeader, PublicKeyRSA publicKey) {
		this(recordHeader, handshakeHeader, publicKey, true);
	}

	public ClientKeyExchange(final byte[] encryptedPreMasterSecret) {
		this(new RecordHeader(RecordHeaderType.HANDSHAKE_RECORD, SSLProtocol.TLS_1_2),
				new HandshakeHeader(HandshakeMessageType.CLIENT_KEY_EXCHANGE),
				new PublicKeyRSA(encryptedPreMasterSecret));
	}

	public static ClientKeyExchange from(InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		PublicKeyRSA publicKey = PublicKeyRSA.from(is);

		return new ClientKeyExchange(recordHeader, handshakeHeader, publicKey, false);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(recordHeader.toByteArray());
		dos.write(handshakeHeader.toByteArray());
		dos.write(publicKey.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public RecordHeader getRecordHeader() {
		return recordHeader;
	}

	public HandshakeHeader getHandshakeHeader() {
		return handshakeHeader;
	}

	public PublicKeyRSA getPublicKey() {
		return publicKey;
	}
}
