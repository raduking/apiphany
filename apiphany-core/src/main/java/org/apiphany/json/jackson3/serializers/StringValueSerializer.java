package org.apiphany.json.jackson3.serializers;

import java.util.function.Function;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Generic serializer that writes a value as a JSON string using the given conversion function.
 *
 * @param <T> the type of the value being serialized
 *
 * @author Radu Sebastian LAZIN
 */
public class StringValueSerializer<T> extends ValueSerializer<T> {

	/**
	 * Function used to convert the value to its JSON string representation.
	 */
	private final Function<T, String> toString;

	/**
	 * Constructs a new serializer.
	 *
	 * @param toString function used to convert the value to its JSON string representation
	 */
	public StringValueSerializer(final Function<T, String> toString) {
		this.toString = toString;
	}

	/**
	 * @see ValueSerializer#serialize(Object, JsonGenerator, SerializationContext)
	 */
	@Override
	@SuppressWarnings("resource")
	public void serialize(final T value, final JsonGenerator gen, final SerializationContext ctxt) throws JacksonException {
		gen.writeString(toString.apply(value));
	}
}
