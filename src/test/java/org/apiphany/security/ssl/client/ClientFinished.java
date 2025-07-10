package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class ClientFinished implements TLSHandshakeBody {

	private BinaryData encryptedData;

	public ClientFinished(final BinaryData encryptedData) {
		this.encryptedData = encryptedData;
	}

	public ClientFinished(final byte[] payload) {
		this(new BinaryData(payload));
	}

	public static ClientFinished from(final InputStream is, int length) throws IOException {
		BinaryData payload = BinaryData.from(is, length);

		return new ClientFinished(payload);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
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
		return encryptedData.size();
	}

	@Override
	public HandshakeType type() {
		return HandshakeType.FINISHED;
	}

	public BinaryData getEncryptedData() {
		return encryptedData;
	}
}
