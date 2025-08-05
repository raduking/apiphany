package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.ByteSizeable;
import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;

/**
 * Represents the compression methods supported in a TLS handshake.
 * <p>
 * This class manages the list of compression algorithms that a client or server supports for TLS connections. In modern
 * TLS implementations, the only supported method is typically {@link CompressionMethod#NO_COMPRESSION} due to security
 * concerns with compression.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-7.4.1.2">RFC 5246 - Compression Methods</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class CompressionMethods implements TLSObject {

	/**
	 * The number of bytes needed to represent the size field (always 1 byte).
	 */
	private final UInt8 size;

	/**
	 * The list of supported compression methods.
	 */
	private final List<CompressionMethod> methods;

	/**
	 * Constructs a compression methods list with explicit size and methods.
	 *
	 * @param size the number of compression methods in the list
	 * @param methods the compression methods to include
	 */
	public CompressionMethods(final UInt8 size, final List<CompressionMethod> methods) {
		this.size = size;
		this.methods = new ArrayList<>(this.size.getValue());
		this.methods.addAll(methods);
	}

	/**
	 * Constructs a compression methods list calculating size automatically.
	 *
	 * @param methods the compression methods to include
	 */
	public CompressionMethods(final List<CompressionMethod> methods) {
		this(UInt8.of((byte) methods.size()), methods);
	}

	/**
	 * Constructs a compression methods list with only NO_COMPRESSION.
	 */
	public CompressionMethods() {
		this(List.of(CompressionMethod.NO_COMPRESSION));
	}

	/**
	 * Returns the binary representation of the compression methods.
	 *
	 * @return byte array containing the size followed by method bytes
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(size.toByteArray());
		for (CompressionMethod method : methods) {
			buffer.put(method.toByteArray());
		}
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of the compression methods.
	 *
	 * @return JSON string containing the compression methods information
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Parses compression methods from an input stream.
	 *
	 * @param is the input stream containing the compression methods data
	 * @return the parsed compression methods object
	 * @throws IOException if an I/O error occurs
	 */
	public static CompressionMethods from(final InputStream is) throws IOException {
		UInt8 size = UInt8.from(is);
		List<CompressionMethod> methods = new ArrayList<>();
		for (int i = 0; i < size.getValue(); ++i) {
			UInt8 int8 = UInt8.from(is);
			CompressionMethod method = CompressionMethod.fromValue(int8.getValue());
			methods.add(method);
		}

		return new CompressionMethods(size, methods);
	}

	/**
	 * Returns the size in bytes of the compression methods list.
	 *
	 * @return the UInt8 wrapper containing the size
	 */
	public UInt8 getSize() {
		return size;
	}

	/**
	 * Returns the list of compression methods.
	 *
	 * @return unmodifiable list of compression methods
	 */
	public List<CompressionMethod> getMethods() {
		return methods;
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes (1 byte for length + 1 byte per method)
	 */
	@Override
	public int sizeOf() {
		return size.sizeOf() + ByteSizeable.sizeOf(methods, CompressionMethod.BYTES);
	}
}
