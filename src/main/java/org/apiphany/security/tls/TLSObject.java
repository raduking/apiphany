package org.apiphany.security.tls;

import org.apiphany.io.BinaryRepresentable;
import org.apiphany.io.ByteSizeable;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Hex;
import org.apiphany.lang.LoggingFormat;

/**
 * Marker interface for all objects that participate in TLS protocol communication.
 * <p>
 * Implementing this interface indicates that the object:
 * <ul>
 * <li>Has a well-defined binary representation for use in TLS messages (via {@link BinaryRepresentable})</li>
 * <li>Has a known size when serialized (via {@link ByteSizeable})</li>
 * </ul>
 *
 * @see BinaryRepresentable
 * @see ByteSizeable
 *
 * @author Radu Sebastian LAZIN
 */
public interface TLSObject extends ByteSizeable, BinaryRepresentable {

	/**
	 * The logging format for TLS objects, configurable via system property {@code apiphany.logging.format.tls}.
	 */
	static LoggingFormat FORMAT = LoggingFormat.fromString(System.getProperty("apiphany.logging.format.tls"));

	/**
	 * Serializes the TLS object according to the configured format.
	 *
	 * @param tlsObject the TLS object to serialize
	 * @return the serialized representation as either HEX dump or JSON string
	 * @throws NullPointerException if tlsObject is null
	 */
	static String serialize(final TLSObject tlsObject) {
		return switch (FORMAT) {
		case HEX -> Hex.dump(tlsObject);
		case JSON -> JsonBuilder.toJson(tlsObject);
		};
	}

}
