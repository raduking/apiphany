package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class ServerHello {

	private RecordHeader recordHeader;

	private HandshakeHeader handshakeHeader;

	private Version version;

	private HandshakeRandom serverRandom;

	private SessionId sessionId;

	private CipherSuite cypherSuite;

	private CompressionMethod compressionMethod;

	private Int16 extensionsLength;

	private RenegotiationInfo renegotiationInfo;

	public ServerHello(RecordHeader recordHeader,
			HandshakeHeader handshakeHeader,
			Version version,
			HandshakeRandom handshakeRandom,
			SessionId sessionId,
			CipherSuite cypherSuite,
			CompressionMethod compressionMethod,
			Int16 extensionsLength,
			RenegotiationInfo renegotiationInfo) {
		this.recordHeader = recordHeader;
		this.handshakeHeader = handshakeHeader;
		this.version = version;
		this.serverRandom = handshakeRandom;
		this.sessionId = sessionId;
		this.cypherSuite = cypherSuite;
		this.compressionMethod = compressionMethod;
		this.extensionsLength = extensionsLength;
		this.renegotiationInfo = renegotiationInfo;
	}

	public static ServerHello from(InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		Version version = Version.from(is);
		HandshakeRandom serverRandom = HandshakeRandom.from(is);
		SessionId sessionId = SessionId.from(is);
		CipherSuite cypherSuite = CipherSuite.from(is);
		CompressionMethod compressionMethod = CompressionMethod.from(is);
		Int16 extensionsLength = Int16.from(is);
		RenegotiationInfo renegotiationInfo = RenegotiationInfo.from(is);

		return new ServerHello(
				recordHeader,
				handshakeHeader,
				version,
				serverRandom,
				sessionId,
				cypherSuite,
				compressionMethod,
				extensionsLength,
				renegotiationInfo);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(recordHeader.toByteArray());
		dos.write(handshakeHeader.toByteArray());
		dos.write(version.toByteArray());
		dos.write(serverRandom.toByteArray());
		dos.write(sessionId.toByteArray());
		dos.write(cypherSuite.toByteArray());
		dos.write(compressionMethod.toByteArray());
		dos.write(extensionsLength.toByteArray());
		dos.write(renegotiationInfo.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public RecordHeader getRecordHeader() {
		return recordHeader;
	}

	public HandshakeHeader getHandshakeHeader() {
		return handshakeHeader;
	}

	public Version getVersion() {
		return version;
	}

	public HandshakeRandom getServerRandom() {
		return serverRandom;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

	public CipherSuite getCypherSuite() {
		return cypherSuite;
	}

	public CompressionMethod getCompressionMethod() {
		return compressionMethod;
	}

	public Int16 getExtensionsLength() {
		return extensionsLength;
	}

	public RenegotiationInfo getRenegotiationInfo() {
		return renegotiationInfo;
	}
}
