package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.UInt16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;

public class ServerNames implements TLSExtension {

	private final ExtensionType type;

	private final UInt16 length;

	private final List<ServerName> entries;

	private ServerNames(final ExtensionType type, final UInt16 length, final List<ServerName> entries, final boolean updateLength) {
		this.type = type;
		this.length = updateLength ? UInt16.of((short) ByteSizeable.sizeOf(entries)) : length;
		this.entries = entries;
	}

	public ServerNames(final UInt16 size, final List<ServerName> serverNames) {
		this(ExtensionType.SERVER_NAME, size, serverNames, true);
	}

	public ServerNames(final List<String> names) {
		this(UInt16.of((short) names.size()), names.stream().map(ServerName::new).toList());
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		for (ServerName entry : entries) {
			buffer.put(entry.toByteArray());
		}
		return buffer.array();
	}

	public static ServerNames from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static ServerNames from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);
		List<ServerName> serverNames = new ArrayList<>();
		int currentLength = 0;
		while (currentLength < length.getValue()) {
			ServerName serverName = ServerName.from(is);
			serverNames.add(serverName);
			currentLength += serverName.sizeOf();
		}

		return new ServerNames(type, length, serverNames, false);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return type.sizeOf() + length.sizeOf() + ByteSizeable.sizeOf(entries);
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public UInt16 getLength() {
		return length;
	}

	public List<ServerName> getEntries() {
		return entries;
	}
}
