package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents encrypted handshake data in a TLS connection.
 * <p>
 * This class encapsulates the encrypted handshake exchanged between client and server.
 * <p>
 * This class can be used for both client and server encrypted handshakes since their structure is identical.
 *
 * @author Radu Sebastian LAZIN
 */
public class EncryptedHandshake extends TLSEncryptedObject {

	/**
	 * Constructs an EncryptedHandshake object with encrypted handshake message.
	 *
	 * @param data the encrypted data payload
	 */
	public EncryptedHandshake(final Encrypted data) {
		super(data);
	}

	/**
	 * Parses EncryptedHandshake from an input stream.
	 *
	 * @param is the input stream containing encrypted data
	 * @param length the length of the encrypted data
	 * @return the parsed EncryptedHandshake object
	 * @throws IOException if an I/O error occurs
	 */
	public static EncryptedHandshake from(final InputStream is, final int length) throws IOException {
		Encrypted payload = Encrypted.from(is, length);
		return new EncryptedHandshake(payload);
	}
}
