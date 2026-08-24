package org.apiphany.json;

import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.morphix.reflection.Constructors;

/**
 * Utility methods for JSON object serialization.
 *
 * @author Radu Sebastian LAZIN
 */
public final class JsonObjects {

	/**
	 * Private constructor to prevent instantiation.
	 *
	 * @throws UnsupportedOperationException if called via reflection
	 */
	private JsonObjects() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Returns {@code true} if the given value should be serialized as a structured JSON object (preserving shape),
	 * {@code false} if it should be serialized as a scalar value (string, number, boolean, etc.).
	 *
	 * @param value the value to check
	 * @return {@code true} if the value is a complex object
	 */
	public static boolean isStructured(final Object value) {
		return switch (value) {
			case CharSequence ignored -> false;
			case Number ignored -> false;
			case Boolean ignored -> false;
			case Character ignored -> false;
			case Enum<?> ignored -> false;
			case TemporalAccessor ignored -> false;
			case Date ignored -> false;
			case Calendar ignored -> false;
			case UUID ignored -> false;
			default -> true;
		};
	}
}
