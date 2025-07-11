package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class ChangeCipherSpec implements TLSObject {

	private Int8 payload;

	public ChangeCipherSpec(final Int8 payload) {
		this.payload = payload;
	}

	public ChangeCipherSpec(final byte payload) {
		this(new Int8(payload));
	}

	public ChangeCipherSpec() {
		this((byte) 0x01);
	}

	public static ChangeCipherSpec from(final InputStream is) throws IOException {
		Int8 payload = Int8.from(is);

		return new ChangeCipherSpec(payload);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(payload.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public int size() {
		return payload.size();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public Int8 getPayload() {
		return payload;
	}
}
