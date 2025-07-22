package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt24;
import org.apiphany.json.JsonBuilder;

/**
 * Represents a TLS Certificate message containing X.509 certificates.
 * <p>
 * This class encapsulates the certificate chain sent by the server during the TLS handshake, including both the leaf
 * certificate and any intermediate CAs.
 *
 * @author Radu Sebastian LAZIN
 */
public class Certificate implements TLSObject {

	/**
	 * The length of the certificate data in bytes.
	 */
	private final UInt24 length;

	/**
	 * The raw certificate data in ASN.1 DER format.
	 */
	private final BytesWrapper data;

	/**
	 * Constructs a Certificate with length wrapper and data wrapper.
	 *
	 * @param length the length of certificate data
	 * @param data the wrapped certificate bytes
	 */
	public Certificate(final UInt24 length, final BytesWrapper data) {
		this.length = length;
		this.data = data;
	}

	/**
	 * Constructs a Certificate with length wrapper and raw bytes.
	 *
	 * @param length the length of certificate data
	 * @param bytes the raw certificate bytes
	 */
	public Certificate(final UInt24 length, final byte[] bytes) {
		this(length, new BytesWrapper(bytes));
	}

	/**
	 * Constructs a Certificate with primitive length and raw bytes.
	 *
	 * @param length the length of certificate data
	 * @param bytes the raw certificate bytes
	 */
	public Certificate(final int length, final byte[] bytes) {
		this(UInt24.of(length), bytes);
	}

	/**
	 * Parses a Certificate from an input stream.
	 *
	 * @param is the input stream containing certificate data
	 * @return the parsed Certificate object
	 * @throws IOException if an I/O error occurs
	 */
	public static Certificate from(final InputStream is) throws IOException {
		UInt24 length = UInt24.from(is);
		BytesWrapper data = BytesWrapper.from(is, length.getValue());
		return new Certificate(length, data);
	}

	/**
	 * Returns the binary representation of this Certificate.
	 *
	 * @return byte array containing length and certificate data
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(length.toByteArray());
		buffer.put(data.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this Certificate.
	 *
	 * @return JSON string containing certificate information
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of length field plus certificate data
	 */
	@Override
	public int sizeOf() {
		return length.sizeOf() + data.sizeOf();
	}

	/**
	 * Returns the length of certificate data.
	 *
	 * @return the UInt24 wrapper containing data length
	 */
	public UInt24 getLength() {
		return length;
	}

	/**
	 * Returns the certificate data.
	 *
	 * @return the BytesWrapper containing certificate bytes
	 */
	public BytesWrapper getData() {
		return data;
	}
}
