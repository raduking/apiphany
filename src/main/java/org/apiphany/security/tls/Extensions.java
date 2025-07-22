package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.UInt16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;
import org.apiphany.security.tls.ext.ECPointFormats;
import org.apiphany.security.tls.ext.ExtensionType;
import org.apiphany.security.tls.ext.RenegotiationInfo;
import org.apiphany.security.tls.ext.ServerNames;
import org.apiphany.security.tls.ext.SignatureAlgorithms;
import org.apiphany.security.tls.ext.SignedCertificateTimestamp;
import org.apiphany.security.tls.ext.StatusRequest;
import org.apiphany.security.tls.ext.SupportedGroups;

/**
 * Represents a collection of TLS extensions included in handshake messages.
 * <p>
 * This class manages the list of extensions that a client or server supports for TLS connections. Extensions provide
 * additional functionality and negotiation capabilities beyond the base TLS protocol.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-7.4.1.4">RFC 5246 - Extensions</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class Extensions implements TLSObject {

	/**
	 * The total length in bytes of all extensions when serialized.
	 */
	private final UInt16 length;

	/**
	 * The list of TLS extensions contained in this collection.
	 */
	private final List<TLSExtension> list = new ArrayList<>();

	/**
	 * Constructs an extensions list with optional length updating.
	 *
	 * @param length the initial length value
	 * @param extensions the list of extensions to include
	 * @param updateSize if true, the length will be recalculated from extensions
	 */
	public Extensions(final UInt16 length, final List<TLSExtension> extensions, final boolean updateSize) {
		this.length = updateSize ? UInt16.of((short) ByteSizeable.sizeOf(extensions)) : length;
		this.list.addAll(extensions);
	}

	/**
	 * Constructs an extensions list with automatic length calculation.
	 *
	 * @param length the initial length value
	 * @param extensions the list of extensions to include
	 */
	public Extensions(
			final UInt16 length,
			final List<TLSExtension> extensions) {
		this(length, extensions, true);
	}

	/**
	 * Constructs a default extensions list with common extensions for client hello.
	 *
	 * @param serverNames the list of supported server names (SNI)
	 * @param namedCurves the list of supported elliptic curves
	 * @param signatureAlgorithms the list of supported signature algorithms
	 */
	public Extensions(final List<String> serverNames, final List<NamedCurve> namedCurves, final List<SignatureAlgorithm> signatureAlgorithms) {
		this(
				UInt16.ZERO,
				List.of(
						new ServerNames(serverNames),
						new StatusRequest(),
						new SupportedGroups(namedCurves),
						new ECPointFormats(),
						new SignatureAlgorithms(signatureAlgorithms),
						new RenegotiationInfo(),
						new SignedCertificateTimestamp()));
	}

	/**
	 * Returns the binary representation of the extensions list.
	 *
	 * @return byte array containing the length followed by all extensions
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(length.toByteArray());
		for (TLSExtension extension : list) {
			buffer.put(extension.toByteArray());
		}
		return buffer.array();
	}

	/**
	 * Parses extensions from an input stream.
	 *
	 * @param is the input stream containing the extensions data
	 * @return the parsed extensions object
	 * @throws IOException if an I/O error occurs
	 */
	public static Extensions from(final InputStream is) throws IOException {
		UInt16 length = UInt16.from(is);

		List<TLSExtension> extensions = new ArrayList<>();
		int currentLength = 0;
		while (currentLength < length.getValue()) {
			UInt16 int16 = UInt16.from(is);
			ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

			TLSExtension extension = extensionType.extensionFrom(is);
			extensions.add(extension);

			currentLength += extension.sizeOf();
		}

		return new Extensions(length, extensions, false);
	}

	/**
	 * Returns a JSON representation of the extensions.
	 *
	 * @return JSON string containing the extensions information
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes (2 bytes for length + size of all extensions)
	 */
	@Override
	public int sizeOf() {
		return length.sizeOf() + ByteSizeable.sizeOf(list);
	}

	/**
	 * Returns the length of all extensions when serialized.
	 *
	 * @return the UInt16 wrapper containing the total length
	 */
	public UInt16 getLength() {
		return length;
	}

	/**
	 * Returns the list of extensions.
	 *
	 * @return unmodifiable list of TLS extensions
	 */
	public List<TLSExtension> getList() {
		return list;
	}
}
