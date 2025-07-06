package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class HandshakeHeader implements Sizeable {

	public static final int BYTES = 4;

	private HandshakeMessageType messageType;

	private Int24 messageLength;

	public HandshakeHeader(final HandshakeMessageType messageType, final Int24 messageLength) {
		this.messageType = messageType;
		this.messageLength = messageLength;
	}

	public HandshakeHeader(final HandshakeMessageType messageType, final int messageLength) {
		this(messageType, new Int24(messageLength));
	}

	public HandshakeHeader(final HandshakeMessageType messageType) {
		this(messageType, (short) 0x0000);
	}

	public static HandshakeHeader from(final InputStream is) throws IOException {
		int firstByte = is.read();
		if (-1 == firstByte) {
			throw new EOFException("Connection closed by server");
		}
		HandshakeMessageType type = HandshakeMessageType.fromValue((byte) firstByte);

		Int24 messageLength = Int24.from(is);

		return new HandshakeHeader(type, messageLength);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeByte(messageType.value());
		dos.write(messageLength.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return messageType.size() + messageLength.size();
	}

	public HandshakeMessageType getMessageType() {
		return messageType;
	}

	public Int24 getMessageLength() {
		return messageLength;
	}
}

