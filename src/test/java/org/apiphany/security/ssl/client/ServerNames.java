package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class ServerNames implements TLSExtension {

	private ExtensionType type;

	private Int16 length;

	private List<ServerName> entries = new ArrayList<>();

	private ServerNames(final ExtensionType type, final Int16 length, final List<ServerName> entries, final boolean updateLength) {
		this.type = type;
		this.length = length;
		this.entries = entries;
		if (updateLength) {
			int result = 0;
			for (ServerName serverName : entries) {
				result += serverName.size();
			}
			this.length.setValue((short) result);
		}
	}

	public ServerNames(final Int16 size, final List<ServerName> serverNames) {
		this(ExtensionType.SERVER_NAME, size, serverNames, true);
	}

	public ServerNames(final List<String> names) {
		this(new Int16((short) names.size()), names.stream().map(ServerName::new).toList());
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.writeShort(type.value());
			dos.write(length.toByteArray());
			for (ServerName entry : entries) {
				byte[] entryBytes = entry.toByteArray();
				dos.write(entryBytes);
			}
		}).run();
		return bos.toByteArray();
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

		return new ServerNames(type, length, serverNames, false);
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
