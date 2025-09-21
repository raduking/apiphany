package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.BytesWrapper;
import org.apiphany.lang.Bytes;

/**
 * Represents encrypted data in TLS protocol messages.
 * <p>
 * This class encapsulates encrypted payloads used in TLS, including the nonce/IV and the actual encrypted data.
 *
 * @author Radu Sebastian LAZIN
 */
public class Encrypted implements TLSObject {

	/**
	 * The encrypted data, for AEAD the bytes in the beginning can be nonce or initialization vector used for encryption.
	 */
	private final BytesWrapper data;

	/**
	 * Constructs an Encrypted message with wrapped data.
	 *
	 * @param data the encrypted data wrapper
	 */
	public Encrypted(final BytesWrapper data) {
		this.data = data;
	}

	/**
	 * Constructs an Encrypted message with raw byte arrays.
	 *
	 * @param data the encrypted data wrapper
	 */
	public Encrypted(final byte[] data) {
		this(new BytesWrapper(data));
	}

	/**
	 * Constructs an Encrypted message with raw byte arrays.
	 *
	 * @param nonce the nonce/IV bytes
	 * @param encryptedData the encrypted data bytes
	 */
	public Encrypted(final byte[] nonce, final byte[] encryptedData) {
		this(Bytes.concatenate(nonce, encryptedData));
	}

	/**
	 * Parses an Encrypted message from an input stream.
	 *
	 * @param is the input stream containing the encrypted data
	 * @param totalLength the total length of encrypted message
	 * @return the parsed Encrypted object
	 * @throws IOException if an I/O error occurs
	 */
	public static Encrypted from(final InputStream is, final int totalLength) throws IOException {
		BytesWrapper data = BytesWrapper.from(is, totalLength);
		return new Encrypted(data);
	}

	/**
	 * Returns the binary representation of this Encrypted message.
	 *
	 * @return byte array containing nonce and encrypted data
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(data.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this Encrypted message.
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
	 * @return size in bytes of encrypted data
	 */
	@Override
	public int sizeOf() {
		return data.sizeOf();
	}

	/**
	 * Returns the encrypted data wrapper.
	 *
	 * @return the encrypted data wrapper
	 */
	public BytesWrapper getData() {
		return data;
	}

	/**
	 * Returns the nonce/initialization vector.
	 *
	 * @param cipher the bulk cipher
	 * @return the BytesWrapper containing nonce bytes
	 */
	public BytesWrapper getNonce(final BulkCipher cipher) {
		return switch (cipher.type()) {
			case AEAD -> getData().slice(0, cipher.explicitNonceLength());
			case BLOCK, STREAM, NO_ENCRYPTION -> BytesWrapper.empty();
		};
	}

	/**
	 * Returns the encrypted data.
	 *
	 * @param cipher the bulk cipher
	 * @return the BytesWrapper containing encrypted bytes
	 */
	public BytesWrapper getEncryptedData(final BulkCipher cipher) {
		return switch (cipher.type()) {
			case AEAD -> {
				int nonceLength = cipher.explicitNonceLength();
				yield getData().slice(nonceLength, sizeOf() - nonceLength);
			}
			case BLOCK, STREAM, NO_ENCRYPTION -> getData();
		};
	}
}
