package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.apiphany.io.BytesWrapper;

/**
 * Represents the Finished message in TLS handshake protocol.
 * <p>
 * This is the final message in the TLS handshake, containing verify data that proves the integrity of the handshake
 * process.
 * <p>
 * This class can be used for both client and server Finished messages since their structure is identical.
 *
 * @author Radu Sebastian LAZIN
 */
public class Finished implements TLSHandshakeBody {

	/**
	 * The verify data containing the handshake hash.
	 */
	private final BytesWrapper verifyData;

	/**
	 * Constructs a Finished message with wrapped verify data.
	 *
	 * @param verifyData the wrapped verification data
	 */
	public Finished(final BytesWrapper verifyData) {
		this.verifyData = verifyData;
	}

	/**
	 * Constructs a Finished message with raw verify data.
	 *
	 * @param payload the raw verification data bytes
	 */
	public Finished(final byte[] payload) {
		this(new BytesWrapper(payload));
	}

	/**
	 * Parses a Finished message from an input stream.
	 *
	 * @param is the input stream containing verify data
	 * @param length the length of verify data to read
	 * @return the parsed Finished object
	 * @throws IOException if an I/O error occurs
	 */
	public static Finished from(final InputStream is, final int length) throws IOException {
		BytesWrapper payload = BytesWrapper.from(is, length);
		return new Finished(payload);
	}

	/**
	 * Returns the binary representation of this Finished message.
	 *
	 * @return byte array containing verify data
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(verifyData.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this Finished message.
	 *
	 * @return JSON string containing verify data
	 */
	@Override
	public String toString() {
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of verify data
	 */
	@Override
	public int sizeOf() {
		return verifyData.sizeOf();
	}

	/**
	 * Compares this {@link Finished} message to another object for equality.
	 *
	 * @param obj the object to compare with
	 * @return true if both objects are {@link Finished} messages with the same verify data, false otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Finished that) {
			return Objects.equals(this.verifyData, that.verifyData);
		}
		return false;
	}

	/**
	 * Returns the hash code for this {@link Finished} message.
	 *
	 * @return hash code based on verify data
	 */
	@Override
	public int hashCode() {
		return Objects.hash(verifyData);
	}

	/**
	 * Returns the handshake message type.
	 *
	 * @return always returns FINISHED
	 */
	@Override
	public HandshakeType getType() {
		return HandshakeType.FINISHED;
	}

	/**
	 * Returns the verify data.
	 *
	 * @return the BytesWrapper containing verification data
	 */
	public BytesWrapper getVerifyData() {
		return verifyData;
	}
}
