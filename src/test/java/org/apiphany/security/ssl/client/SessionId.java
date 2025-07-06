package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apiphany.json.JsonBuilder;

public class SessionId implements Sizeable {

	private Int8 length;

	private BinaryData value;

	public SessionId(final Int8 length, final BinaryData value) {
		this.length = length;
		this.value = value;
	}

	public SessionId(final Int8 length, final byte[] value) {
		this(length, new BinaryData(value));
	}

	public SessionId(final byte length, final byte[] value) {
		this(new Int8(length), value);
	}

	public SessionId(final String value) {
		this((byte) value.length(), value.getBytes(StandardCharsets.US_ASCII));
	}

	public SessionId() {
		this("");
	}

	public static SessionId from(final InputStream is) throws IOException {
		Int8 length = Int8.from(is);
		BinaryData value = BinaryData.from(is, length.getValue());

		return new SessionId(length, value);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(length.toByteArray());
		dos.write(value.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return length.size() + value.size();
	}

	public Int8 getLength() {
		return length;
	}

	public BinaryData getValue() {
		return value;
	}

}

