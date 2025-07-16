package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

public class ClientKeyExchange implements TLSHandshakeBody {

	private PublicKeyECDHE publicKey;

	public ClientKeyExchange(final PublicKeyECDHE publicKey) {
		this.publicKey = publicKey;
	}

	public ClientKeyExchange(final byte[] encryptedPreMasterSecret) {
		this(new PublicKeyECDHE(encryptedPreMasterSecret));
	}

	public static ClientKeyExchange from(final InputStream is) throws IOException {
		PublicKeyECDHE publicKey = PublicKeyECDHE.from(is);

		return new ClientKeyExchange(publicKey);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(publicKey.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return publicKey.sizeOf();
	}

	@Override
	public HandshakeType type() {
		return HandshakeType.CLIENT_KEY_EXCHANGE;
	}

	public PublicKeyECDHE getPublicKey() {
		return publicKey;
	}
}
