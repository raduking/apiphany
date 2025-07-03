package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServerName {

	private Int16 size;

	/**
	 * 0x00 - DNS hostname.
	 */
	private Int8 type = new Int8((byte) 0x00);

	private Int16 nameLength;

	private byte[] name;

	public ServerName(final String name) {
		this.name = name.getBytes(StandardCharsets.US_ASCII);
		this.nameLength = new Int16((short) name.length());
		this.size = new Int16((short) (type.size() + nameLength.size() + name.length()));
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(size.toByteArray());
		dos.write(type.toByteArray());
		dos.write(nameLength.toByteArray());
		dos.write(name);

		return bos.toByteArray();
	}
}
