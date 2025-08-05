package org.apiphany.security.tls.ext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.UInt16;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;
import org.apiphany.security.tls.NamedCurve;
import org.apiphany.security.tls.TLSExtension;

/**
 * Represents the Supported Groups (formerly Elliptic Curves) extension in TLS.
 * <p>
 * This extension allows clients to indicate which named elliptic curves they support for key exchange during the TLS
 * handshake. The server then selects one of these curves for the key exchange.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7919">RFC 7919 - Supported Groups Extension</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class SupportedGroups implements TLSExtension {

	/**
	 * The extension type {@link ExtensionType#SUPPORTED_GROUPS}.
	 */
	private final ExtensionType type;

	/**
	 * The total length of the extension data.
	 */
	private final UInt16 length;

	/**
	 * The size of the groups list in bytes.
	 */
	private final UInt16 groupsSize;

	/**
	 * The list of supported named curves.
	 */
	private final List<NamedCurve> groups;

	/**
	 * Constructs a SupportedGroups extension with all fields specified.
	 *
	 * @param type the extension type (should be SUPPORTED_GROUPS)
	 * @param size the total extension data length
	 * @param groupsSize the size of the groups list
	 * @param groups the list of supported named curves
	 */
	public SupportedGroups(final ExtensionType type, final UInt16 size, final UInt16 groupsSize, final List<NamedCurve> groups) {
		this.type = type;
		this.length = size;
		this.groupsSize = groupsSize;
		this.groups = groups;
	}

	/**
	 * Constructs a SupportedGroups extension from a list of named curves.
	 *
	 * @param namedCurves the list of supported elliptic curves
	 */
	public SupportedGroups(final List<NamedCurve> namedCurves) {
		this(
				ExtensionType.SUPPORTED_GROUPS,
				UInt16.of((short) (namedCurves.size() * NamedCurve.BYTES + UInt16.BYTES)),
				UInt16.of((short) (namedCurves.size() * NamedCurve.BYTES)),
				namedCurves);
	}

	/**
	 * Constructs a SupportedGroups extension with all available named curves.
	 */
	public SupportedGroups() {
		this(List.of(NamedCurve.values()));
	}

	/**
	 * Parses a SupportedGroups extension from an input stream.
	 *
	 * @param is the input stream containing the extension data
	 * @return the parsed SupportedGroups object
	 * @throws IOException if an I/O error occurs
	 */
	public static SupportedGroups from(final InputStream is) throws IOException {
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	/**
	 * Parses a SupportedGroups extension with known extension type.
	 *
	 * @param is the input stream containing the extension data
	 * @param type the expected extension type
	 * @return the parsed SupportedGroups object
	 * @throws IOException if an I/O error occurs
	 */
	public static SupportedGroups from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);
		UInt16 listSize = UInt16.from(is);
		List<NamedCurve> groups = new ArrayList<>();
		for (int i = 0; i < listSize.getValue() / NamedCurve.BYTES; ++i) {
			UInt16 value = UInt16.from(is);
			NamedCurve curveName = NamedCurve.fromValue(value.getValue());
			groups.add(curveName);
		}

		return new SupportedGroups(type, length, listSize, groups);
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
		buffer.put(groupsSize.toByteArray());
		for (NamedCurve group : groups) {
			buffer.put(group.toByteArray());
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
		return type.sizeOf() + length.sizeOf() + groupsSize.sizeOf() + ByteSizeable.sizeOf(groups, NamedCurve.BYTES);
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
	 * Returns the size of the groups list.
	 *
	 * @return the UInt16 wrapper containing the list size
	 */
	public UInt16 getGroupsSize() {
		return groupsSize;
	}

	/**
	 * Returns the list of supported named curves.
	 *
	 * @return list of NamedCurve enum values
	 */
	public List<NamedCurve> getGroups() {
		return groups;
	}
}
