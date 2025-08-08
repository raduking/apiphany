package org.apiphany.security.tls;

import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt64;
import org.apiphany.security.ssl.SSLProtocol;

/**
 * Represents Additional Authenticated Data (AAD) used in TLS record encryption.
 * <p>
 * This class encapsulates the data used for integrity protection in AEAD cipher modes, containing metadata about the
 * encrypted record that is authenticated but not encrypted.
 *
 * @author Radu Sebastian LAZIN
 */
public class AdditionalAuthenticatedData implements TLSObject {

	/**
	 * The sequence number of the TLS record.
	 */
	private final UInt64 sequenceNumber;

	/**
	 * The content type of the TLS record.
	 */
	private final RecordContentType type;

	/**
	 * The protocol version being used.
	 */
	private final Version protocolVersion;

	/**
	 * The length of the encrypted record.
	 */
	private final UInt16 length;

	/**
	 * Constructs AAD with all fields specified.
	 *
	 * @param sequenceNumber the record sequence number
	 * @param type the record content type
	 * @param protocolVersion the TLS protocol version
	 * @param length the record length
	 */
	public AdditionalAuthenticatedData(final UInt64 sequenceNumber, final RecordContentType type,
			final Version protocolVersion, final UInt16 length) {
		this.sequenceNumber = sequenceNumber;
		this.type = type;
		this.protocolVersion = protocolVersion;
		this.length = length;
	}

	/**
	 * Constructs AAD with primitive values.
	 *
	 * @param sequenceNumber the record sequence number
	 * @param type the record content type
	 * @param protocol the SSL protocol version
	 * @param length the record length
	 */
	public AdditionalAuthenticatedData(final long sequenceNumber, final RecordContentType type,
			final SSLProtocol protocol, final short length) {
		this(UInt64.of(sequenceNumber), type, new Version(protocol), UInt16.of(length));
	}

	/**
	 * Returns the binary representation of this AAD.
	 *
	 * @return byte array containing all AAD fields
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(sequenceNumber.toByteArray());
		buffer.put(type.toByteArray());
		buffer.put(protocolVersion.toByteArray());
		buffer.put(length.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this AAD.
	 *
	 * @return JSON string containing AAD information
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
		return sequenceNumber.sizeOf()
				+ type.sizeOf()
				+ protocolVersion.sizeOf()
				+ length.sizeOf();
	}

	/**
	 * Returns the sequence number.
	 *
	 * @return the UInt64 wrapper containing sequence number
	 */
	public UInt64 getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * Returns the record content type.
	 *
	 * @return the RecordContentType enum value
	 */
	public RecordContentType getType() {
		return type;
	}

	/**
	 * Returns the protocol version.
	 *
	 * @return the Version object
	 */
	public Version getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * Returns the record length.
	 *
	 * @return the UInt16 wrapper containing length
	 */
	public UInt16 getLength() {
		return length;
	}
}
