package org.apiphany.io;

import java.nio.ByteOrder;

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

}
