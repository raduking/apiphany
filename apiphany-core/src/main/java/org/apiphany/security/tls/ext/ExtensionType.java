package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apiphany.io.UInt16;
import org.apiphany.security.tls.TLSExtension;
import org.apiphany.security.tls.TLSObject;
import org.morphix.lang.Enums;

/**
 * Enumerates all TLS extension types with their protocol codes and parsing logic.
 * <p>
 * Each enum value represents a distinct extension type as defined in various TLS RFCs, along with the factory function
 * to parse the extension data.
 *
 * @see <a href="https://www.iana.org/assignments/tls-extensiontype-values/tls-extensiontype-values.xhtml"> IANA TLS
 * ExtensionType Values Registry</a>
 *
 * @author Radu Sebastian LAZIN
 */
public enum ExtensionType implements TLSObject {

	/**
	 * Server Name Indication extension (RFC 6066).
	 */
	SERVER_NAME((short) 0x0000, ServerNames::from),

	/**
	 * Certificate Status Request extension (RFC 6066).
	 */
	STATUS_REQUEST((short) 0x0005, StatusRequest::from),

	/**
	 * Supported Groups extension (RFC 7919).
	 */
	SUPPORTED_GROUPS((short) 0x000A, SupportedGroups::from),

	/**
	 * EC Point Formats extension (RFC 4492).
	 */
	EC_POINTS_FORMAT((short) 0x000B, ECPointFormats::from),

	/**
	 * Signature Algorithms extension (RFC 5246).
	 */
	SIGNATURE_ALGORITHMS((short) 0x000D, SignatureAlgorithms::from),

	/**
	 * Renegotiation Indication extension (RFC 5746).
	 */
	RENEGOTIATION_INFO((short) 0xFF01, RenegotiationInfo::from),

	/**
	 * Signed Certificate Timestamp extension (RFC 6962).
	 */
	SIGNED_CERTIFICATE_TIMESTAMP((short) 0x0012, SignedCertificateTimestamp::from),

	/**
	 * Session Ticket extension (RFC 5077).
	 */
	SESSION_TICKET((short) 0x0023, SessionTicket::from),

	/**
	 * Extended Master Secret extension (RFC 7627).
	 */
	EXTENDED_MASTER_SECRET((short) 0x0016, ExtendedMasterSecret::from),

	/**
	 * OpenSSL-specific extended renegotiation indication (non-standard).
	 */
	RENEGOTIATION_INFO_EXTENDED((short) 0x0017, RenegotiationInfoExtended::from);

	/**
	 * The size in bytes of an extension type when serialized.
	 */
	public static final int BYTES = 2;

	/**
	 * Functional interface for extension parsing.
	 */
	public interface FromFunction {
		/**
		 * Parses an extension from an input stream.
		 *
		 * @param is the input stream containing extension data
		 * @param type the extension type being parsed
		 * @return the parsed TLSExtension object
		 * @throws IOException if an I/O error occurs
		 */
		TLSExtension from(InputStream is, ExtensionType type) throws IOException;
	}

	/**
	 * Map of extension values to enum constants.
	 */
	private static final Map<Short, ExtensionType> VALUE_MAP = Enums.buildNameMap(values(), ExtensionType::value);

	/**
	 * The protocol-defined value for this extension type.
	 */
	private final short value;

	/**
	 * The function to parse this extension type.
	 */
	private final FromFunction fromFunction;

	/**
	 * Constructs a new {@link ExtensionType} enum constant.
	 *
	 * @param value the protocol-defined value for this extension
	 * @param fromFunction the function to parse this extension type
	 */
	ExtensionType(final short value, final FromFunction fromFunction) {
		this.value = value;
		this.fromFunction = fromFunction;
	}

	/**
	 * Looks up an extension type by its protocol value.
	 *
	 * @param value the extension type code to look up
	 * @return the matching ExtensionType enum constant
	 * @throws IllegalArgumentException if no matching extension type is found
	 */
	public static ExtensionType fromValue(final short value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	/**
	 * Returns the protocol-defined value for this extension type.
	 *
	 * @return the 2-byte value identifying this extension type
	 */
	public short value() {
		return value;
	}

	/**
	 * Returns the size of this extension type when serialized.
	 *
	 * @return always returns {@value #BYTES} (2) as extension types are always two bytes
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Returns the binary representation of this extension type.
	 *
	 * @return 2-byte array containing the extension type code
	 */
	@Override
	public byte[] toByteArray() {
		return UInt16.toByteArray(value);
	}

	/**
	 * Parses an extension of this type from an input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @return the parsed TLSExtension object
	 * @throws IOException if an I/O error occurs
	 */
	public TLSExtension extensionFrom(final InputStream is) throws IOException {
		return fromFunction.from(is, this);
	}
}
