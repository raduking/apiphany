package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class EncryptedFinished implements TLSObject {

	private BinaryData nonce;
	private BinaryData encryptedData;

	public EncryptedFinished(final BinaryData nonce, final BinaryData encryptedData) {
		this.nonce = nonce;
		this.encryptedData = encryptedData;
	}

	public EncryptedFinished(final byte[] nonce, final byte[] encryptedData) {
		this(new BinaryData(nonce), new BinaryData(encryptedData));
	}

	public static EncryptedFinished from(final InputStream is, final int length) throws IOException {
		BinaryData nonce = BinaryData.from(is, 8);
		BinaryData payload = BinaryData.from(is, length - 8);

		return new EncryptedFinished(nonce, payload);
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
