package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt16;
import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;

/**
 * Represents elliptic curve information in TLS key exchange.
 * <p>
 * This class combines a curve type with a named curve identifier, used during ECDHE key exchange in TLS handshake.
 *
 * @author Radu Sebastian LAZIN
 */
public class CurveInfo implements TLSObject {

	/**
	 * The type of elliptic curve being specified.
	 */
	private final CurveType type;

	/**
	 * The specific named curve identifier.
	 */
	private final NamedCurve name;

	/**
	 * Constructs a CurveInfo with specified type and name.
	 *
	 * @param type the curve type
	 * @param name the named curve identifier
	 */
	public CurveInfo(final CurveType type, final NamedCurve name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * Parses a CurveInfo from an input stream.
	 *
	 * @param is the input stream containing curve data
	 * @return the parsed CurveInfo object
	 * @throws IOException if an I/O error occurs
	 */
	public static CurveInfo from(final InputStream is) throws IOException {
		UInt8 int8 = UInt8.from(is);
		CurveType type = CurveType.fromValue(int8.getValue());

		UInt16 int16 = UInt16.from(is);
		NamedCurve name = NamedCurve.fromValue(int16.getValue());

		return new CurveInfo(type, name);
	}

	/**
	 * Returns the binary representation of this CurveInfo.
	 *
	 * @return byte array containing type and name
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(name.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns a JSON representation of this CurveInfo.
	 *
	 * @return JSON string containing curve information
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return size in bytes of type plus name
	 */
	@Override
	public int sizeOf() {
		return type.sizeOf() + name.sizeOf();
	}

	/**
	 * Returns the curve type.
	 *
	 * @return the CurveType enum value
	 */
	public CurveType getType() {
		return type;
	}

	/**
	 * Returns the named curve.
	 *
	 * @return the NamedCurve enum value
	 */
	public NamedCurve getName() {
		return name;
	}
}
