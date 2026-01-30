package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents the Server Finished message in a TLS connection.
 * <p>
 * This class encapsulates the encrypted handshake data sent by the server to indicate the completion of the handshake
 * process and is just an encrypted version of the {@link Finished} message.
 *
 * @author Radu Sebastian LAZIN
 */
public class ServerFinished extends EncryptedHandshake {

	/**
	 * Constructs a {@link ServerFinished} object with encrypted handshake message.
	 *
	 * @param data the encrypted data payload
	 */
	public ServerFinished(final Encrypted data) {
		super(data);
	}

	/**
	 * Parses {@link ServerFinished} from an input stream.
	 *
	 * @param is the input stream containing encrypted server finished data
	 * @param length the length of the encrypted data
	 * @return the parsed ServerFinished object
	 * @throws IOException if an I/O error occurs
	 */
	public static ServerFinished from(final InputStream is, final int length) throws IOException {
		Encrypted payload = Encrypted.from(is, length);
		return new ServerFinished(payload);
	}
}
