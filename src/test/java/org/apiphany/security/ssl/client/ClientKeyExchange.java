package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

public class ClientKeyExchange implements TLSHandshakeBody {

	private final TLSKeyExchange key;

	public ClientKeyExchange(final TLSKeyExchange publicKey) {
		this.key = publicKey;
	}

	public static ClientKeyExchange from(final InputStream is, int size) throws IOException {
		KeyExchangeBytes key = KeyExchangeBytes.from(is, size);

		return new ClientKeyExchange(key);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(key.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return key.sizeOf();
	}

	@Override
	public HandshakeType getType() {
		return HandshakeType.CLIENT_KEY_EXCHANGE;
	}

	public TLSKeyExchange getKey() {
		return key;
	}
}
