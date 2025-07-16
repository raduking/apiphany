package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

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
		ByteBuffer buffer = ByteBuffer.allocate(size());
		buffer.put(verifyData.toByteArray());
		return buffer.array();
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
