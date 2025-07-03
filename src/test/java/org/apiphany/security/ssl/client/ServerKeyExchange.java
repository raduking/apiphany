package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ServerKeyExchange {

	private HandshakeHeader handshakeHeader;

	private CurveInfo curveInfo;

	private PublicKey publicKey;

	private Signature signature;

	public ServerKeyExchange(HandshakeHeader handshakeHeader, CurveInfo curveInfo, PublicKey publicKey, Signature signature) {
		this.handshakeHeader = handshakeHeader;
		this.curveInfo = curveInfo;
		this.publicKey = publicKey;
		this.signature = signature;
	}

	public static ServerKeyExchange from(InputStream is) throws IOException {
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		CurveInfo curveInfo = CurveInfo.from(is);
		PublicKey publicKey = PublicKey.from(is);
		Signature signature = Signature.from(is);

		return new ServerKeyExchange(handshakeHeader, curveInfo, publicKey, signature);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(handshakeHeader.toByteArray());
		dos.write(curveInfo.toByteArray());
		dos.write(publicKey.toByteArray());
		dos.write(signature.toByteArray());

		return bos.toByteArray();
	}

	public HandshakeHeader getHandshakeHeader() {
		return handshakeHeader;
	}

	public CurveInfo getCurveInfo() {
		return curveInfo;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public Signature getSignature() {
		return signature;
	}
}
