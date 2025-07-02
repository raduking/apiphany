package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServerName {

	private short size;

	private byte type = 0x00; // DNS hostname

	private short nameLength;

	private byte[] name;

	public ServerName(final String name) {
		this.name = name.getBytes(StandardCharsets.US_ASCII);
		this.nameLength = (short) name.length();
		this.size = (short) (nameLength + 3); // 3 is sizeof(nameSize) + sizeof(type) in bytes
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeShort(size);
		dos.writeByte(type);
		dos.writeShort(nameLength);
		dos.write(name);

		return bos.toByteArray();
	}
}
