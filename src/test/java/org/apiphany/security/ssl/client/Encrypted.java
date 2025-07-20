package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.BytesWrapper;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.tls.TLSObject;

public class Encrypted implements TLSObject {

	private final BytesWrapper nonce;
	private final BytesWrapper encryptedData;

	public Encrypted(final BytesWrapper nonce, final BytesWrapper encryptedData) {
		this.nonce = nonce;
		this.encryptedData = encryptedData;
	}

	public Encrypted(final byte[] nonce, final byte[] encryptedData) {
		this(new BytesWrapper(nonce), new BytesWrapper(encryptedData));
	}

	public static Encrypted from(final InputStream is, final int totalLength, final int nonceLength) throws IOException {
		BytesWrapper nonce = BytesWrapper.from(is, nonceLength);
		BytesWrapper payload = BytesWrapper.from(is, totalLength - nonceLength);

		return new Encrypted(nonce, payload);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(nonce.toByteArray());
		buffer.put(encryptedData.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return nonce.sizeOf() + encryptedData.sizeOf();
	}

	public BytesWrapper getNonce() {
		return nonce;
	}

	public BytesWrapper getEncryptedData() {
		return encryptedData;
	}
}
