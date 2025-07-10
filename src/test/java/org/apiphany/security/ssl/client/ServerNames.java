package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Holder;
import org.morphix.lang.function.ThrowingRunnable;

public class ServerNames implements TLSExtension {

	private static final int ENTRIES_SIZE_INDEX = 2;

	private ExtensionType type;

	private Int16 length;

	private List<ServerName> entries = new ArrayList<>();

	private ServerNames(final ExtensionType type, final Int16 length, final List<ServerName> entries) {
		this.type = type;
		this.length = length;
		this.entries = entries;
	}

	public ServerNames(final Int16 size, final List<ServerName> serverNames) {
		this(ExtensionType.SERVER_NAME, size, serverNames);
	}

	public ServerNames(final List<String> names) {
		this(new Int16((short) names.size()), names.stream().map(ServerName::new).toList());
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		Holder<Short> entriesSize = new Holder<>((short) 0);
		ThrowingRunnable.unchecked(() -> {
			dos.writeShort(type.value());
			dos.write(length.toByteArray());

			for (ServerName entry : entries) {
				byte[] entryBytes = entry.toByteArray();
				dos.write(entryBytes);
				entriesSize.setValue((short) (entriesSize.getValue() + entryBytes.length));
			}
		}).run();

		byte[] bytes = bos.toByteArray();

		// write actual size
		Bytes.set(entriesSize.getValue(), bytes, ENTRIES_SIZE_INDEX);

		return bytes;
	}

	public static ServerNames from(final InputStream is) throws IOException {
		Int16 int16 = Int16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static ServerNames from(final InputStream is, final ExtensionType type) throws IOException {
		Int16 length = Int16.from(is);
		List<ServerName> serverNames = new ArrayList<>();
		int currentLength = 0;
		while (currentLength < length.getValue()) {
			ServerName serverName = ServerName.from(is);
			serverNames.add(serverName);
			currentLength += serverName.size();
		}

		return new ServerNames(type, length, serverNames);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		int result = type.size() + length.size();
		for (ServerName entry : entries) {
			result += entry.size();
		}
		return result;
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public Int16 getLength() {
		return length;
	}

	public List<ServerName> getEntries() {
		return entries;
	}
}
