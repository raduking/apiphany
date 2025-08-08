package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.ByteSizeable;
import org.apiphany.io.UInt16;

/**
 * Represents a collection of TLS cipher suites offered during handshake negotiation.
 * <p>
 * This class manages the list of cryptographic algorithm combinations that a client or server supports for TLS
 * connections. Each cipher suite specifies key exchange, authentication, encryption, and MAC algorithms.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-7.4.1.2">RFC 5246 - Cipher Suites</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class CipherSuites implements TLSObject {

	/**
	 * The size in bytes of the cipher suites list.
	 */
	private final UInt16 size;

	/**
	 * The list of cipher suites.
	 */
	private final List<CipherSuite> suites;

	/**
	 * Constructs a cipher suites list with explicit size and list of suites.
	 *
	 * @param size the number of bytes needed to represent all cipher suites
	 * @param cipherSuites the list of cipher suites to include
	 */
	public CipherSuites(final UInt16 size, final List<CipherSuite> cipherSuites) {
		this.size = size;
		this.suites = new ArrayList<>(size.getValue());
		this.suites.addAll(cipherSuites);
	}

	/**
	 * Constructs a cipher suites list with primitive size and list of suites.
	 *
	 * @param size the number of bytes needed to represent all cipher suites
	 * @param cipherSuites the list of cipher suites to include
	 */
	public CipherSuites(final short size, final List<CipherSuite> cipherSuites) {
		this(UInt16.of(size), cipherSuites);
	}

	/**
	 * Constructs a cipher suites list calculating size automatically.
	 *
	 * @param cipherSuites the list of cipher suites to include
	 */
	public CipherSuites(final List<CipherSuite> cipherSuites) {
		this((short) (cipherSuites.size() * CipherSuite.BYTES), cipherSuites);
	}

	/**
	 * Constructs a cipher suites list with a single cipher suite.
	 *
	 * @param cipherSuites the single cipher suite to include
	 */
	public CipherSuites(final CipherSuite cipherSuites) {
		this(List.of(cipherSuites));
	}

	/**
	 * Constructs a cipher suites list from varargs.
	 *
	 * @param cipherSuites the cipher suites to include
	 */
	public CipherSuites(final CipherSuite... cipherSuites) {
		this(List.of(cipherSuites));
	}

	/**
	 * Returns the binary representation of the cipher suites list.
	 *
	 * @return byte array containing the size followed by all cipher suites
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(size.toByteArray());
		for (CipherSuite cypherSuite : suites) {
			buffer.put(cypherSuite.toByteArray());
		}
		return buffer.array();
	}

	/**
	 * Parses cipher suites from an input stream.
	 *
	 * @param is the input stream containing the cipher suites data
	 * @return the parsed cipher suites object
	 * @throws IOException if an I/O error occurs
	 */
	public static CipherSuites from(final InputStream is) throws IOException {
		UInt16 size = UInt16.from(is);
		List<CipherSuite> cipherSuites = new ArrayList<>();
		for (int i = 0; i < size.getValue() / CipherSuite.BYTES; ++i) {
			UInt16 int16 = UInt16.from(is);
			CipherSuite cipherSuite = CipherSuite.fromValue(int16.getValue());
			cipherSuites.add(cipherSuite);
		}

		return new CipherSuites(size, cipherSuites);
	}

	/**
	 * Returns a JSON representation of the cipher suites.
	 *
	 * @return JSON string containing the cipher suites information
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size of the cipher suites when serialized.
	 *
	 * @return size in bytes (2 bytes for length + 2 bytes per cipher suite)
	 */
	@Override
	public int sizeOf() {
		return size.sizeOf() + ByteSizeable.sizeOf(suites, CipherSuite.BYTES);
	}

	/**
	 * Returns the size in bytes of the cipher suites list.
	 *
	 * @return the UInt16 wrapper containing the size
	 */
	public UInt16 getSize() {
		return size;
	}

	/**
	 * Returns the list of cipher suites.
	 *
	 * @return unmodifiable list of cipher suites
	 */
	public List<CipherSuite> getSuites() {
		return suites;
	}
}
