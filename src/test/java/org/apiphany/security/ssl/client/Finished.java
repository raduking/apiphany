package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.BytesWrapper;
import org.apiphany.json.JsonBuilder;

public class Finished implements TLSHandshakeBody {

	private final BytesWrapper verifyData;

	public Finished(final BytesWrapper verifyData) {
		this.verifyData = verifyData;
	}

	public Finished(final byte[] payload) {
		this(new BytesWrapper(payload));
	}

	public static Finished from(final InputStream is, final int length) throws IOException {
		BytesWrapper payload = BytesWrapper.from(is, length);

		return new Finished(payload);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(verifyData.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return verifyData.sizeOf();
	}

	@Override
	public HandshakeType getType() {
		return HandshakeType.FINISHED;
	}

	public BytesWrapper getVerifyData() {
		return verifyData;
	}
}
