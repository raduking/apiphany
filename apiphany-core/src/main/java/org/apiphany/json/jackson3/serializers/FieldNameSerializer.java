package org.apiphany.json.jackson3.serializers;

import java.util.function.Function;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Generic serializer that writes a value as a JSON field name using the given conversion function. The field name uses
 * the same conversion as the corresponding value representation, so that a value and its use as a {@link java.util.Map}
 * key are encoded identically.
 * <p>
 * It is meant to be registered as a key serializer on a module, for example:
 *
 * <pre>
 * module.addKeySerializer(Locale.class, new FieldNameSerializer&lt;&gt;(Locale::toLanguageTag));
 * </pre>
 *
 * With that registration a {@code Map<Locale, String>} is written with BCP-47 language tags as field names, for example
 * {@code {"en-US":"value"}}. For the corresponding value representation see {@link FieldValueSerializer}.
 *
 * @param <T> the type of the value being serialized
 *
 * @author Radu Sebastian LAZIN
 */
public class FieldNameSerializer<T> extends ValueSerializer<T> {

	/**
	 * Function used to convert the value to its JSON string representation.
	 */
	private final Function<T, String> toString;

	/**
	 * Constructs a new key serializer.
	 *
	 * @param toString function used to convert the value to its JSON string representation
	 */
	public FieldNameSerializer(final Function<T, String> toString) {
		this.toString = toString;
	}

	/**
	 * @see ValueSerializer#serialize(Object, JsonGenerator, SerializationContext)
	 */
	@Override
	@SuppressWarnings("resource")
	public void serialize(final T value, final JsonGenerator gen, final SerializationContext ctxt) throws JacksonException {
		gen.writeName(toString.apply(value));
	}
}
