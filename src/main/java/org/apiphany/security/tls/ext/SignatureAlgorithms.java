package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.UInt16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;
import org.apiphany.security.tls.SignatureAlgorithm;
import org.apiphany.security.tls.TLSExtension;

/**
 * Represents the Signature Algorithms extension in TLS.
 * <p>
 * This extension allows clients and servers to negotiate which signature algorithms will be used for digital signatures
 * during the TLS handshake.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-7.4.1.4.1">RFC 5246 - Signature Algorithms</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class SignatureAlgorithms implements TLSExtension {

	/**
	 * The extension type {@link ExtensionType#SIGNATURE_ALGORITHMS}.
	 */
	private final ExtensionType type;

	/**
	 * The total length of the extension data.
	 */
	private final UInt16 length;

	/**
	 * The size of the algorithms list in bytes.
	 */
	private final UInt16 algorithmsSize;

	/**
	 * The list of supported signature algorithms.
	 */
	private final List<SignatureAlgorithm> algorithms;

	/**
	 * Constructs a SignatureAlgorithms extension with all fields specified.
	 *
	 * @param type the extension type (should be SIGNATURE_ALGORITHMS)
	 * @param length the total extension data length
	 * @param algorithmsSize the size of the algorithms list
	 * @param algorithms the list of supported signature algorithms
	 */
	public SignatureAlgorithms(final ExtensionType type, final UInt16 length, final UInt16 algorithmsSize,
			final List<SignatureAlgorithm> algorithms) {
		this.type = type;
		this.length = length;
		this.algorithmsSize = algorithmsSize;
		this.algorithms = algorithms;
	}

	/**
	 * Constructs a SignatureAlgorithms extension from a list of algorithms.
	 *
	 * @param algorithms the list of supported signature algorithms
	 */
	public SignatureAlgorithms(final List<SignatureAlgorithm> algorithms) {
		this(
				ExtensionType.SIGNATURE_ALGORITHMS,
				UInt16.of((short) (algorithms.size() * SignatureAlgorithm.BYTES + UInt16.BYTES)),
				UInt16.of((short) (algorithms.size() * SignatureAlgorithm.BYTES)),
				algorithms);
	}

	/**
	 * Constructs a SignatureAlgorithms extension from varargs.
	 *
	 * @param algorithms the signature algorithms to include
	 */
	public SignatureAlgorithms(final SignatureAlgorithm... algorithms) {
		this(List.of(algorithms));
	}

	/**
	 * Constructs a SignatureAlgorithms extension with strong algorithms by default.
	 */
	public SignatureAlgorithms() {
		this(SignatureAlgorithm.STRONG_ALGORITHMS);
	}

	/**
	 * Parses a SignatureAlgorithms extension from an input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @return the parsed SignatureAlgorithms object
	 * @throws IOException if an I/O error occurs
	 */
	public static SignatureAlgorithms from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	/**
	 * Parses a SignatureAlgorithms extension with known extension type.
	 *
	 * @param is the input stream containing the extension data
	 * @param type the expected extension type
	 * @return the parsed SignatureAlgorithms object
	 * @throws IOException if an I/O error occurs
	 */
	public static SignatureAlgorithms from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);
		UInt16 algorithmsSize = UInt16.from(is);
		List<SignatureAlgorithm> algorithms = new ArrayList<>();
		for (int i = 0; i < algorithmsSize.getValue() / SignatureAlgorithm.BYTES; ++i) {
			UInt16 int16 = UInt16.from(is);
			SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.fromValue(int16.getValue());
			algorithms.add(signatureAlgorithm);
		}

		return new SignatureAlgorithms(type, length, algorithmsSize, algorithms);
	}

	/**
	 * Returns the binary representation of this extension.
	 *
	 * @return byte array containing all extension fields
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(algorithmsSize.toByteArray());
		for (SignatureAlgorithm algorithm : algorithms) {
			buffer.put(algorithm.toByteArray());
		}
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this extension.
	 *
	 * @return JSON string containing the extension data
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of all fields combined
	 */
	@Override
	public int sizeOf() {
		return type.sizeOf() + length.sizeOf() + algorithmsSize.sizeOf() + ByteSizeable.sizeOf(algorithms, SignatureAlgorithm.BYTES);
	}

	/**
	 * Returns the extension type.
	 *
	 * @return the ExtensionType enum value
	 */
	@Override
	public ExtensionType getType() {
		return type;
	}

	/**
	 * Returns the extension data length.
	 *
	 * @return the UInt16 wrapper containing the length
	 */
	public UInt16 getLength() {
		return length;
	}

	/**
	 * Returns the size of the algorithms list.
	 *
	 * @return the UInt16 wrapper containing the list size
	 */
	public UInt16 getAlgorithmsSize() {
		return algorithmsSize;
	}

	/**
	 * Returns the list of supported signature algorithms.
	 *
	 * @return list of SignatureAlgorithm enum values
	 */
	public List<SignatureAlgorithm> getAlgorithms() {
		return algorithms;
	}
}
