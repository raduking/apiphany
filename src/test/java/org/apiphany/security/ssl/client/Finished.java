package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class Finished implements TLSHandshakeBody {

	private BinaryData verifyData;

	public Finished(final BinaryData verifyData) {
		this.verifyData = verifyData;
	}

	public Finished(final byte[] payload) {
		this(new BinaryData(payload));
	}

	public static Finished from(final InputStream is, final int length) throws IOException {
		BinaryData payload = BinaryData.from(is, length);

		return new Finished(payload);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(verifyData.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return verifyData.size();
	}

	@Override
	public HandshakeType type() {
		return HandshakeType.FINISHED;
	}

	public BinaryData getVerifyData() {
		return verifyData;
	}
}
