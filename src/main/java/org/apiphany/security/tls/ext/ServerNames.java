package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.UInt16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;
import org.apiphany.security.tls.TLSExtension;

/**
 * Represents the Server Name Indication (SNI) extension in TLS.
 * <p>
 * This extension allows clients to specify the hostname they are trying to connect to during the TLS handshake,
 * enabling servers to present the appropriate certificate for multi-host configurations.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6066#section-3">RFC 6066 - Server Name Indication</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class ServerNames implements TLSExtension {

	/**
	 * The extension type (server_name).
	 */
	private final ExtensionType type;

	/**
	 * The total length of the server names list.
	 */
	private final UInt16 length;

	/**
	 * The list of server name entries.
	 */
	private final List<ServerName> entries;

	/**
	 * Constructs a ServerNames extension with optional length updating.
	 *
	 * @param type the extension type (should be SERVER_NAME)
	 * @param length the initial length value
	 * @param entries the list of server name entries
	 * @param updateLength if true, the length will be recalculated from entries
	 */
	private ServerNames(final ExtensionType type, final UInt16 length,
			final List<ServerName> entries, final boolean updateLength) {
		this.type = type;
		this.length = updateLength ? UInt16.of((short) ByteSizeable.sizeOf(entries)) : length;
		this.entries = entries;
	}

	/**
	 * Constructs a ServerNames extension with automatic length calculation.
	 *
	 * @param size the initial size value
	 * @param serverNames the list of server names
	 */
	public ServerNames(final UInt16 size, final List<ServerName> serverNames) {
		this(ExtensionType.SERVER_NAME, size, serverNames, true);
	}

	/**
	 * Constructs a ServerNames extension from hostname strings.
	 *
	 * @param names the list of hostnames to include
	 */
	public ServerNames(final List<String> names) {
		this(UInt16.of((short) names.size()), names.stream().map(ServerName::new).toList());
	}

	/**
	 * Returns the binary representation of this extension.
	 *
	 * @return byte array containing the type, length and all server name entries
	 */
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

	/**
	 * Parses a ServerNames extension from an input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @return the parsed ServerNames object
	 * @throws IOException if an I/O error occurs
	 */
	public static ServerNames from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());
		return from(is, extensionType);
	}

	/**
	 * Parses a ServerNames extension with known extension type.
	 *
	 * @param is the input stream containing the extension data
	 * @param type the expected extension type
	 * @return the parsed ServerNames object
	 * @throws IOException if an I/O error occurs
	 */
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

	/**
	 * Returns a JSON representation of this extension.
	 *
	 * @return JSON string containing the extension data
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of all fields combined
	 */
	@Override
	public int sizeOf() {
		return type.sizeOf() + length.sizeOf() + ByteSizeable.sizeOf(entries);
	}

	/**
	 * Returns the extension type.
	 *
	 * @return the ExtensionType enum value
	 */
	@Override
	public ExtensionType getType() {
		return type;
	}

	/**
	 * Returns the length of the server names list.
	 *
	 * @return the UInt16 wrapper containing the length
	 */
	public UInt16 getLength() {
		return length;
	}

	/**
	 * Returns the list of server name entries.
	 *
	 * @return list of ServerName objects
	 */
	public List<ServerName> getEntries() {
		return entries;
	}
}
