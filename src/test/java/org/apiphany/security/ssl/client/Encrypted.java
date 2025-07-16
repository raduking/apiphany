package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

public class Encrypted implements TLSObject {

	private BinaryData nonce;
	private BinaryData encryptedData;

	public Encrypted(final BinaryData nonce, final BinaryData encryptedData) {
		this.nonce = nonce;
		this.encryptedData = encryptedData;
	}

	public Encrypted(final byte[] nonce, final byte[] encryptedData) {
		this(new BinaryData(nonce), new BinaryData(encryptedData));
	}

	public static Encrypted from(final InputStream is, final int totalLength, final int nonceLength) throws IOException {
		BinaryData nonce = BinaryData.from(is, nonceLength);
		BinaryData payload = BinaryData.from(is, totalLength - nonceLength);

		return new Encrypted(nonce, payload);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(size());
		buffer.put(nonce.toByteArray());
		buffer.put(encryptedData.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return nonce.size() + encryptedData.size();
	}

	public BinaryData getNonce() {
		return nonce;
	}

	public BinaryData getEncryptedData() {
		return encryptedData;
	}
}
