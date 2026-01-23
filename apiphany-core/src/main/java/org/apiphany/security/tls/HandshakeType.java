package org.apiphany.security.tls;

import java.io.InputStream;
import java.util.Map;

import org.apiphany.io.UInt8;
import org.morphix.lang.Enums;
import org.morphix.lang.JavaObjects;

/**
 * Enumerates all possible TLS handshake message types and their associated parsing logic.
 * <p>
 * Each enum value represents a distinct handshake message type as defined in the TLS specification, and includes:
 * <ul>
 * <li>The protocol-defined byte value for the message type</li>
 * <li>The factory function to parse the handshake message body</li>
 * <li>Methods to convert between byte values and enum constants</li>
 * </ul>
 *
 * <p>
 * This enum implements {@link TLSObject} as the handshake type itself is transmitted as part of TLS protocol messages
 * and has a well-defined binary representation.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-7.4">RFC 5246 - Handshake Protocol</a>
 * @see TLSObject
 *
 * @author Radu Sebastian LAZIN
 */
public enum HandshakeType implements TLSObject {

	/**
	 * Initial client message to begin TLS negotiation. Contains client capabilities and random data.
	 */
	CLIENT_HELLO((byte) 0x01, FromFunction.ignoreSize(ClientHello::from)),

	/**
	 * Server's response to CLIENT_HELLO, selecting connection parameters.
	 */
	SERVER_HELLO((byte) 0x02, FromFunction.ignoreSize(ServerHello::from)),

	/**
	 * Contains the server's certificate chain.
	 */
	CERTIFICATE((byte) 0x0B, FromFunction.ignoreSize(Certificates::from)),

	/**
	 * Contains cryptographic information for key exchange when certificate doesn't contain enough data.
	 */
	SERVER_KEY_EXCHANGE((byte) 0x0C, FromFunction.ignoreSize(ServerKeyExchange::from)),

	/**
	 * Signals end of server hello and associated messages.
	 */
	SERVER_HELLO_DONE((byte) 0x0E, FromFunction.ignoreSize(ServerHelloDone::from)),

	/**
	 * Contains client's contribution to key exchange.
	 */
	CLIENT_KEY_EXCHANGE((byte) 0x10, ClientKeyExchange::from),

	/**
	 * Final handshake message containing verify data for handshake verification.
	 */
	FINISHED((byte) 0x14, Finished::from);

	/**
	 * The size in bytes of a handshake type when serialized.
	 */
	public static final int BYTES = 1;

	/**
	 * Value map for easy {@link #fromValue(byte)} implementation.
	 */
	private static final Map<Byte, HandshakeType> VALUE_MAP = Enums.buildNameMap(values(), HandshakeType::value);

	/**
	 * The TLS encapsulated value.
	 */
	private final byte value;

	/**
	 * Factory method to build the handshake object from an {@link InputStream}.
	 */
	private final FromFunction<? extends TLSHandshakeBody> fromFunction;

	/**
	 * Constructs a new handshake type enum constant.
	 *
	 * @param value The protocol-defined byte value for this handshake type
	 * @param fromFunction The factory function to parse this handshake type's body
	 */
	HandshakeType(final byte value, final FromFunction<? extends TLSHandshakeBody> fromFunction) {
		this.value = value;
		this.fromFunction = fromFunction;
	}

	/**
	 * Returns the protocol-defined byte value for this handshake type.
	 *
	 * @return The byte value used in the TLS protocol to identify this handshake type
	 */
	public byte value() {
		return value;
	}

	/**
	 * Looks up a handshake type by its protocol byte value.
	 *
	 * @param value The byte value to look up
	 * @return The corresponding HandshakeType enum constant
	 * @throws IllegalArgumentException if the value doesn't correspond to a known handshake type
	 */
	public static HandshakeType fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	/**
	 * Returns {@link #BYTES} (1) as handshake types are always transmitted as a single byte.
	 *
	 * @return 1 as handshake types are always transmitted as a single byte
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Returns the binary representation of this handshake type.
	 *
	 * @return A single-element byte array containing this handshake type's value
	 */
	@Override
	public byte[] toByteArray() {
		return UInt8.toByteArray(value);
	}

	/**
	 * Returns the factory function capable of parsing this handshake type's message body.
	 *
	 * @param <T> the handshake body type
	 *
	 * @return the FromFunction that can deserialize this handshake type's body
	 */
	public <T extends TLSHandshakeBody> FromFunction<T> handshake() {
		return JavaObjects.cast(fromFunction);
	}
}
