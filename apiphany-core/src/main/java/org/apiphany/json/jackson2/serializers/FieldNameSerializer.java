package org.apiphany.json.jackson2.serializers;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

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
public class FieldNameSerializer<T> extends JsonSerializer<T> {

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
	 * @see JsonSerializer#serialize(Object, JsonGenerator, SerializerProvider)
	 */
	@Override
	public void serialize(final T value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
		gen.writeFieldName(toString.apply(value));
	}
}
