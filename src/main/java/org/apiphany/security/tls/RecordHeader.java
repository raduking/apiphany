package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

/**
 * Represents the header of a TLS record layer message.
 * <p>
 * This class encapsulates the metadata that precedes every TLS record, including the content type, protocol version,
 * and payload length.
 *
 * @author Radu Sebastian LAZIN
 */
public class RecordHeader implements TLSObject {

	/**
	 * The fixed size of a record header in bytes.
	 */
	public static final int BYTES = 5;

	/**
	 * The type of content in the record.
	 */
	private final RecordContentType type;

	/**
	 * The protocol version being used.
	 */
	private final Version version;

	/**
	 * The length of the record payload.
	 */
	private final UInt16 length;

	/**
	 * Constructs a RecordHeader with all fields specified.
	 *
	 * @param type the record content type
	 * @param version the protocol version
	 * @param length the payload length
	 */
	public RecordHeader(final RecordContentType type, final Version version, final UInt16 length) {
		this.type = type;
		this.version = version;
		this.length = length;
	}

	/**
	 * Constructs a RecordHeader with SSLProtocol and primitive length.
	 *
	 * @param type the record content type
	 * @param sslProtocol the SSL protocol version
	 * @param length the payload length as short
	 */
	public RecordHeader(final RecordContentType type, final SSLProtocol sslProtocol, final short length) {
		this(type, Version.of(sslProtocol), UInt16.of(length));
	}

	/**
	 * Constructs a RecordHeader with default zero length.
	 *
	 * @param type the record content type
	 * @param sslProtocol the SSL protocol version
	 */
	public RecordHeader(final RecordContentType type, final SSLProtocol sslProtocol) {
		this(type, sslProtocol, (short) 0x0000);
	}

	/**
	 * Parses a RecordHeader from an input stream.
	 *
	 * @param is the input stream containing header data
	 * @return the parsed RecordHeader object
	 * @throws IOException if an I/O error occurs
	 */
	public static RecordHeader from(final InputStream is) throws IOException {
		UInt8 int8 = UInt8.from(is);
		RecordContentType type = RecordContentType.fromValue(int8.getValue());
		Version version = Version.from(is);
		UInt16 length = UInt16.from(is);
		return new RecordHeader(type, version, length);
	}

	/**
	 * Returns the binary representation of this RecordHeader.
	 *
	 * @return byte array containing type, version and length
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(version.toByteArray());
		buffer.put(length.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this RecordHeader.
	 *
	 * @return JSON string containing header information
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return always returns {@value #BYTES} (5) bytes
	 */
	@Override
	public int sizeOf() {
		return type.sizeOf() + version.sizeOf() + length.sizeOf();
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
	public Version getVersion() {
		return version;
	}

	/**
	 * Returns the payload length.
	 *
	 * @return the UInt16 wrapper containing length
	 */
	public UInt16 getLength() {
		return length;
	}
}
