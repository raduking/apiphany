package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.tls.TLSExtension;

/**
 * Represents the OpenSSL-specific extended renegotiation indication extension.
 * <p>
 * This non-standard extension provides additional renegotiation protection in OpenSSL implementations beyond what's
 * specified in RFC 5746.
 *
 * @see <a href="https://www.openssl.org/docs/man1.0.2/man3/SSL_CTX_set_options.html"> OpenSSL SSL_CTX_set_options
 * documentation</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class RenegotiationInfoExtended implements TLSExtension {

	/**
	 * The extension type {@link ExtensionType#RENEGOTIATION_INFO_EXTENDED}.
	 */
	private final ExtensionType type;

	/**
	 * The length of the extension data.
	 */
	private final UInt16 length;

	/**
	 * Constructs a RenegotiationInfoExtended extension with specified fields.
	 *
	 * @param type the extension type (should be RENEGOTIATION_INFO_EXTENDED)
	 * @param length the length of the extension data
	 */
	public RenegotiationInfoExtended(final ExtensionType type, final UInt16 length) {
		this.type = type;
		this.length = length;
	}

	/**
	 * Constructs a default RenegotiationInfoExtended extension.
	 * <p>
	 * Creates an empty instance indicating support for OpenSSL's extended renegotiation protection.
	 */
	public RenegotiationInfoExtended() {
		this(ExtensionType.RENEGOTIATION_INFO_EXTENDED, UInt16.ZERO);
	}

	/**
	 * Parses a RenegotiationInfoExtended extension from an input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @return the parsed RenegotiationInfoExtended object
	 * @throws IOException if an I/O error occurs
	 */
	public static RenegotiationInfoExtended from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());
		return from(is, extensionType);
	}

	/**
	 * Parses a RenegotiationInfoExtended extension with known extension type.
	 *
	 * @param is the input stream containing the extension data
	 * @param type the expected extension type
	 * @return the parsed RenegotiationInfoExtended object
	 * @throws IOException if an I/O error occurs
	 */
	public static RenegotiationInfoExtended from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);
		return new RenegotiationInfoExtended(type, length);
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
	 * Returns the length of the extension data.
	 *
	 * @return the UInt16 wrapper containing the length
	 */
	public UInt16 getLength() {
		return length;
	}
}
