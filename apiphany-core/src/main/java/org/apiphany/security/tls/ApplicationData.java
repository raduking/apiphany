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
public class ApplicationData extends TLSEncryptedObject {

	/**
	 * Constructs an ApplicationData object with encrypted payload.
	 *
	 * @param data the encrypted data payload
	 */
	public ApplicationData(final Encrypted data) {
		super(data);
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
		Encrypted payload = Encrypted.from(is, length);
		return new ApplicationData(payload);
	}
}
