package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

public class ServerKeyExchange implements TLSHandshakeBody {

	private CurveInfo curveInfo;

	private PublicKeyECDHE publicKey;

	private Signature signature;

	public ServerKeyExchange(final CurveInfo curveInfo, final PublicKeyECDHE publicKey, final Signature signature) {
		this.curveInfo = curveInfo;
		this.publicKey = publicKey;
		this.signature = signature;
	}

	public static ServerKeyExchange from(final InputStream is) throws IOException {
		CurveInfo curveInfo = CurveInfo.from(is);
		PublicKeyECDHE publicKey = PublicKeyECDHE.from(is);
		Signature signature = Signature.from(is);

		return new ServerKeyExchange(curveInfo, publicKey, signature);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(curveInfo.toByteArray());
		buffer.put(publicKey.toByteArray());
		buffer.put(signature.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return curveInfo.sizeOf() + publicKey.sizeOf() + signature.sizeOf();
	}

	@Override
	public HandshakeType type() {
		return HandshakeType.SERVER_KEY_EXCHANGE;
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
