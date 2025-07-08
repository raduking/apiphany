package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class HandshakeHeader implements Sizeable {

	public static final int BYTES = 4;

	private HandshakeType type;

	private Int24 length;

	public HandshakeHeader(final HandshakeType type, final Int24 length) {
		this.type = type;
		this.length = length;
	}

	public HandshakeHeader(final HandshakeType type, final int length) {
		this(type, new Int24(length));
	}

	public HandshakeHeader(final HandshakeType type) {
		this(type, (short) 0x0000);
	}

	public static HandshakeHeader from(final InputStream is) throws IOException {
		Int8 int8 = Int8.from(is);
		HandshakeType type = HandshakeType.fromValue(int8.getValue());

		Int24 messageLength = Int24.from(is);

		return new HandshakeHeader(type, messageLength);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeByte(type.value());
		dos.write(length.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return type.size() + length.size();
	}

	public HandshakeType getType() {
		return type;
	}

	public Int24 getLength() {
		return length;
	}
}

