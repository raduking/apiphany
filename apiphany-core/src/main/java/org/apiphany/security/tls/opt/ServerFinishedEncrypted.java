package org.apiphany.security.tls.opt;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.security.tls.Encrypted;
import org.apiphany.security.tls.EncryptedHandshake;
import org.apiphany.security.tls.Finished;

/**
 * Represents the Server Finished message in a TLS connection. This is an optional class and it can be used
 * interchangeably with the standard {@link EncryptedHandshake} class and is present for code clarity purposes.
 * <p>
 * This class encapsulates the encrypted handshake data sent by the server to indicate the completion of the handshake
 * process and is just an encrypted version of the {@link Finished} message.
 * <p>
 * It contains verification data that proves the integrity of the handshake.
 *
 * @author Radu Sebastian LAZIN
 */
public class ServerFinishedEncrypted extends EncryptedHandshake {

	/**
	 * Constructs a {@link ServerFinishedEncrypted} object with encrypted handshake message.
	 *
	 * @param data the encrypted data payload
	 */
	public ServerFinishedEncrypted(final Encrypted data) {
		super(data);
	}

	/**
	 * Parses {@link ServerFinishedEncrypted} from an input stream.
	 *
	 * @param is the input stream containing encrypted server finished data
	 * @param length the length of the encrypted data
	 * @return the parsed ServerFinished object
	 * @throws IOException if an I/O error occurs
	 */
	public static ServerFinishedEncrypted from(final InputStream is, final int length) throws IOException {
		Encrypted payload = Encrypted.from(is, length);
		return new ServerFinishedEncrypted(payload);
	}
}
