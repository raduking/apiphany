package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apiphany.json.JsonBuilder;

public class SignedCertificateTimestamp implements Sizeable {

	private ExtensionType type = ExtensionType.SCT;

	private Int16 size = new Int16((short) 0x0000);

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeShort(type.value());
		dos.write(size.toByteArray());

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return type.size() + size.size();
	}

	public ExtensionType getType() {
		return type;
	}

	public Int16 getSize() {
		return size;
	}
}

