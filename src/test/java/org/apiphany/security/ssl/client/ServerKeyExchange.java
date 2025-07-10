package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class ServerKeyExchange implements TLSObject {

	private HandshakeHeader handshakeHeader;

	private CurveInfo curveInfo;

	private PublicKeyECDHE publicKey;

	private Signature signature;

	public ServerKeyExchange(final HandshakeHeader handshakeHeader, final CurveInfo curveInfo, final PublicKeyECDHE publicKey, final Signature signature) {
		this.handshakeHeader = handshakeHeader;
		this.curveInfo = curveInfo;
		this.publicKey = publicKey;
		this.signature = signature;
	}

	public static ServerKeyExchange from(final InputStream is) throws IOException {
		HandshakeHeader handshakeHeader = HandshakeHeader.from(is);
		CurveInfo curveInfo = CurveInfo.from(is);
		PublicKeyECDHE publicKey = PublicKeyECDHE.from(is);
		Signature signature = Signature.from(is);

		return new ServerKeyExchange(handshakeHeader, curveInfo, publicKey, signature);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(handshakeHeader.toByteArray());
			dos.write(curveInfo.toByteArray());
			dos.write(publicKey.toByteArray());
			dos.write(signature.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return handshakeHeader.size() + curveInfo.size() + publicKey.size() + signature.size();
	}

	public HandshakeHeader getHandshakeHeader() {
		return handshakeHeader;
	}

	public CurveInfo getCurveInfo() {
		return curveInfo;
	}

	public PublicKeyECDHE getPublicKey() {
		return publicKey;
	}

	public Signature getSignature() {
		return signature;
	}
}
