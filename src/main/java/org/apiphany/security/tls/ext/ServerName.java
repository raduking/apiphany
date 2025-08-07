package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.apiphany.security.tls.TLSObject;

/**
 * Represents a Server Name Indication (SNI) extension entry in TLS.
 * <p>
 * This class implements the server_name extension from RFC 6066, allowing clients to specify which hostname they are
 * trying to connect to during the handshake.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6066#section-3">RFC 6066 - Server Name Indication</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class ServerName implements TLSObject {

	/**
	 * The total size of this ServerName entry in bytes.
	 */
	private final UInt16 size;

	/**
	 * The name type (0 for host_name).
	 */
	private final UInt8 type;

	/**
	 * The length of the name field in bytes.
	 */
	private final UInt16 length;

	/**
	 * The actual server name value.
	 */
	private final BytesWrapper name;

	/**
	 * Constructs a ServerName with explicit size fields.
	 *
	 * @param size the total size of this ServerName entry
	 * @param type the name type (typically 0 for host_name)
	 * @param length the length of the name value
	 * @param name the wrapped server name bytes
	 */
	public ServerName(final UInt16 size, final UInt8 type, final UInt16 length, final BytesWrapper name) {
		this.size = size;
		this.type = type;
		this.length = length;
		this.name = name;
	}

	/**
	 * Constructs a ServerName from a hostname string.
	 *
	 * @param name the hostname to use (will be encoded as ASCII)
	 */
	public ServerName(final String name) {
		this(
				UInt16.of((short) (UInt8.BYTES + UInt16.BYTES + name.length())),
				UInt8.ZERO,
				UInt16.of((short) name.length()),
				new BytesWrapper(name.getBytes(StandardCharsets.US_ASCII)));
	}

	/**
	 * Returns the binary representation of this ServerName.
	 *
	 * @return byte array containing size, type, length and name fields
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(size.toByteArray());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(name.toByteArray());
		return buffer.array();
	}

	/**
	 * Parses a ServerName from an input stream.
	 *
	 * @param is the input stream containing the ServerName data
	 * @return the parsed ServerName object
	 * @throws IOException if an I/O error occurs
	 */
	public static ServerName from(final InputStream is) throws IOException {
		UInt16 size = UInt16.from(is);
		UInt8 type = UInt8.from(is);
		UInt16 length = UInt16.from(is);
		BytesWrapper name = BytesWrapper.from(is, length.getValue());

		return new ServerName(size, type, length, name);
	}

	/**
	 * Returns a JSON representation of this ServerName.
	 *
	 * @return JSON string containing the ServerName information
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes (2 + 1 + 2 + name length)
	 */
	@Override
	public int sizeOf() {
		return size.sizeOf() + type.sizeOf() + length.sizeOf() + name.sizeOf();
	}

	/**
	 * Returns the total size of this ServerName entry.
	 *
	 * @return the UInt16 wrapper containing the total size
	 */
	public UInt16 getSize() {
		return size;
	}

	/**
	 * Returns the name type field.
	 *
	 * @return the UInt8 wrapper containing the name type
	 */
	public UInt8 getType() {
		return type;
	}

	/**
	 * Returns the length of the name field.
	 *
	 * @return the UInt16 wrapper containing the name length
	 */
	public UInt16 getLength() {
		return length;
	}

	/**
	 * Returns the server name bytes.
	 *
	 * @return the BytesWrapper containing the raw name bytes
	 */
	public BytesWrapper getName() {
		return name;
	}

	/**
	 * Returns the server name as an ASCII string.
	 *
	 * @return the decoded hostname string
	 */
	public String getNameASCII() {
		return new String(name.toByteArray(), StandardCharsets.US_ASCII);
	}
}
