package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt16;
import org.apiphany.security.tls.TLSExtension;
import org.apiphany.security.tls.TLSObject;

/**
 * Represents the Signed Certificate Timestamp extension for Certificate Transparency.
 * <p>
 * This extension is used to provide proof that a certificate has been submitted to Certificate Transparency logs,
 * helping detect misissued certificates.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6962">RFC 6962 - Certificate Transparency</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class SignedCertificateTimestamp implements TLSExtension {

	/**
	 * The extension type {@link ExtensionType#SIGNED_CERTIFICATE_TIMESTAMP}.
	 */
	private final ExtensionType type;

	/**
	 * The length of the SCT data.
	 */
	private final UInt16 length;

	/**
	 * The SCT data bytes.
	 */
	private final BytesWrapper data;

	/**
	 * Constructs a SignedCertificateTimestamp extension with specified fields.
	 *
	 * @param type the extension type (should be SIGNED_CERTIFICATE_TIMESTAMP)
	 * @param length the length of the SCT data
	 * @param data the SCT data bytes
	 */
	public SignedCertificateTimestamp(final ExtensionType type, final UInt16 length, final BytesWrapper data) {
		this.type = type;
		this.length = length;
		this.data = data;
	}

	/**
	 * Constructs an empty {@link SignedCertificateTimestamp} extension.
	 * <p>
	 * Creates a default instance with zero length, typically used when no SCT data is available but the extension needs to
	 * be present.
	 */
	public SignedCertificateTimestamp() {
		this(ExtensionType.SIGNED_CERTIFICATE_TIMESTAMP, UInt16.ZERO, BytesWrapper.empty());
	}

	/**
	 * Parses a {@link SignedCertificateTimestamp} extension from an input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @return the parsed SignedCertificateTimestamp object
	 * @throws IOException if an I/O error occurs
	 */
	public static SignedCertificateTimestamp from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	/**
	 * Parses a {@link SignedCertificateTimestamp} extension with known extension type. This method assumes the type has
	 * already been read from the given input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @param type the expected extension type
	 * @return the parsed SignedCertificateTimestamp object
	 * @throws IOException if an I/O error occurs
	 */
	public static SignedCertificateTimestamp from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);
		BytesWrapper data = BytesWrapper.from(is, length.getValue());

		return new SignedCertificateTimestamp(type, length, data);
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
		buffer.put(data.toByteArray());
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
		return type.sizeOf() + length.sizeOf() + data.sizeOf();
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
	 * Returns the length of the SCT data.
	 *
	 * @return the UInt16 wrapper containing the length
	 */
	public UInt16 getLength() {
		return length;
	}

	/**
	 * Returns the SCT data bytes.
	 *
	 * @return the BytesWrapper containing the SCT data
	 */
	public BytesWrapper getData() {
		return data;
	}
}
