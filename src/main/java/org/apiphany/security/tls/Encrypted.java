package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.BytesWrapper;

/**
 * Represents encrypted data in TLS protocol messages.
 * <p>
 * This class encapsulates encrypted payloads used in TLS, including the nonce/IV and the actual encrypted data. It is
 * typically used in TLS 1.3 for encrypted handshake messages.
 *
 * @author Radu Sebastian LAZIN
 */
public class Encrypted implements TLSObject {

	/**
	 * The nonce or initialization vector used for encryption.
	 */
	private final BytesWrapper nonce;

	/**
	 * The encrypted payload data.
	 */
	private final BytesWrapper encryptedData;

	/**
	 * Constructs an Encrypted message with wrapped data.
	 *
	 * @param nonce the nonce/IV bytes wrapper
	 * @param encryptedData the encrypted data wrapper
	 */
	public Encrypted(final BytesWrapper nonce, final BytesWrapper encryptedData) {
		this.nonce = nonce;
		this.encryptedData = encryptedData;
	}

	/**
	 * Constructs an Encrypted message with raw byte arrays.
	 *
	 * @param nonce the nonce/IV bytes
	 * @param encryptedData the encrypted data bytes
	 */
	public Encrypted(final byte[] nonce, final byte[] encryptedData) {
		this(new BytesWrapper(nonce), new BytesWrapper(encryptedData));
	}

	/**
	 * Parses an Encrypted message from an input stream.
	 *
	 * @param is the input stream containing the encrypted data
	 * @param totalLength the total length of encrypted message
	 * @param nonceLength the length of the nonce/IV
	 * @return the parsed Encrypted object
	 * @throws IOException if an I/O error occurs
	 */
	public static Encrypted from(final InputStream is, final int totalLength, final int nonceLength) throws IOException {
		BytesWrapper nonce = BytesWrapper.from(is, nonceLength);
		BytesWrapper payload = BytesWrapper.from(is, totalLength - nonceLength);
		return new Encrypted(nonce, payload);
	}

	/**
	 * Returns the binary representation of this Encrypted message.
	 *
	 * @return byte array containing nonce and encrypted data
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(nonce.toByteArray());
		buffer.put(encryptedData.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this Encrypted message.
	 *
	 * @return JSON string containing encrypted data information
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of nonce plus encrypted data
	 */
	@Override
	public int sizeOf() {
		return nonce.sizeOf() + encryptedData.sizeOf();
	}

	/**
	 * Returns the nonce/initialization vector.
	 *
	 * @return the BytesWrapper containing nonce bytes
	 */
	public BytesWrapper getNonce() {
		return nonce;
	}

	/**
	 * Returns the encrypted data.
	 *
	 * @return the BytesWrapper containing encrypted bytes
	 */
	public BytesWrapper getEncryptedData() {
		return encryptedData;
	}
}
