package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class TLSRecordServerHello implements Sizeable, BinaryRepresentable {

	private RecordHeader recordHeader;

	private HandshakeHeader handshakeHeader;

	private ServerHello serverHello;

	private ServerCertificate serverCertificate;

	private ServerKeyExchange serverKeyExchange;

	private HandshakeHeader serverHelloDone;

	public TLSRecordServerHello(final RecordHeader recordHeader,
			final HandshakeHeader handshakeHeader,
			final ServerHello serverHello,
			final ServerCertificate serverCertificate,
			final ServerKeyExchange serverKeyExchange,
			final HandshakeHeader serverHelloDone) {
		this.recordHeader = recordHeader;
		this.handshakeHeader = handshakeHeader;
		this.serverHello = serverHello;
		this.serverCertificate = serverCertificate;
		this.serverKeyExchange = serverKeyExchange;
		this.serverHelloDone = serverHelloDone;
	}

	public static TLSRecordServerHello from(final InputStream is) throws IOException {
		RecordHeader recordHeader = RecordHeader.from(is);
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		ServerHello serverHello = ServerHello.from(is);
		ServerCertificate serverCertificate = ServerCertificate.from(is);
		ServerKeyExchange serverKeyExchange = ServerKeyExchange.from(is);
		HandshakeHeader serverHelloDone = HandshakeHeader.from(is);

		return new TLSRecordServerHello(
				recordHeader,
				handshakeHeader,
				serverHello,
				serverCertificate,
				serverKeyExchange,
				serverHelloDone);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(recordHeader.toByteArray());
			dos.write(handshakeHeader.toByteArray());
			dos.write(serverHello.toByteArray());
			dos.write(serverCertificate.toByteArray());
			dos.write(serverKeyExchange.toByteArray());
			dos.write(serverHelloDone.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public int size() {
		return recordHeader.size()
				+ handshakeHeader.size()
				+ serverHello.size()
				+ serverCertificate.size()
				+ serverKeyExchange.size()
				+ serverHelloDone.size();
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

	public ServerHello getServerHello() {
		return serverHello;
	}

	public ServerCertificate getServerCertificate() {
		return serverCertificate;
	}

	public ServerKeyExchange getServerKeyExchange() {
		return serverKeyExchange;
	}

	public HandshakeHeader getServerHelloDone() {
		return serverHelloDone;
	}
}
