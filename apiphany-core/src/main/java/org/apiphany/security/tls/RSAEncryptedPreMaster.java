package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.apiphany.io.BytesWrapper;
import org.apiphany.io.UInt16;

/**
 * Represents the RSA-encrypted premaster secret in TLS key exchange.
 * <p>
 * This class encapsulates the encrypted premaster secret that is sent from the client to the server during RSA-based
 * key exchange in the TLS handshake.
 *
 * @author Radu Sebastian LAZIN
 */
public class RSAEncryptedPreMaster implements TLSKeyExchange {

	/**
	 * The length of the encrypted premaster secret in bytes.
	 */
	private final UInt16 length;

	/**
	 * The encrypted premaster secret bytes.
	 */
	private final BytesWrapper bytes;

	/**
	 * Constructs an RSAEncryptedPreMaster with length wrapper and bytes wrapper.
	 *
	 * @param length the length of encrypted data
	 * @param bytes the wrapped encrypted bytes
	 */
	public RSAEncryptedPreMaster(final UInt16 length, final BytesWrapper bytes) {
		this.length = length;
		this.bytes = bytes;
	}

	/**
	 * Constructs an RSAEncryptedPreMaster with primitive length and raw bytes.
	 *
	 * @param length the length of encrypted data
	 * @param bytes the raw encrypted bytes
	 */
	public RSAEncryptedPreMaster(final short length, final byte[] bytes) {
		this(UInt16.of(length), new BytesWrapper(bytes));
	}

	/**
	 * Constructs an RSAEncryptedPreMaster with raw bytes (auto-calculates length).
	 *
	 * @param bytes the raw encrypted bytes
	 */
	public RSAEncryptedPreMaster(final byte[] bytes) {
		this((short) bytes.length, bytes);
	}

	/**
	 * Parses an RSAEncryptedPreMaster from an input stream.
	 *
	 * @param is the input stream containing the encrypted data
	 * @return the parsed RSAEncryptedPreMaster object
	 * @throws IOException if an I/O error occurs
	 */
	public static RSAEncryptedPreMaster from(final InputStream is) throws IOException {
		UInt16 length = UInt16.from(is);
		BytesWrapper bytes = BytesWrapper.from(is, length.getValue());
		return new RSAEncryptedPreMaster(length, bytes);
	}

	/**
	 * Returns the binary representation of this encrypted premaster secret.
	 *
	 * @return byte array containing length and encrypted data
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(length.toByteArray());
		buffer.put(bytes.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this encrypted premaster secret.
	 *
	 * @return JSON string containing encrypted data information
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of length field plus encrypted data
	 */
	@Override
	public int sizeOf() {
		return length.sizeOf() + bytes.sizeOf();
	}

	/**
	 * Compares this RSAEncryptedPreMaster to another object for equality.
	 *
	 * @param obj the object to compare with
	 * @return true if both objects are equal, false otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof RSAEncryptedPreMaster that) {
			return Objects.equals(this.length, that.length)
					&& Objects.equals(this.bytes, that.bytes);
		}
		return false;
	}

	/**
	 * Returns the hash code based on length and bytes.
	 *
	 * @return hash code of the object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(length, bytes);
	}

	/**
	 * Returns the length of encrypted data.
	 *
	 * @return the UInt16 wrapper containing length
	 */
	public UInt16 getLength() {
		return length;
	}

	/**
	 * Returns the encrypted premaster secret.
	 *
	 * @return the BytesWrapper containing encrypted bytes
	 */
	public BytesWrapper getBytes() {
		return bytes;
	}
}
