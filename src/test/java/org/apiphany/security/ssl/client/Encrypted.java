package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

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
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(nonce.toByteArray());
			dos.write(encryptedData.toByteArray());
		}).run();
		return bos.toByteArray();
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
