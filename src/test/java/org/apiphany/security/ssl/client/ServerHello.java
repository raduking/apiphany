package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class ServerHello implements Sizeable {

	private RecordHeader recordHeader;

	private HandshakeHeader handshakeHeader;

	private Version version;

	private ExchangeRandom serverRandom;

	private SessionId sessionId;

	private CipherSuite cipherSuite;

	private CompressionMethod compressionMethod;

	private Int16 extensionsLength;

	private RenegotiationInfo renegotiationInfo;

	public ServerHello(final RecordHeader recordHeader,
			final HandshakeHeader handshakeHeader,
			final Version version,
			final ExchangeRandom serverRandom,
			final SessionId sessionId,
			final CipherSuite cipherSuite,
			final CompressionMethod compressionMethod,
			final Int16 extensionsLength,
			final RenegotiationInfo renegotiationInfo) {
		this.recordHeader = recordHeader;
		this.handshakeHeader = handshakeHeader;
		this.version = version;
		this.serverRandom = serverRandom;
		this.sessionId = sessionId;
		this.cipherSuite = cipherSuite;
		this.compressionMethod = compressionMethod;
		this.extensionsLength = extensionsLength;
		this.renegotiationInfo = renegotiationInfo;
	}

	public static ServerHello from(final InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		Version version = Version.from(is);
		ExchangeRandom serverRandom = ExchangeRandom.from(is);
		SessionId sessionId = SessionId.from(is);
		CipherSuite cipherSuite = CipherSuite.from(is);
		CompressionMethod compressionMethod = CompressionMethod.from(is);
		Int16 extensionsLength = Int16.from(is);
		RenegotiationInfo renegotiationInfo = RenegotiationInfo.from(is);

		return new ServerHello(
				recordHeader,
				handshakeHeader,
				version,
				serverRandom,
				sessionId,
				cipherSuite,
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
		dos.write(cipherSuite.toByteArray());
		dos.write(compressionMethod.toByteArray());
		dos.write(extensionsLength.toByteArray());
		dos.write(renegotiationInfo.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public int size() {
		return recordHeader.size()
				+ handshakeHeader.size()
				+ version.size()
				+ serverRandom.size()
				+ sessionId.size()
				+ cipherSuite.size()
				+ compressionMethod.size()
				+ extensionsLength.size()
				+ renegotiationInfo.size();
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

	public ExchangeRandom getServerRandom() {
		return serverRandom;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

	public CipherSuite getCipherSuite() {
		return cipherSuite;
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
