package org.apiphany.security.tls;

/**
 * Base abstract class for all objects that contain an {@link Encrypted} object in TLS protocol communication.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class TLSEncryptedObject implements TLSObject {

	/**
	 * The encrypted payload data.
	 */
	private final Encrypted encrypted;

	/**
	 * Constructs an encrypted object with encrypted payload.
	 *
	 * @param data the encrypted data payload
	 */
	protected TLSEncryptedObject(final Encrypted data) {
		this.encrypted = data;
	}

	/**
	 * Returns the binary representation of this encrypted data.
	 *
	 * @return byte array containing the encrypted payload
	 */
	@Override
	public byte[] toByteArray() {
		return encrypted.toByteArray();
	}

	/**
	 * Returns a JSON representation of this encrypted object.
	 *
	 * @return JSON string containing metadata about the encrypted data
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the size of the encrypted data.
	 *
	 * @return size in bytes of the encrypted payload
	 */
	@Override
	public int sizeOf() {
		return encrypted.sizeOf();
	}

	/**
	 * Returns the encrypted data payload.
	 *
	 * @return the Encrypted data wrapper
	 */
	public Encrypted getEncrypted() {
		return encrypted;
	}
}
