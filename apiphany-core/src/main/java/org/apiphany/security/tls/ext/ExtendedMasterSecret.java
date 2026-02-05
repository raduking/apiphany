package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.security.tls.TLSExtension;
import org.apiphany.security.tls.TLSObject;

/**
 * Represents the Extended Master Secret extension for TLS.
 * <p>
 * This extension strengthens the master secret derivation process to prevent certain types of attacks by incorporating
 * the full handshake transcript.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7627">RFC 7627 - Extended Master Secret</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class ExtendedMasterSecret implements TLSExtension {

	/**
	 * The extension type {@link ExtensionType#EXTENDED_MASTER_SECRET}.
	 */
	private final ExtensionType type;

	/**
	 * The length of the extension data.
	 */
	private final UInt16 length;

	/**
	 * Constructs an ExtendedMasterSecret extension with specified fields.
	 *
	 * @param type the extension type (should be EXTENDED_MASTER_SECRET)
	 * @param length the length of the extension data
	 */
	public ExtendedMasterSecret(final ExtensionType type, final UInt16 length) {
		this.type = type;
		this.length = length;
	}

	/**
	 * Constructs a default {@link ExtendedMasterSecret} extension.
	 * <p>
	 * Creates an empty instance indicating support for the extension, with zero-length data as specified in RFC 7627.
	 */
	public ExtendedMasterSecret() {
		this(ExtensionType.EXTENDED_MASTER_SECRET, UInt16.ZERO);
	}

	/**
	 * Parses an {@link ExtendedMasterSecret} extension from an input stream. This method assumes the type has already been
	 * read from the given input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @return the parsed ExtendedMasterSecret object
	 * @throws IOException if an I/O error occurs
	 */
	public static ExtendedMasterSecret from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	/**
	 * Parses an {@link ExtendedMasterSecret} extension with known extension type.
	 *
	 * @param is the input stream containing the extension data
	 * @param type the expected extension type
	 * @return the parsed ExtendedMasterSecret object
	 * @throws IOException if an I/O error occurs
	 */
	public static ExtendedMasterSecret from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);

		return new ExtendedMasterSecret(type, length);
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
		return TLSObject.serialize(this);
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
	 * Returns the length of the extension data.
	 *
	 * @return the UInt16 wrapper containing the length
	 */
	public UInt16 getLength() {
		return length;
	}
}
