package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Represents the Client Key Exchange message in TLS handshake protocol.
 * <p>
 * This message contains the client's contribution to the key exchange process, which varies depending on the negotiated
 * cipher suite (RSA, DH, ECDH, etc.).
 *
 * @author Radu Sebastian LAZIN
 */
public class ClientKeyExchange implements TLSHandshakeBody {

	/**
	 * The key exchange data structure.
	 */
	private final TLSKeyExchange key;

	/**
	 * Constructs a ClientKeyExchange message.
	 *
	 * @param publicKey the key exchange data structure
	 */
	public ClientKeyExchange(final TLSKeyExchange publicKey) {
		this.key = publicKey;
	}

	/**
	 * Parses a ClientKeyExchange from an input stream.
	 *
	 * @param is the input stream containing key exchange data
	 * @param size the expected size of the key exchange data
	 * @return the parsed ClientKeyExchange object
	 * @throws IOException if an I/O error occurs
	 */
	public static ClientKeyExchange from(final InputStream is, final int size) throws IOException {
		KeyExchangeData key = KeyExchangeData.from(is, size);
		return new ClientKeyExchange(key);
	}

	/**
	 * Returns the binary representation of this message.
	 *
	 * @return byte array containing the key exchange data
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(key.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this message.
	 *
	 * @return JSON string containing key exchange information
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of the key exchange data
	 */
	@Override
	public int sizeOf() {
		return key.sizeOf();
	}

	/**
	 * Returns the handshake message type.
	 *
	 * @return always returns CLIENT_KEY_EXCHANGE
	 */
	@Override
	public HandshakeType getType() {
		return HandshakeType.CLIENT_KEY_EXCHANGE;
	}

	/**
	 * Returns the key exchange data.
	 *
	 * @return the TLSKeyExchange implementation containing the key material
	 */
	public TLSKeyExchange getKey() {
		return key;
	}
}
