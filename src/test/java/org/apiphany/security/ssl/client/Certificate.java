package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class Certificate {

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

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(length.toByteArray());
		dos.write(data.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public Int24 getLength() {
		return length;
	}

	public BinaryData getData() {
		return data;
	}
}
