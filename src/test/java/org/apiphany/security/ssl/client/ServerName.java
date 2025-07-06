package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apiphany.json.JsonBuilder;

public class ServerName implements Sizeable {

	private Int16 size;

	/**
	 * 0x00 - DNS hostname.
	 */
	private Int8 type = new Int8((byte) 0x00);

	private Int16 length;

	private BinaryData name;

	public ServerName(final String name) {
		this.name = new BinaryData(name.getBytes(StandardCharsets.US_ASCII));
		this.length = new Int16((short) name.length());
		this.size = new Int16((short) (type.size() + length.size() + name.length()));
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(size.toByteArray());
		dos.write(type.toByteArray());
		dos.write(length.toByteArray());
		dos.write(name.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return size.size() + type.size() + length.size() + name.size();
	}

	public Int16 getSize() {
		return size;
	}

	public Int8 getType() {
		return type;
	}

	public Int16 getLength() {
		return length;
	}

	public BinaryData getName() {
		return name;
	}

	public String getNameASCII() {
		return new String(name.getBytes(), StandardCharsets.US_ASCII);
	}
}
