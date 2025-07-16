package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

public class ApplicationData implements TLSObject {

	private Encrypted data;

	public ApplicationData(final Encrypted data) {
		this.data = data;
	}

	public static ApplicationData from(final InputStream is, final int length) throws IOException {
		Encrypted payload = Encrypted.from(is, length, 8);

		return new ApplicationData(payload);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(data.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return data.sizeOf();
	}

	public Encrypted getData() {
		return data;
	}
}
