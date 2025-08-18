package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents encrypted alert data in a TLS connection.
 * <p>
 * This class encapsulates the encrypted alert exchanged between client and server.
 *
 * @author Radu Sebastian LAZIN
 */
public class EncryptedAlert extends TLSEncryptedObject {

	/**
	 * Constructs an EncryptedAlert object with encrypted alert message.
	 *
	 * @param data the encrypted data payload
	 */
	public EncryptedAlert(final Encrypted data) {
		super(data);
	}

	/**
	 * Parses EncryptedAlert from an input stream.
	 *
	 * @param is the input stream containing encrypted data
	 * @param length the length of the encrypted data
	 * @return the parsed ApplicationData object
	 * @throws IOException if an I/O error occurs
	 */
	public static EncryptedAlert from(final InputStream is, final int length) throws IOException {
		// TODO: 8 sized nonce is specific to AES/GCM/NoPadding make it configurable
		Encrypted payload = Encrypted.from(is, length, 8);
		return new EncryptedAlert(payload);
	}
}
