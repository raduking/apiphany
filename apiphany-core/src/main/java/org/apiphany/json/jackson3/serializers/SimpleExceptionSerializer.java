package org.apiphany.json.jackson3.serializers;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Simple exception serializer that only writes the class name.
 *
 * @author Radu Sebastian LAZIN
 */
public class SimpleExceptionSerializer extends StdSerializer<Exception> {

	/**
	 * Default constructor.
	 */
	public SimpleExceptionSerializer() {
		super(Exception.class);
	}

	/**
	 * @see StdSerializer#serialize(Object, JsonGenerator, SerializationContext)
	 */
	@Override
	@SuppressWarnings("resource")
	public void serialize(final Exception value, final JsonGenerator gen, final SerializationContext ctxt) throws JacksonException {
		gen.writeString(value.getClass().getCanonicalName());
	}
}
