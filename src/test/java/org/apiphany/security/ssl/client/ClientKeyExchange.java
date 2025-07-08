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

	private PublicKeyECDHE publicKey;

	public ClientKeyExchange(final RecordHeader recordHeader, final HandshakeHeader handshakeHeader, final PublicKeyECDHE publicKey, final boolean setSizes) {
		this.recordHeader = recordHeader;
		this.handshakeHeader = handshakeHeader;
		this.publicKey = publicKey;
		if (setSizes) {
			this.recordHeader.getLength().setValue((short) (HandshakeHeader.BYTES + publicKey.size()));
			this.handshakeHeader.getLength().setValue(publicKey.size());
		}
	}

	public ClientKeyExchange(final RecordHeader recordHeader, final HandshakeHeader handshakeHeader, final PublicKeyECDHE publicKey) {
		this(recordHeader, handshakeHeader, publicKey, true);
	}

	public ClientKeyExchange(final byte[] encryptedPreMasterSecret) {
		this(new RecordHeader(RecordType.HANDSHAKE, SSLProtocol.TLS_1_2),
				new HandshakeHeader(HandshakeType.CLIENT_KEY_EXCHANGE),
				new PublicKeyECDHE(encryptedPreMasterSecret));
	}

	public static ClientKeyExchange from(final InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		PublicKeyECDHE publicKey = PublicKeyECDHE.from(is);

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

	public PublicKeyECDHE getPublicKey() {
		return publicKey;
	}
}
