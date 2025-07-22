package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

/**
 * Represents the ServerKeyExchange message in TLS handshake protocol.
 * <p>
 * This message contains the server's ephemeral ECDHE public key and a signature that proves the server owns the private
 * key corresponding to its certificate.
 *
 * @author Radu Sebastian LAZIN
 */
public class ServerKeyExchange implements TLSHandshakeBody {

	/**
	 * The elliptic curve parameters used for key exchange.
	 */
	private final CurveInfo curveInfo;

	/**
	 * The server's ephemeral ECDHE public key.
	 */
	private final ECDHEPublicKey publicKey;

	/**
	 * The signature over the handshake parameters.
	 */
	private final Signature signature;

	/**
	 * Constructs a ServerKeyExchange message.
	 *
	 * @param curveInfo the elliptic curve parameters
	 * @param publicKey the server's ephemeral public key
	 * @param signature the signature over handshake parameters
	 */
	public ServerKeyExchange(final CurveInfo curveInfo, final ECDHEPublicKey publicKey, final Signature signature) {
		this.curveInfo = curveInfo;
		this.publicKey = publicKey;
		this.signature = signature;
	}

	/**
	 * Parses a ServerKeyExchange from an input stream.
	 *
	 * @param is the input stream containing the message data
	 * @return the parsed ServerKeyExchange object
	 * @throws IOException if an I/O error occurs
	 */
	public static ServerKeyExchange from(final InputStream is) throws IOException {
		CurveInfo curveInfo = CurveInfo.from(is);
		ECDHEPublicKey publicKey = ECDHEPublicKey.from(is);
		Signature signature = Signature.from(is);
		return new ServerKeyExchange(curveInfo, publicKey, signature);
	}

	/**
	 * Returns the binary representation of this ServerKeyExchange.
	 *
	 * @return byte array containing curve info, public key and signature
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(curveInfo.toByteArray());
		buffer.put(publicKey.toByteArray());
		buffer.put(signature.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this ServerKeyExchange.
	 *
	 * @return JSON string containing key exchange information
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of all fields combined
	 */
	@Override
	public int sizeOf() {
		return curveInfo.sizeOf() + publicKey.sizeOf() + signature.sizeOf();
	}

	/**
	 * Returns the handshake message type.
	 *
	 * @return always returns SERVER_KEY_EXCHANGE
	 */
	@Override
	public HandshakeType getType() {
		return HandshakeType.SERVER_KEY_EXCHANGE;
	}

	/**
	 * Returns the curve parameters.
	 *
	 * @return the CurveInfo object
	 */
	public CurveInfo getCurveInfo() {
		return curveInfo;
	}

	/**
	 * Returns the server's ephemeral public key.
	 *
	 * @return the ECDHEPublicKey object
	 */
	public ECDHEPublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * Returns the signature over handshake parameters.
	 *
	 * @return the Signature object
	 */
	public Signature getSignature() {
		return signature;
	}
}
