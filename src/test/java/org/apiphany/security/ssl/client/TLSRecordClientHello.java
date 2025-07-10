package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;
import org.morphix.lang.function.ThrowingRunnable;

/**
 * Client Hello TLS record.
 *
 * @author Radu Sebastian LAZIN
 */
public class TLSRecordClientHello implements Sizeable, BinaryRepresentable {

	private RecordHeader recordHeader;

	private HandshakeHeader handshakeHeader;

	private ClientHello clientHello;

	private TLSRecordClientHello(
			final RecordHeader recordHeader,
			final HandshakeHeader handshakeHeader,
			final ClientHello clientHello,
			final boolean setSizes) {
		this.recordHeader = recordHeader;
		this.handshakeHeader = handshakeHeader;
		this.clientHello = clientHello;
		if (setSizes) {
			this.recordHeader.getLength().setValue((short) (size() - RecordHeader.BYTES));
			this.handshakeHeader.getLength().setValue((short) (recordHeader.getLength().getValue() - HandshakeHeader.BYTES));
		}
	}

	public TLSRecordClientHello(
			final RecordHeader recordHeader,
			final HandshakeHeader handshakeHeader,
			final ClientHello clientHello) {
		this(
				recordHeader,
				handshakeHeader,
				clientHello,
				true
		);
	}

	public TLSRecordClientHello(final List<String> serverNames, final CipherSuites cypherSuites, final List<CurveName> curveNames) {
		this(
				new RecordHeader(RecordType.HANDSHAKE, SSLProtocol.TLS_1_0),
				new HandshakeHeader(HandshakeType.CLIENT_HELLO),
				new ClientHello(
						new Version(SSLProtocol.TLS_1_2),
						new ExchangeRandom(ExchangeRandom.generateLinear()),
						new SessionId(),
						cypherSuites,
						new CompressionMethods(),
						new Extensions(serverNames, curveNames))
		);
	}

	public TLSRecordClientHello(final List<String> serverNames, final List<CipherSuite> cypherSuites, final List<CurveName> curveNames) {
		this(serverNames, new CipherSuites(cypherSuites), curveNames);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(recordHeader.toByteArray());
			dos.write(handshakeHeader.toByteArray());
			dos.write(clientHello.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	public static TLSRecordClientHello from(final InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		ClientHello clientHello = ClientHello.from(is);

		return new TLSRecordClientHello(recordHeader, handshakeHeader, clientHello, false);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return recordHeader.size()
				+ handshakeHeader.size()
				+ clientHello.size();
	}

	public RecordHeader getRecordHeader() {
		return recordHeader;
	}

	public HandshakeHeader getHandshakeHeader() {
		return handshakeHeader;
	}

	public ClientHello getClientHello() {
		return clientHello;
	}
}
