package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class HandshakeHeader implements TLSObject {

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

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.writeByte(type.value());
			dos.write(length.toByteArray());
		}).run();
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

