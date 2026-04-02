package org.apiphany.json.jackson2.serializers;

import java.io.IOException;

import org.apiphany.security.Sensitive;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

/**
 * This serializer is used to redact the value.
 *
 * @author Radu Sebastian LAZIN
 */
public class RedactedValueSerializer extends JsonSerializer<Object> implements ContextualSerializer {

	/**
	 * The type of the value being serialized, used to determine how to redact it.
	 */
	private final JavaType type;

	/**
	 * Default constructor.
	 */
	public RedactedValueSerializer() {
		this(null);
	}

	/**
	 * Constructs a new {@link RedactedValueSerializer} with the specified type.
	 *
	 * @param type the type of the value being serialized
	 */
	private RedactedValueSerializer(final JavaType type) {
		this.type = type;
	}

	/**
	 * @see JsonSerializer#serialize(Object, JsonGenerator, SerializerProvider)
	 */
	@Override
	public void serialize(final Object value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
		if (null != type && type.isCollectionLikeType()) {
			gen.writeStartArray();
			gen.writeString(Sensitive.Value.REDACTED);
			gen.writeEndArray();
		} else if (type != null && type.isMapLikeType()) {
			gen.writeStartObject();
			gen.writeStringField("redacted", Sensitive.Value.REDACTED);
			gen.writeEndObject();
		} else {
			gen.writeString(Sensitive.Value.REDACTED);
		}
	}

	/**
	 * @see ContextualSerializer#createContextual(SerializerProvider, BeanProperty)
	 */
	@Override
	public JsonSerializer<?> createContextual(final SerializerProvider provider, final BeanProperty property) {
		if (null != property) {
			return new RedactedValueSerializer(property.getType());
		}
		return this;
	}
}
