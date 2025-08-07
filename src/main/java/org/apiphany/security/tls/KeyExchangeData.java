package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.io.BytesWrapper;

/**
 * Represents key exchange data in TLS handshake messages.
 * <p>
 * This class encapsulates the raw key exchange information sent during the TLS handshake, which varies depending on the
 * key exchange algorithm.
 *
 * @author Radu Sebastian LAZIN
 */
public class KeyExchangeData implements TLSKeyExchange {

	/**
	 * The raw key exchange data bytes.
	 */
	private final BytesWrapper bytes;

	/**
	 * Constructs a KeyExchangeData with wrapped bytes.
	 *
	 * @param bytes the wrapped key exchange data
	 */
	public KeyExchangeData(final BytesWrapper bytes) {
		this.bytes = bytes;
	}

	/**
	 * Constructs a KeyExchangeData with raw bytes.
	 *
	 * @param bytes the raw key exchange data
	 */
	public KeyExchangeData(final byte[] bytes) {
		this(new BytesWrapper(bytes));
	}

	/**
	 * Parses KeyExchangeData from an input stream.
	 *
	 * @param is the input stream containing key exchange data
	 * @param length the number of bytes to read
	 * @return the parsed KeyExchangeData object
	 * @throws IOException if an I/O error occurs
	 */
	public static KeyExchangeData from(final InputStream is, final int length) throws IOException {
		BytesWrapper bytes = BytesWrapper.from(is, length);
		return new KeyExchangeData(bytes);
	}

	/**
	 * Returns the binary representation of this key exchange data.
	 *
	 * @return byte array containing the raw key exchange data
	 */
	@Override
	public byte[] toByteArray() {
		return bytes.toByteArray();
	}

	/**
	 * Returns a JSON representation of this key exchange data.
	 *
	 * @return JSON string containing the key exchange information
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the key exchange data.
	 *
	 * @return the BytesWrapper containing key exchange bytes
	 */
	public BytesWrapper getBytes() {
		return bytes;
	}

	/**
	 * Returns the size of the key exchange data.
	 *
	 * @return size in bytes of the key exchange data
	 */
	@Override
	public int sizeOf() {
		return bytes.sizeOf();
	}
}
