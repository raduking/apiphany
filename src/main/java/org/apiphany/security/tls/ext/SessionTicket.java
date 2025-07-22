package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.tls.TLSExtension;

/**
 * Represents the Session Ticket extension for TLS session resumption.
 * <p>
 * This extension enables stateless session resumption by allowing the server to issue encrypted session tickets to
 * clients instead of maintaining server-side session state.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5077">RFC 5077 - Session Ticket Extension</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class SessionTicket implements TLSExtension {

	/**
	 * The extension type {@link ExtensionType#SESSION_TICKET}.
	 */
	private final ExtensionType type;

	/**
	 * The length of the session ticket data.
	 */
	private final UInt16 length;

	/**
	 * Constructs a SessionTicket extension with specified fields.
	 *
	 * @param type the extension type (should be SESSION_TICKET)
	 * @param length the length of the ticket data
	 */
	public SessionTicket(final ExtensionType type, final UInt16 length) {
		this.type = type;
		this.length = length;
	}

	/**
	 * Constructs an empty SessionTicket extension.
	 * <p>
	 * Creates a default instance with zero length, typically used when requesting a new session ticket from the server.
	 */
	public SessionTicket() {
		this(ExtensionType.SESSION_TICKET, UInt16.ZERO);
	}

	/**
	 * Parses a SessionTicket extension from an input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @return the parsed SessionTicket object
	 * @throws IOException if an I/O error occurs
	 */
	public static SessionTicket from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	/**
	 * Parses a SessionTicket extension with known extension type.
	 *
	 * @param is the input stream containing the extension data
	 * @param type the expected extension type
	 * @return the parsed SessionTicket object
	 * @throws IOException if an I/O error occurs
	 */
	public static SessionTicket from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);

		return new SessionTicket(type, length);
	}

	/**
	 * Returns the binary representation of this extension.
	 *
	 * @return byte array containing the type and length fields
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		return buffer.array();
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
		return type.sizeOf() + length.sizeOf();
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
	 * Returns the length of the ticket data.
	 *
	 * @return the UInt16 wrapper containing the length
	 */
	public UInt16 getLength() {
		return length;
	}
}
