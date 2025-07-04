package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class ServerCertificate {

	private HandshakeHeader handshakeHeader;

	private Int24 certificatesLength;

	private Certificate certificate;

	public ServerCertificate(HandshakeHeader handshakeHeader, Int24 certificatesLength, Certificate certificate) {
		this.handshakeHeader = handshakeHeader;
		this.certificatesLength = certificatesLength;
		this.certificate = certificate;
	}

	public static ServerCertificate from(InputStream is) throws IOException {
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		Int24 certificatesLength = Int24.from(is);
		Certificate certificate = Certificate.from(is);

		return new ServerCertificate(handshakeHeader, certificatesLength, certificate);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(handshakeHeader.toByteArray());
		dos.write(certificatesLength.toByteArray());
		dos.write(certificate.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
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
