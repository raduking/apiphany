package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import org.apiphany.io.BytesWrapper;
import org.apiphany.lang.annotation.AsValue;

/**
 * Represents the random values exchanged during TLS handshake initialization.
 * <p>
 * This class encapsulates the 32-byte random values used in TLS handshake messages (ClientHello and ServerHello). The
 * random values provide protection against replay attacks and contribute to cryptographic security of the session.
 *
 * <p>
 * Implements {@link TLSObject} for protocol serialization and extends {@link BytesWrapper} for byte array management.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-7.4.1.2">RFC 5246 - Random Values</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class ExchangeRandom extends BytesWrapper implements TLSObject {

	/**
	 * The fixed size in bytes of TLS random values (32 bytes).
	 */
	public static final int BYTES = 32;

	/**
	 * Constructs an {@link ExchangeRandom} with the specified byte array.
	 *
	 * @param random the 32-byte random value (must be exactly {@value #BYTES} bytes)
	 * @throws IllegalArgumentException If the input array length is not {@value #BYTES}
	 */
	public ExchangeRandom(final byte[] random) {
		super(random);
		if (random.length != BYTES) {
			throw new IllegalArgumentException("ExchangeRandom must be exactly " + BYTES + " bytes long");
		}
	}

	/**
	 * Generates a new {@link ExchangeRandom} using the specified SecureRandom.
	 *
	 * @param secureRandom The cryptographically secure random number generator to use
	 */
	public ExchangeRandom(final SecureRandom secureRandom) {
		this(generate(secureRandom, BYTES));
	}

	/**
	 * Generates a new {@link ExchangeRandom} using the system default SecureRandom.
	 */
	public ExchangeRandom() {
		this(new SecureRandom());
	}

	/**
	 * Parses an {@link ExchangeRandom} from an input stream.
	 *
	 * @param is the input stream containing exactly {@value #BYTES} bytes
	 * @return the parsed ExchangeRandom object
	 * @throws IOException If an I/O error occurs or stream ends before reading {@value #BYTES} bytes
	 */
	public static ExchangeRandom from(final InputStream is) throws IOException {
		BytesWrapper binaryData = BytesWrapper.from(is, BYTES);
		return new ExchangeRandom(binaryData.toByteArray());
	}

	/**
	 * Returns the hexadecimal string representation of this random value.
	 *
	 * @return a hex-encoded string of the random bytes
	 */
	@Override
	@AsValue
	public String toString() {
		return super.toString();
	}

	/**
	 * Returns the raw random bytes, alias from {@link #toByteArray()}.
	 *
	 * @return a copy of the 32-byte random array
	 */
	public byte[] getRandom() {
		return toByteArray();
	}

	/**
	 * Generates random bytes using the specified {@link SecureRandom}.
	 *
	 * @param secureRandom the random number generator to use
	 * @param bytes the array to fill with random bytes
	 * @return the filled byte array
	 */
	public static byte[] generate(final SecureRandom secureRandom, final byte[] bytes) {
		secureRandom.nextBytes(bytes);
		return bytes;
	}

	/**
	 * Generates random bytes using the specified {@link SecureRandom}.
	 *
	 * @param secureRandom the random number generator to use
	 * @param size the size of the array to fill with random bytes
	 * @return the filled byte array
	 */
	public static byte[] generate(final SecureRandom secureRandom, final int size) {
		return generate(secureRandom, new byte[size]);
	}

	/**
	 * Generates random bytes using a new {@link SecureRandom}.
	 *
	 * @param size the size of the array to fill with random bytes
	 * @return the filled byte array
	 */
	public static byte[] generate(final int size) {
		return generate(new SecureRandom(), size);
	}
}
