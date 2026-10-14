package org.apiphany.json.jackson2.serializers;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Generic serializer that writes a value as a JSON string using the given conversion function.
 *
 * @param <T> the type of the value being serialized
 *
 * @author Radu Sebastian LAZIN
 */
public class FieldValueSerializer<T> extends JsonSerializer<T> {

	/**
	 * Function used to convert the value to its JSON string representation.
	 */
	private final Function<T, String> toString;

	/**
	 * Constructs a new serializer.
	 *
	 * @param toString function used to convert the value to its JSON string representation
	 */
	public FieldValueSerializer(final Function<T, String> toString) {
		this.toString = toString;
	}

	/**
	 * @see JsonSerializer#serialize(Object, JsonGenerator, SerializerProvider)
	 */
	@Override
	public void serialize(final T value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
		gen.writeString(toString.apply(value));
	}
}
