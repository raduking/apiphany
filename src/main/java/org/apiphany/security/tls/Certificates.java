package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.UInt24;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;

/**
 * Represents a TLS Certificate message containing a chain of X.509 certificates.
 * <p>
 * This class encapsulates the complete certificate chain sent by the server, including the leaf certificate and all
 * intermediate certificates up to (but excluding) the trusted root certificate.
 *
 * @author Radu Sebastian LAZIN
 */
public class Certificates implements TLSHandshakeBody {

	/**
	 * The total length of all certificates in bytes.
	 */
	private final UInt24 length;

	/**
	 * The list of certificates in the chain.
	 */
	private final List<Certificate> list;

	/**
	 * Constructs a Certificates message with optional length updating.
	 *
	 * @param length the initial length value
	 * @param list the list of certificates
	 * @param updateLength if true, the length will be recalculated
	 */
	public Certificates(final UInt24 length, final List<Certificate> list, final boolean updateLength) {
		this.list = list;
		this.length = updateLength ? UInt24.of(length.sizeOf() + ByteSizeable.sizeOf(list)) : length;
	}

	/**
	 * Constructs a Certificates message with automatic length calculation.
	 *
	 * @param length the initial length value
	 * @param list the list of certificates
	 */
	public Certificates(final UInt24 length, final List<Certificate> list) {
		this(length, list, true);
	}

	/**
	 * Parses a Certificates message from an input stream.
	 *
	 * @param is the input stream containing certificate data
	 * @return the parsed Certificates object
	 * @throws IOException if an I/O error occurs
	 */
	public static Certificates from(final InputStream is) throws IOException {
		UInt24 length = UInt24.from(is);
		List<Certificate> certificates = new ArrayList<>();
		int certificatesLength = length.getValue();
		while (certificatesLength > 0) {
			Certificate certificate = Certificate.from(is);
			certificates.add(certificate);
			certificatesLength -= certificate.sizeOf();
		}
		return new Certificates(length, certificates, false);
	}

	/**
	 * Returns the binary representation of this Certificates message.
	 *
	 * @return byte array containing length and all certificates
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(length.toByteArray());
		for (Certificate certificate : list) {
			buffer.put(certificate.toByteArray());
		}
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this Certificates message.
	 *
	 * @return JSON string containing certificate chain information
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of length field plus all certificates
	 */
	@Override
	public int sizeOf() {
		return length.sizeOf() + ByteSizeable.sizeOf(list);
	}

	/**
	 * Returns the handshake message type.
	 *
	 * @return always returns CERTIFICATE
	 */
	@Override
	public HandshakeType getType() {
		return HandshakeType.CERTIFICATE;
	}

	/**
	 * Returns the total length of certificate data.
	 *
	 * @return the UInt24 wrapper containing total length
	 */
	public UInt24 getLength() {
		return length;
	}

	/**
	 * Returns the certificate chain.
	 *
	 * @return list of Certificate objects
	 */
	public List<Certificate> getList() {
		return list;
	}
}
