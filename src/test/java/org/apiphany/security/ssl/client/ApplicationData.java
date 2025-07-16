package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

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
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(data.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return data.size();
	}

	public Encrypted getData() {
		return data;
	}
}
