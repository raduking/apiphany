package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

/**
 * Minimal Client Hello builder.
 *
 * @author Radu Sebastian LAZIN
 */
public class ClientHello implements Sizeable {

	private RecordHeader recordHeader = new RecordHeader(RecordHeaderType.HANDSHAKE, SSLProtocol.TLS_1_0);

	private HandshakeHeader handshakeHeader = new HandshakeHeader(HandshakeMessageType.CLIENT_HELLO);

	private Version clientVersion = new Version(SSLProtocol.TLS_1_2);

	private ExchangeRandom clientRandom = new ExchangeRandom(ExchangeRandom.generateLinear());

	private SessionId sessionId = new SessionId();

	private CipherSuites cipherSuites;

	private CompressionMethods compressionMethods = new CompressionMethods();

	private Extensions extensions;

	public ClientHello(final List<String> serverNames, final CipherSuites cypherSuites) {
		this.extensions = new Extensions(serverNames);
		this.cipherSuites = cypherSuites;
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
		dos.write(cipherSuites.toByteArray());

		// Compression Methods
		dos.write(compressionMethods.toByteArray());

		// Extensions Length
		dos.write(extensions.toByteArray());

		byte[] bytes = bos.toByteArray();

		// bytes after record header
		short bytesAfterRecordHeader = (short) (bytes.length - RecordHeader.BYTES);
		bytes[3] = (byte) ((bytesAfterRecordHeader >> 8) & 0xFF);
		bytes[4] = (byte) (bytesAfterRecordHeader & 0xFF);

		// bytes after handshake header
		short bytesAfterHandshakeHeader = (short) (bytesAfterRecordHeader - HandshakeHeader.BYTES);
		bytes[7] = (byte) ((bytesAfterHandshakeHeader >> 8) & 0xFF);
		bytes[8] = (byte) (bytesAfterHandshakeHeader & 0xFF);

		return bytes;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return recordHeader.size()
				+ handshakeHeader.size()
				+ clientVersion.size()
				+ clientRandom.size()
				+ sessionId.size()
				+ cipherSuites.size()
				+ compressionMethods.size();
	}

	public RecordHeader getRecordHeader() {
		return recordHeader;
	}

	public HandshakeHeader getHandshakeHeader() {
		return handshakeHeader;
	}

	public Version getClientVersion() {
		return clientVersion;
	}

	public ExchangeRandom getClientRandom() {
		return clientRandom;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

	public CipherSuites getCipherSuites() {
		return cipherSuites;
	}

	public CompressionMethods getCompressionMethods() {
		return compressionMethods;
	}

	public Extensions getExtensions() {
		return extensions;
	}
}
