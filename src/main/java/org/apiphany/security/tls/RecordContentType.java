package org.apiphany.security.tls;

import java.util.Map;

import org.apiphany.io.UInt8;
import org.morphix.lang.Enums;
import org.morphix.lang.JavaObjects;

/**
 * Represents the content type of a TLS record layer message.
 * <p>
 * This enum defines the different types of messages that can be carried in the TLS record protocol, each with its own
 * parsing logic.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-6.2.1">RFC 5246 - Record Layer</a>
 * @author Radu Sebastian LAZIN
 */
public enum RecordContentType implements TLSObject {

	/**
	 * Signals change of cipher spec (RFC 5246).
	 */
	CHANGE_CIPHER_SPEC((byte) 0x14, FromFunction.ignoreSize(ChangeCipherSpec::from)),

	/**
	 * Alert messages for connection status (RFC 5246).
	 */
	ALERT((byte) 0x15, FromFunction.ignoreSize(Alert::from)),

	/**
	 * Handshake protocol messages (RFC 5246).
	 */
	HANDSHAKE((byte) 0x16, FromFunction.ignoreSize(Handshake::from)),

	/**
	 * Encrypted application data (RFC 5246).
	 */
	APPLICATION_DATA((byte) 0x17, ApplicationData::from),

	/**
	 * Heartbeat messages (RFC 6520).
	 */
	HEARTBEAT((byte) 0x18, null);

	/**
	 * The size in bytes of a content type when serialized.
	 */
	public static final int BYTES = 1;

	/**
	 * Value map for easy {@link #fromValue(byte)} implementation.
	 */
	private static final Map<Byte, RecordContentType> VALUE_MAP = Enums.buildNameMap(values(), RecordContentType::value);

	/**
	 * The protocol-defined value for this content type.
	 */
	private final byte value;

	/**
	 * The function to parse this content type's payload.
	 */
	private final FromFunction<? extends TLSObject> fromFunction;

	/**
	 * Constructs a RecordContentType enum constant.
	 *
	 * @param value the protocol-defined value
	 * @param fromFunction the parsing function
	 */
	RecordContentType(final byte value, final FromFunction<? extends TLSObject> fromFunction) {
		this.value = value;
		this.fromFunction = fromFunction;
	}

	/**
	 * Returns the protocol-defined value.
	 *
	 * @return the byte value identifying this content type
	 */
	public byte value() {
		return value;
	}

	/**
	 * Looks up a content type by its protocol value.
	 *
	 * @param value the content type code to look up
	 * @return the matching RecordContentType enum constant
	 * @throws IllegalArgumentException if no matching type is found
	 */
	public static RecordContentType fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	/**
	 * Determines content type from TLS object type.
	 *
	 * @param tlsObject the object to analyze
	 * @return the appropriate RecordContentType
	 * @throws UnsupportedOperationException for unknown object types
	 */
	public static RecordContentType from(final TLSObject tlsObject) {
		return switch (tlsObject) {
			case Handshake handshake -> HANDSHAKE;
			case TLSHandshakeBody tlsHandshakeBody -> HANDSHAKE;
			case ChangeCipherSpec changeCipherSpec -> CHANGE_CIPHER_SPEC;
			case Encrypted encrypted -> HANDSHAKE;
			case ApplicationData applicationData -> APPLICATION_DATA;
			default -> throw new UnsupportedOperationException("Unknown TLS object type: " + tlsObject.getClass());
		};
	}

	/**
	 * Returns the size when serialized.
	 *
	 * @return always returns {@value #BYTES} (1) as content types are one byte
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Returns the binary representation.
	 *
	 * @return single-byte array containing the content type
	 */
	@Override
	public byte[] toByteArray() {
		return UInt8.toByteArray(value);
	}

	/**
	 * Returns the parsing function for this content type.
	 *
	 * @param <T> TLS object type
	 *
	 * @return the FromFunction that can parse this type's payload
	 */
	public <T extends TLSObject> FromFunction<T> fragment() {
		return JavaObjects.cast(fromFunction);
	}
}
