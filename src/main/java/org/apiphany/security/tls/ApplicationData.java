package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents encrypted application data in a TLS connection.
 * <p>
 * This class encapsulates the encrypted payload exchanged between client and server after the TLS handshake is
 * complete. The actual decryption happens at a lower level.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApplicationData implements TLSObject {

	/**
	 * The encrypted payload data.
	 */
	private final Encrypted data;

	/**
	 * Constructs an ApplicationData object with encrypted payload.
	 *
	 * @param data the encrypted data payload
	 */
	public ApplicationData(final Encrypted data) {
		this.data = data;
	}

	/**
	 * Parses ApplicationData from an input stream.
	 *
	 * @param is the input stream containing encrypted data
	 * @param length the length of the encrypted data
	 * @return the parsed ApplicationData object
	 * @throws IOException if an I/O error occurs
	 */
	public static ApplicationData from(final InputStream is, final int length) throws IOException {
		// TODO: 8 sized nonce is specific to AES/GCM/NoPadding make it configurable
		Encrypted payload = Encrypted.from(is, length, 8);
		return new ApplicationData(payload);
	}

	/**
	 * Returns the binary representation of this encrypted data.
	 *
	 * @return byte array containing the encrypted payload
	 */
	@Override
	public byte[] toByteArray() {
		return data.toByteArray();
	}

	/**
	 * Returns a JSON representation of this ApplicationData.
	 *
	 * @return JSON string containing metadata about the encrypted data
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the size of the encrypted data.
	 *
	 * @return size in bytes of the encrypted payload
	 */
	@Override
	public int sizeOf() {
		return data.sizeOf();
	}

	/**
	 * Returns the encrypted data payload.
	 *
	 * @return the Encrypted data wrapper
	 */
	public Encrypted getData() {
		return data;
	}
}
