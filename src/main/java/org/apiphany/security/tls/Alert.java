package org.apiphany.security.tls;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;

/**
 * Represents a TLS Alert message used for error notification and connection termination.
 * <p>
 * Alert messages convey the severity level and specific description of any issues encountered during the TLS
 * communication.
 *
 * @author Radu Sebastian LAZIN
 */
public class Alert implements TLSObject {

	/**
	 * The severity level of the alert.
	 */
	private final AlertLevel level;

	/**
	 * The specific alert description.
	 */
	private final AlertDescription description;

	/**
	 * Constructs an Alert with level and description enums.
	 *
	 * @param level the alert severity level
	 * @param description the alert description
	 */
	public Alert(final AlertLevel level, final AlertDescription description) {
		this.level = level;
		this.description = description;
	}

	/**
	 * Constructs an Alert with raw byte values.
	 *
	 * @param level the alert level byte value
	 * @param code the alert description byte value
	 */
	public Alert(final byte level, final byte code) {
		this(AlertLevel.fromValue(level), AlertDescription.fromCode(code));
	}

	/**
	 * Parses an Alert from an input stream.
	 *
	 * @param is the input stream containing alert data
	 * @return the parsed Alert object
	 * @throws IOException if an I/O error occurs
	 */
	public static Alert from(final InputStream is) throws IOException {
		UInt8 int81 = UInt8.from(is);
		UInt8 int82 = UInt8.from(is);
		return new Alert(int81.getValue(), int82.getValue());
	}

	/**
	 * Returns the binary representation of this Alert.
	 *
	 * @return 2-byte array containing level and description
	 */
	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(level.toByteArray());
		buffer.put(description.toByteArray());
		return buffer.array();
	}

	/**
	 * Returns the total size when serialized.
	 *
	 * @return always returns 2 (1 byte for level + 1 byte for description)
	 */
	@Override
	public int sizeOf() {
		return level.sizeOf() + description.sizeOf();
	}

	/**
	 * Returns a JSON representation of this Alert.
	 *
	 * @return JSON string containing alert information
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	/**
	 * Returns the alert level.
	 *
	 * @return the AlertLevel enum value
	 */
	public AlertLevel getLevel() {
		return level;
	}

	/**
	 * Returns the alert description as a string.
	 *
	 * @return string representation of the alert description
	 */
	public String getDescription() {
		return description.toString();
	}
}
