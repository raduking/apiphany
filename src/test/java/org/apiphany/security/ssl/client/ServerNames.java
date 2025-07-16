package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;

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
		ByteBuffer buffer = ByteBuffer.allocate(size());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		for (ServerName entry : entries) {
			buffer.put(entry.toByteArray());
		}
		return buffer.array();
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
