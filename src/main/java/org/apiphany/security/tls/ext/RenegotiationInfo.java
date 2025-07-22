package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.tls.TLSExtension;

/**
 * Represents the TLS Renegotiation Indication extension.
 * <p>
 * This extension provides a secure way to indicate support for TLS renegotiation and prevents attacks against the
 * renegotiation process.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5746">RFC 5746 - TLS Renegotiation Indication</a>
 * @author Radu Sebastian LAZIN
 */
public class RenegotiationInfo implements TLSExtension {

	/**
	 * The extension type {@link ExtensionType#RENEGOTIATION_INFO}.
	 */
	private final ExtensionType type;

	/**
	 * The total size of the extension data.
	 */
	private final UInt16 size;

	/**
	 * The length of the renegotiation info payload.
	 */
	private final UInt8 length;

	/**
	 * Constructs a RenegotiationInfo extension with all fields specified.
	 *
	 * @param type the extension type (should be RENEGOTIATION_INFO)
	 * @param size the total extension data size
	 * @param length the length of the renegotiation info
	 */
	public RenegotiationInfo(final ExtensionType type, final UInt16 size, final UInt8 length) {
		this.type = type;
		this.size = size;
		this.length = length;
	}

	/**
	 * Constructs a RenegotiationInfo extension with primitive values.
	 *
	 * @param type the extension type
	 * @param size the total extension data size as short
	 * @param length the info length as byte
	 */
	public RenegotiationInfo(final ExtensionType type, final short size, final byte length) {
		this(type, UInt16.of(size), UInt8.of(length));
	}

	/**
	 * Constructs an empty RenegotiationInfo extension.
	 * <p>
	 * Creates a default instance indicating support for secure renegotiation with empty verification data.
	 */
	public RenegotiationInfo() {
		this(ExtensionType.RENEGOTIATION_INFO, (short) 0x0001, (byte) 0x00);
	}

	/**
	 * Parses a RenegotiationInfo extension from an input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @return the parsed RenegotiationInfo object
	 * @throws IOException if an I/O error occurs
	 */
	public static RenegotiationInfo from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	/**
	 * Parses a RenegotiationInfo extension with known extension type.
	 *
	 * @param is the input stream containing the extension data
	 * @param type the expected extension type
	 * @return the parsed RenegotiationInfo object
	 * @throws IOException if an I/O error occurs
	 */
	public static RenegotiationInfo from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 size = UInt16.from(is);
		UInt8 length = UInt8.from(is);

		return new RenegotiationInfo(type, size, length);
	}

	/**
	 * Returns the binary representation of this extension.
	 *
	 * @return byte array containing all extension fields
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(size.toByteArray());
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
		return type.sizeOf() + size.sizeOf() + length.sizeOf();
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
	 * Returns the extension data size.
	 *
	 * @return the UInt16 wrapper containing the total size
	 */
	public UInt16 getSize() {
		return size;
	}

	/**
	 * Returns the renegotiation info length.
	 *
	 * @return the UInt8 wrapper containing the info length
	 */
	public UInt8 getLength() {
		return length;
	}
}
