package org.apiphany.security.tls;

import org.apiphany.io.BinaryRepresentable;
import org.apiphany.io.ByteSizeable;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Hex;
import org.apiphany.lang.Strings;
import org.apiphany.logging.LoggingFormat;

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
	 * The logging format for TLS objects, configurable via system property {@code apiphany.logging.format.tls}. This
	 * property only supports {@link LoggingFormat#HEX} and {@link LoggingFormat#JSON} formats, defaulting to
	 * {@link LoggingFormat#JSON} if an unsupported value is provided.
	 */
	LoggingFormat FORMAT = LoggingFormat.fromString(System.getProperty("apiphany.logging.format.tls", LoggingFormat.JSON.name()));

	/**
	 * Serializes the TLS object according to the configured format.
	 *
	 * @param tlsObject the TLS object to serialize
	 * @return the serialized representation as either HEX dump or JSON string
	 * @throws NullPointerException if tlsObject is null
	 */
	static String serialize(final TLSObject tlsObject) {
		return serialize(tlsObject, FORMAT);
	}

	/**
	 * Serializes the TLS object according to the given format.
	 *
	 * @param tlsObject the TLS object to serialize
	 * @param loggingFormat the logging format
	 * @return the serialized representation as either HEX dump or JSON string
	 * @throws NullPointerException if tlsObject is null
	 */
	static String serialize(final TLSObject tlsObject, final LoggingFormat loggingFormat) {
		return switch (loggingFormat) {
			case HEX -> Strings.EOL + Hex.dump(tlsObject);
			default -> JsonBuilder.toJson(tlsObject);
		};
	}
}
