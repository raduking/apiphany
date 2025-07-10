package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class Certificate implements TLSObject {

	private Int24 length;

	private BinaryData data;

	public Certificate(final Int24 length, final BinaryData data) {
		this.length = length;
		this.data = data;
	}

	public Certificate(final Int24 length, final byte[] bytes) {
		this(length, new BinaryData(bytes));
	}

	public Certificate(final int length, final byte[] bytes) {
		this(new Int24(length), bytes);
	}

	public static Certificate from(final InputStream is) throws IOException {
		Int24 length = Int24.from(is);
		BinaryData data = BinaryData.from(is, length.getValue());

		return new Certificate(length, data);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(length.toByteArray());
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
		return length.size() + data.size();
	}

	public Int24 getLength() {
		return length;
	}

	public BinaryData getData() {
		return data;
	}
}
