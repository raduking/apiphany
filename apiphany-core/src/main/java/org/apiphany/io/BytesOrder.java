package org.apiphany.io;

import java.nio.ByteOrder;
import java.util.Map;

import org.morphix.lang.Enums;

/**
 * Similar to {@link ByteOrder} but it is actually an enum.
 *
 * @author Radu Sebastian LAZIN
 */
public enum BytesOrder {

	/**
	 * Constant denoting big-endian byte order. In this order, the bytes of a multibyte value are ordered from most
	 * significant to least significant.
	 */
	LITTLE_ENDIAN,

	/**
	 * Constant denoting little-endian byte order. In this order, the bytes of a multibyte value are ordered from least
	 * significant to most significant.
	 */
	BIG_ENDIAN;

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, BytesOrder> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * Returns a {@link BytesOrder} enum from a {@link String}.
	 *
	 * @param bytesOrder bytes order as string
	 * @return a bytes order enum
	 */
	public static BytesOrder fromString(final String bytesOrder) {
		return Enums.fromString(bytesOrder, NAME_MAP, values());
	}
}
