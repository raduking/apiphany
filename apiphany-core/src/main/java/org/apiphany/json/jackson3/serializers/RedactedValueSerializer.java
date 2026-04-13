package org.apiphany.json.jackson3.serializers;

import org.apiphany.security.Sensitive;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * This serializer is used to redact the value.
 *
 * @author Radu Sebastian LAZIN
 */
public class RedactedValueSerializer extends StdSerializer<Object> {

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
	protected RedactedValueSerializer(final JavaType type) {
		super(Object.class);
		this.type = type;
	}

	/**
	 * @see StdSerializer#serialize(Object, JsonGenerator, SerializationContext)
	 */
	@Override
	@SuppressWarnings("resource")
	public void serialize(final Object value, final JsonGenerator gen, final SerializationContext ctxt) throws JacksonException {
		if (null == value) {
			gen.writeNull();
			return;
		}
		if (null == type) {
			gen.writeString(Sensitive.Value.REDACTED);
			return;
		}
		if (type.isCollectionLikeType() || type.isArrayType()) {
			gen.writeStartArray();
			gen.writeString(Sensitive.Value.REDACTED);
			gen.writeEndArray();
			return;
		}
		if (type.isMapLikeType()) {
			gen.writeStartObject();
			gen.writeStringProperty(Sensitive.Field.SERIALIZED_NAME, Sensitive.Value.REDACTED);
			gen.writeEndObject();
			return;
		}
		gen.writeString(Sensitive.Value.REDACTED);
	}

	/**
	 * @see ValueSerializer#createContextual(SerializationContext, BeanProperty)
	 */
	@Override
	public ValueSerializer<?> createContextual(final SerializationContext ctxt, final BeanProperty property) {
		if (null == property) {
			return this;
		}
		return new RedactedValueSerializer(property.getType());
	}

	/**
	 * Returns the type of the value being serialized.
	 *
	 * @return the type of the value being serialized
	 */
	public JavaType getType() {
		return type;
	}
}
