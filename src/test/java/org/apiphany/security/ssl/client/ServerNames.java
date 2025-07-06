package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;

public class ServerNames implements Sizeable {

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
		Bytes.set(entriesSize, bytes, ENTRIES_SIZE_INDEX);

		return bytes;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		int result = type.size() + size.size();
		for (ServerName entry : entries) {
			result += entry.size();
		}
		return result;
	}

	public ExtensionType getType() {
		return type;
	}

	public Int16 getSize() {
		return size;
	}

	public List<ServerName> getEntries() {
		return entries;
	}
}
