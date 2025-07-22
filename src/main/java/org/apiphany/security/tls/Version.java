package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.io.UInt16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents a TLS protocol version in both its wire format and semantic version.
 * <p>
 * This class encapsulates the two-byte version representation used in the TLS protocol, providing conversion between
 * the numeric format and semantic version names (e.g., TLSv1.2). It implements {@link TLSObject} for serialization and
 * deserialization purposes.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-6.2.1">RFC 5246 - Protocol Version</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class Version implements TLSObject {

	/**
	 * The size in bytes of a version field when serialized.
	 */
	public static final int BYTES = 2;

	/**
	 * The SSL protocol.
	 */
	private final SSLProtocol protocol;

	/**
	 * Constructs a new Version instance for the specified protocol.
	 *
	 * @param protocol The SSL/TLS protocol version to represent
	 */
	public Version(final SSLProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * Creates a new Version instance for the specified protocol.
	 *
	 * @param protocol the SSL/TLS protocol version to represent
	 * @return a new Version instance
	 */
	public static Version of(final SSLProtocol protocol) {
		return new Version(protocol);
	}

	/**
	 * Parses a Version from an input stream.
	 *
	 * @param is the input stream containing the version bytes
	 * @return the parsed Version object
	 * @throws IOException If an I/O error occurs while reading
	 * @throws IllegalArgumentException If the version bytes are invalid
	 */
	public static Version from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		SSLProtocol protocol = SSLProtocol.fromVersion(int16.getValue());

		return Version.of(protocol);
	}

	/**
	 * Returns the binary representation of this version.
	 *
	 * @return a two-byte array containing the protocol version in wire format
	 */
	@Override
	public byte[] toByteArray() {
		return UInt16.toByteArray(protocol.handshakeVersion());
	}

	/**
	 * Returns a JSON representation of this version.
	 *
	 * @return a JSON string representing this version
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the size of this version when serialized.
	 *
	 * @return always returns {@value #BYTES} (2) as versions are always two bytes
	 */
	@Override
	public int sizeOf() {
		return BYTES;
	}

	/**
	 * Returns the protocol version represented by this object.
	 *
	 * @return the SSLProtocol enum constant representing this version
	 */
	public SSLProtocol getProtocol() {
		return protocol;
	}

	/**
	 * Returns the string representation of this protocol version.
	 * <p>
	 * This method is marked with {@code @JsonValue} for proper JSON serialization.
	 *
	 * @return the string name of the protocol version (e.g., "TLSv1.2")
	 */
	@JsonValue
	public String getProtocolString() {
		return protocol.value();
	}
}
