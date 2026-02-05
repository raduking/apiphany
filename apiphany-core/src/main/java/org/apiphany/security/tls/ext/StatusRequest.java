package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.apiphany.security.tls.TLSExtension;
import org.apiphany.security.tls.TLSObject;

/**
 * Represents a Certificate Status Request extension (RFC 6066).
 * <p>
 * This extension allows clients to request OCSP stapled responses from servers, enabling real-time certificate
 * revocation checking during the TLS handshake.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6066#section-8">RFC 6066 - Certificate Status Request</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class StatusRequest implements TLSExtension {

	/**
	 * The extension type {@link ExtensionType#STATUS_REQUEST}.
	 */
	private final ExtensionType type;

	/**
	 * The length of the extension data.
	 */
	private final UInt16 length;

	/**
	 * The certificate status type (1 for OCSP).
	 */
	private final UInt8 certificateStatusType;

	/**
	 * The size of responder ID list (typically 0).
	 */
	private final UInt16 responderIDInfoSize;

	/**
	 * The size of request extensions (typically 0).
	 */
	private final UInt16 requestExtensionInfoSize;

	/**
	 * Constructs a StatusRequest with all fields specified.
	 *
	 * @param type the extension type (should be STATUS_REQUEST)
	 * @param length the extension data length
	 * @param certificateStatusType the status type (1 for OCSP)
	 * @param responderIDInfoSize the size of responder ID list
	 * @param requestExtensionInfoSize the size of request extensions
	 */
	public StatusRequest(
			final ExtensionType type,
			final UInt16 length,
			final UInt8 certificateStatusType,
			final UInt16 responderIDInfoSize,
			final UInt16 requestExtensionInfoSize) {
		this.type = type;
		this.length = length;
		this.certificateStatusType = certificateStatusType;
		this.responderIDInfoSize = responderIDInfoSize;
		this.requestExtensionInfoSize = requestExtensionInfoSize;
	}

	/**
	 * Constructs a default {@link StatusRequest} for OCSP stapling.
	 */
	public StatusRequest() {
		this(ExtensionType.STATUS_REQUEST, UInt16.of((short) 0x0005), UInt8.of((byte) 0x01), UInt16.ZERO, UInt16.ZERO);
	}

	/**
	 * Parses a {@link StatusRequest} from an input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @return the parsed StatusRequest object
	 * @throws IOException if an I/O error occurs
	 */
	public static StatusRequest from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	/**
	 * Parses a {@link StatusRequest} with known extension type.
	 *
	 * @param is the input stream containing the extension data
	 * @param type the expected extension type
	 * @return the parsed StatusRequest object
	 * @throws IOException if an I/O error occurs
	 */
	public static StatusRequest from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);
		UInt8 certificateStatusType = UInt8.from(is);
		UInt16 responderIDInfoSize = UInt16.from(is);
		UInt16 requestExtensionInfoSize = UInt16.from(is);

		return new StatusRequest(type, length, certificateStatusType, responderIDInfoSize, requestExtensionInfoSize);
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
		buffer.put(length.toByteArray());
		buffer.put(certificateStatusType.toByteArray());
		buffer.put(responderIDInfoSize.toByteArray());
		buffer.put(requestExtensionInfoSize.toByteArray());
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
		return type.sizeOf()
				+ length.sizeOf()
				+ certificateStatusType.sizeOf()
				+ responderIDInfoSize.sizeOf()
				+ requestExtensionInfoSize.sizeOf();
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
	 * Returns the extension data length.
	 *
	 * @return the UInt16 wrapper containing the length
	 */
	public UInt16 getLength() {
		return length;
	}

	/**
	 * Returns the certificate status type.
	 *
	 * @return the UInt8 wrapper containing the status type (1 for OCSP)
	 */
	public UInt8 getCertificateStatusType() {
		return certificateStatusType;
	}

	/**
	 * Returns the responder ID list size.
	 *
	 * @return the UInt16 wrapper containing the size
	 */
	public UInt16 getResponderIDInfoSize() {
		return responderIDInfoSize;
	}

	/**
	 * Returns the request extensions size.
	 *
	 * @return the UInt16 wrapper containing the size
	 */
	public UInt16 getRequestExtensionInfoSize() {
		return requestExtensionInfoSize;
	}
}
