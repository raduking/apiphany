package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerNames {

	private static final int ENTRIES_SIZE_INDEX = 2;

	private final ExtensionType type = ExtensionType.SERVER_NAME;

	private Int16 size = new Int16();

	private List<ServerName> entries = new ArrayList<>();

	public ServerNames(final List<String> names) {
		for (String name : names) {
			entries.add(new ServerName(name));
		}
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeShort(type.value());
		dos.write(size.toByteArray());

		short entriesSize = 0;
		for (ServerName entry : entries) {
			byte[] entryBytes = entry.toByteArray();
			dos.write(entryBytes);
			entriesSize += entryBytes.length;
		}

		byte[] bytes = bos.toByteArray();

		// write actual size
		bytes[ENTRIES_SIZE_INDEX] = (byte) ((entriesSize >> 8) & 0xFF);
		bytes[ENTRIES_SIZE_INDEX + 1] = (byte) (entriesSize & 0xFF);

		return bytes;
	}
}
