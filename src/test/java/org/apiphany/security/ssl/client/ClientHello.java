package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.apiphany.security.ssl.SSLProtocol;

/**
 * Minimal Client Hello builder.
 *
 * @author Radu Sebastian LAZIN
 */
public class ClientHello {

	RecordHeader recordHeader = new RecordHeader(RecordHeaderType.HANDSHAKE_RECORD, SSLProtocol.TLS_1_0);

	HandshakeHeader handshakeHeader = new HandshakeHeader(HandshakeMessageType.CLIENT_HELLO);

	Version clientVersion = new Version(SSLProtocol.TLS_1_2);

	HandshakeRandom clientRandom = new HandshakeRandom(HandshakeRandom.generateLinear());

	SessionId sessionId = new SessionId();

	CipherSuites cypherSuites;

	CompressionMethods compressionMethods = new CompressionMethods();

	Extensions extensions;

	public ClientHello(final List<String> serverNames, final CipherSuites cypherSuites) {
		this.extensions = new Extensions(serverNames);
		this.cypherSuites = cypherSuites;
	}

	public ClientHello(final List<String> serverNames, final List<CipherSuite> cypherSuites) {
		this(serverNames, new CipherSuites(cypherSuites));
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		// Record Header
		dos.write(recordHeader.toByteArray());

		// Handshake Header
		dos.write(handshakeHeader.toByteArray());

		// Client Version
		dos.write(clientVersion.toByteArray());

		// Client Random
		dos.write(clientRandom.toByteArray());

		// Session ID
		dos.write(sessionId.toByteArray());

		// Cipher Suites
		dos.write(cypherSuites.toByteArray());

		// Compression Methods
		dos.write(compressionMethods.toByteArray());

		// Extensions Length
		dos.write(extensions.toByteArray());

		byte[] bytes = bos.toByteArray();

		// bytes after record header
		short bytesAfterRecordHeader = (short) (bytes.length - RecordHeader.SIZE);
		bytes[3] = (byte) ((bytesAfterRecordHeader >> 8) & 0xFF);
		bytes[4] = (byte) (bytesAfterRecordHeader & 0xFF);

		// bytes after handshake header
		short bytesAfterHandshakeHeader = (short) (bytesAfterRecordHeader - HandshakeHeader.SIZE);
		bytes[7] = (byte) ((bytesAfterHandshakeHeader >> 8) & 0xFF);
		bytes[8] = (byte) (bytesAfterHandshakeHeader & 0xFF);

		return bytes;
	}
}
