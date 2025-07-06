package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

/**
 * Minimal Client Hello builder.
 *
 * @author Radu Sebastian LAZIN
 */
public class ClientHello implements Sizeable {

	private RecordHeader recordHeader;

	private HandshakeHeader handshakeHeader;

	private Version clientVersion;

	private ExchangeRandom clientRandom;

	private SessionId sessionId;

	private CipherSuites cipherSuites;

	private CompressionMethods compressionMethods;

	private Extensions extensions;

	private ClientHello(
			final RecordHeader recordHeader,
			final HandshakeHeader handshakeHeader,
			final Version clientVersion,
			final ExchangeRandom clientRandom,
			final SessionId sessionId,
			final CipherSuites cipherSuites,
			final CompressionMethods compressionMethods,
			final Extensions extensions,
			final boolean setSizes) {
		this.recordHeader = recordHeader;
		this.handshakeHeader = handshakeHeader;
		this.clientVersion = clientVersion;
		this.clientRandom = clientRandom;
		this.sessionId = sessionId;
		this.cipherSuites = cipherSuites;
		this.compressionMethods = compressionMethods;
		this.extensions = extensions;
		if (setSizes) {
			this.recordHeader.getLength().setValue((short) (size() - RecordHeader.BYTES));
			this.handshakeHeader.getLength().setValue((short) (recordHeader.getLength().getValue() - HandshakeHeader.BYTES));
		}
	}

	public ClientHello(
			final RecordHeader recordHeader,
			final HandshakeHeader handshakeHeader,
			final Version clientVersion,
			final ExchangeRandom clientRandom,
			final SessionId sessionId,
			final CipherSuites cipherSuites,
			final CompressionMethods compressionMethods,
			final Extensions extensions) {
		this(
				recordHeader,
				handshakeHeader,
				clientVersion,
				clientRandom,
				sessionId,
				cipherSuites,
				compressionMethods,
				extensions,
				true
		);
	}

	public ClientHello(final List<String> serverNames, final CipherSuites cypherSuites, final List<CurveName> curveNames) {
		this(
				new RecordHeader(RecordHeaderType.HANDSHAKE, SSLProtocol.TLS_1_0),
				new HandshakeHeader(HandshakeMessageType.CLIENT_HELLO),
				new Version(SSLProtocol.TLS_1_2),
				new ExchangeRandom(ExchangeRandom.generateLinear()),
				new SessionId(),
				cypherSuites,
				new CompressionMethods(),
				new Extensions(serverNames, curveNames)
		);
	}

	public ClientHello(final List<String> serverNames, final List<CipherSuite> cypherSuites, final List<CurveName> curveNames) {
		this(serverNames, new CipherSuites(cypherSuites), curveNames);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(recordHeader.toByteArray());
		dos.write(handshakeHeader.toByteArray());
		dos.write(clientVersion.toByteArray());
		dos.write(clientRandom.toByteArray());
		dos.write(sessionId.toByteArray());
		dos.write(cipherSuites.toByteArray());
		dos.write(compressionMethods.toByteArray());
		dos.write(extensions.toByteArray());

		return bos.toByteArray();
	}

	public static ClientHello from(final InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		Version clientVersion = Version.from(is);
		ExchangeRandom clientRandom = ExchangeRandom.from(is);
		SessionId sessionId = SessionId.from(is);
		CipherSuites cipherSuites = CipherSuites.from(is);
		CompressionMethods compressionMethods = CompressionMethods.from(is);
		Extensions extensions = Extensions.from(is);

		return new ClientHello(recordHeader, handshakeHeader, clientVersion, clientRandom,
				sessionId, cipherSuites, compressionMethods, extensions, false);
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
				+ compressionMethods.size()
				+ extensions.size();
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
