package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class ServerCertificate implements TLSObject {

	private HandshakeHeader handshakeHeader;

	private Int24 certificatesLength;

	private Certificate certificate;

	public ServerCertificate(final HandshakeHeader handshakeHeader, final Int24 certificatesLength, final Certificate certificate) {
		this.handshakeHeader = handshakeHeader;
		this.certificatesLength = certificatesLength;
		this.certificate = certificate;
	}

	public static ServerCertificate from(final InputStream is) throws IOException {
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		Int24 certificatesLength = Int24.from(is);
		Certificate certificate = Certificate.from(is);

		return new ServerCertificate(handshakeHeader, certificatesLength, certificate);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(handshakeHeader.toByteArray());
			dos.write(certificatesLength.toByteArray());
			dos.write(certificate.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return handshakeHeader.size() + certificatesLength.size() + certificate.size();
	}

	public HandshakeHeader getHandshakeHeader() {
		return handshakeHeader;
	}

	public Int24 getCertificatesLength() {
		return certificatesLength;
	}

	public Certificate getCertificate() {
		return certificate;
	}
}
