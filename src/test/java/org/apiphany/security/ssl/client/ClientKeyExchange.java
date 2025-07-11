package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

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
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(publicKey.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return publicKey.size();
	}

	@Override
	public HandshakeType type() {
		return HandshakeType.CLIENT_KEY_EXCHANGE;
	}

	public PublicKeyECDHE getPublicKey() {
		return publicKey;
	}
}
