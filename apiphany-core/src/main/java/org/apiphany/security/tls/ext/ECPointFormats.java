package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.ByteSizeable;
import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.apiphany.security.tls.TLSExtension;
import org.apiphany.security.tls.TLSObject;

/**
 * Represents the Elliptic Curve Point Formats extension in TLS.
 * <p>
 * This extension allows clients and servers to negotiate the format used for elliptic curve points during key exchange.
 * The most common format is uncompressed (0).
 *
 * @see <a href="https://tools.ietf.org/html/rfc4492#section-5.1.2">RFC 4492 - EC Point Formats</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class ECPointFormats implements TLSExtension {

	/**
	 * The extension type {@link ExtensionType#EC_POINTS_FORMAT}.
	 */
	private final ExtensionType type;

	/**
	 * The total length of the extension data.
	 */
	private final UInt16 length;

	/**
	 * The number of point formats in the list.
	 */
	private final UInt8 formatsSize;

	/**
	 * The list of supported point formats.
	 */
	private final List<UInt8> formats;

	/**
	 * Constructs an {@link ECPointFormats} extension with all fields specified.
	 *
	 * @param type the extension type (should be EC_POINTS_FORMAT)
	 * @param length the total extension data length
	 * @param formatsSize the number of point formats
	 * @param formats the list of supported point formats
	 */
	public ECPointFormats(final ExtensionType type, final UInt16 length, final UInt8 formatsSize, final List<UInt8> formats) {
		this.type = type;
		this.length = length;
		this.formatsSize = formatsSize;
		this.formats = formats;
	}

	/**
	 * Constructs a default {@link ECPointFormats} extension with uncompressed (0) format.
	 */
	public ECPointFormats() {
		this(ExtensionType.EC_POINTS_FORMAT, UInt16.of((short) 0x0002), UInt8.of((byte) 0x01), List.of(UInt8.ZERO));
	}

	/**
	 * Parses an {@link ECPointFormats} extension from an input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @return the parsed ECPointFormats object
	 * @throws IOException if an I/O error occurs
	 */
	public static ECPointFormats from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	/**
	 * Parses an {@link ECPointFormats} extension with known extension type.
	 *
	 * @param is the input stream containing the extension data
	 * @param type the expected extension type
	 * @return the parsed ECPointFormats object
	 * @throws IOException if an I/O error occurs
	 */
	public static ECPointFormats from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);
		UInt8 listSize = UInt8.from(is);
		List<UInt8> formats = new ArrayList<>();
		for (int i = 0; i < listSize.getValue(); ++i) {
			UInt8 format = UInt8.from(is);
			formats.add(format);
		}

		return new ECPointFormats(type, length, listSize, formats);
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
		buffer.put(formatsSize.toByteArray());
		for (UInt8 format : formats) {
			buffer.put(format.toByteArray());
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
		return TLSObject.serialize(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of all fields combined
	 */
	@Override
	public int sizeOf() {
		return type.sizeOf() + length.sizeOf() + formatsSize.sizeOf() + ByteSizeable.sizeOf(formats, UInt8.BYTES);
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
	 * Returns the number of point formats.
	 *
	 * @return the UInt8 wrapper containing the formats count
	 */
	public UInt8 getFormatsSize() {
		return formatsSize;
	}

	/**
	 * Returns the list of supported point formats.
	 *
	 * @return list of point format codes (0 = uncompressed)
	 */
	public List<UInt8> getFormats() {
		return formats;
	}
}
