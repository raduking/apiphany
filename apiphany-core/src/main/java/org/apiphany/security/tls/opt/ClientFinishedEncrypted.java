package org.apiphany.security.tls.opt;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.security.tls.Encrypted;
import org.apiphany.security.tls.EncryptedHandshake;
import org.apiphany.security.tls.Finished;

/**
 * Represents the Client Finished message in a TLS connection. This is an optional class and it can be used
 * interchangeably with the standard {@link EncryptedHandshake} class and is present for code clarity purposes.
 * <p>
 * This class encapsulates the encrypted handshake data sent by the client to indicate the completion of the handshake
 * process and is just an encrypted version of the {@link Finished} message.
 * <p>
 * It contains verification data that proves the integrity of the handshake.
 *
 * @author Radu Sebastian LAZIN
 */
public class ClientFinishedEncrypted extends EncryptedHandshake {

	/**
	 * Constructs a {@link ClientFinishedEncrypted} object with encrypted handshake message.
	 *
	 * @param data the encrypted data payload
	 */
	public ClientFinishedEncrypted(final Encrypted data) {
		super(data);
	}

	/**
	 * Parses {@link ClientFinishedEncrypted} from an input stream.
	 *
	 * @param is the input stream containing encrypted server finished data
	 * @param length the length of the encrypted data
	 * @return the parsed ServerFinished object
	 * @throws IOException if an I/O error occurs
	 */
	public static ClientFinishedEncrypted from(final InputStream is, final int length) throws IOException {
		Encrypted payload = Encrypted.from(is, length);
		return new ClientFinishedEncrypted(payload);
	}
}
