package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class RenegotiationInfo {

	private ExtensionType type;

	private Int16 size;

	private Int8 length;

	public RenegotiationInfo(ExtensionType type, Int16 size, Int8 length) {
		this.type = type;
		this.size = size;
		this.length = length;
	}

	public RenegotiationInfo(ExtensionType type, short size, byte length) {
		this(type, new Int16(size), new Int8(length));
	}

	public RenegotiationInfo() {
		this(ExtensionType.RENEGOTIATION_INFO, (short) 0x0001, (byte) 0x00);
	}

	public static RenegotiationInfo from(InputStream is) throws IOException {
		byte[] shortBuffer = new byte[Bytes.Size.SHORT];
		int bytesRead = is.read(shortBuffer);
		if (Bytes.Size.SHORT != bytesRead) {
			throw new EOFException("Short renegotiation infe, cannot read extension type");
		}
		ExtensionType type = ExtensionType.fromValue(Bytes.toShort(shortBuffer));

		Int16 size = Int16.from(is);

		Int8 length = Int8.from(is);

		return new RenegotiationInfo(type, size, length);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeShort(type.value());
		dos.write(size.toByteArray());
		dos.write(length.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public ExtensionType getType() {
		return type;
	}

	public Int16 getSize() {
		return size;
	}

	public Int8 getLength() {
		return length;
	}
}
